package com.andyl.iris.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.util.Xml
import android.view.SurfaceHolder
import org.xmlpull.v1.XmlPullParser
import java.io.File

class IrisLiveWallpaperService : WallpaperService() {

    private data class Target(val isVideo: Boolean, val path: String?)

    override fun onCreateEngine(): Engine = LiveVideoEngine()

    private inner class LiveVideoEngine : Engine() {

        private var player: MediaPlayer? = null
        private var surfaceHolder: SurfaceHolder? = null
        private var lastTarget: Target? = null
        private var handler: Handler? = null
        private var isVisible = true
        private var consecutiveErrors = 0
        private var lastErrorTime = 0L

        // Tracks the last path whose playback failed so periodic refreshes can
        // retry it (e.g. a video that was still being copied when first read).
        private var failedVideoPath: String? = null
        private var failureAttempts = 0

        // Animated GIF/WEBP rendering state. AnimatedImageDrawable (API 28+)
        // handles GIF and animated WEBP; Movie is the legacy fallback.
        private var gifDrawable: android.graphics.drawable.AnimatedImageDrawable? = null
        private var gifMovie: android.graphics.Movie? = null
        private var gifStartedAtMs = 0L
        private var gifPlaying = false

        private val gifFrameRunnable = object : Runnable {
            override fun run() {
                if (!gifPlaying) return
                surfaceHolder?.let { drawGifFrame(it) }
                handler?.postDelayed(this, GIF_FRAME_INTERVAL_MS)
            }
        }

        private val refreshRunnable = object : Runnable {
            override fun run() {
                try {
                    val holder = surfaceHolder
                    if (holder != null) render(holder)
                } catch (e: Exception) {
                    Log.e("LIVE_WALLPAPER", "refresh error", e)
                }
                handler?.postDelayed(this, REFRESH_INTERVAL_MS)
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            handler = Handler(Looper.getMainLooper())
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            Log.d("LIVE_WALLPAPER", "Surface created")
            surfaceHolder = holder
            render(holder, force = true)
            handler?.removeCallbacks(refreshRunnable)
            handler?.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS)
        }

        override fun onSurfaceRedrawNeeded(holder: SurfaceHolder) {
            super.onSurfaceRedrawNeeded(holder)
            render(holder, force = true)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            Log.d("LIVE_WALLPAPER", "Visibility: $visible")
            isVisible = visible
            try {
                val holder = surfaceHolder
                if (holder != null) {
                    if (visible) {
                        render(holder, force = true)
                    } else {
                        player?.let { if (it.isPlaying) it.pause() }
                        pauseGif()
                    }
                }
            } catch (e: Exception) {
                Log.e("LIVE_WALLPAPER", "visibility error", e)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            Log.d("LIVE_WALLPAPER", "Surface destroyed")
            surfaceHolder = null
            handler?.removeCallbacks(refreshRunnable)
            releasePlayer()
            stopGif()
        }

        override fun onDestroy() {
            Log.d("LIVE_WALLPAPER", "Engine onDestroy")
            handler?.removeCallbacks(refreshRunnable)
            handler = null
            releasePlayer()
            stopGif()
            super.onDestroy()
        }

        // This service runs in its own process (:wallpaper) while the worker
        // writes prefs from the app process. SharedPreferences' in-memory cache
        // is per-process, so reading it here would return stale values forever.
        // Read the backing XML straight from disk to always see the latest target.
        private fun readLivePrefs(): Map<String, String> {
            return try {
                val file = File(this@IrisLiveWallpaperService.dataDir, "shared_prefs/user_preferences.xml")
                if (!file.exists()) return emptyMap()
                val parser = Xml.newPullParser()
                parser.setInput(file.inputStream(), "UTF-8")
                val map = HashMap<String, String>()
                var event = parser.eventType
                while (event != XmlPullParser.END_DOCUMENT) {
                    if (event == XmlPullParser.START_TAG) {
                        when (parser.name) {
                            "string" -> {
                                val name = parser.getAttributeValue(null, "name")
                                event = parser.next()
                                map[name] = parser.text ?: ""
                            }
                            "boolean" -> {
                                val name = parser.getAttributeValue(null, "name")
                                map[name] = parser.getAttributeValue(null, "value")
                            }
                        }
                    }
                    event = parser.next()
                }
                map
            } catch (e: Exception) {
                Log.e("LIVE_WALLPAPER", "readLivePrefs failed", e)
                emptyMap()
            }
        }

        private fun currentTarget(): Target {
            // Preferred source: dedicated cross-process file written by the
            // worker. Fall back to prefs for anything written before the file
            // mechanism existed (or directly via the video picker).
            val store = com.andyl.iris.domain.helper.LiveTargetStore.read(applicationContext)
            if (store != null) {
                return Target(isVideo = store.isVideo, path = store.path)
            }
            val prefs = readLivePrefs()
            val videoEnabled = prefs["live_video_enabled"] == "true"
            val videoPath = prefs["live_video_path"]
            if (videoEnabled && !videoPath.isNullOrBlank()) {
                return Target(isVideo = true, path = videoPath)
            }
            val staticPath = prefs["live_static_path"]
            if (!staticPath.isNullOrBlank()) {
                return Target(isVideo = false, path = staticPath)
            }
            return Target(isVideo = false, path = null)
        }

        private fun render(holder: SurfaceHolder, force: Boolean = false) {
            val target = currentTarget()
            if (!force && target == lastTarget) {
                // Safety net: if the video stopped while we're visible,
                // restart it (covers devices where completion/error
                // listeners don't fire reliably).
                val current = player
                if (target.isVideo && isVisible) {
                    if (current != null && !current.isPlaying) {
                        Log.d("LIVE_WALLPAPER", "Video stalled, restarting...")
                        try {
                            current.seekTo(0)
                            current.start()
                        } catch (e: Exception) {
                            Log.e("LIVE_WALLPAPER", "restart stalled video failed", e)
                        }
                    } else if (current == null && !target.path.isNullOrEmpty() &&
                        target.path == failedVideoPath &&
                        failureAttempts < MAX_PLAYBACK_RETRIES
                    ) {
                        // Previous start attempt failed (missing/corrupt file,
                        // codec error...). Retry periodically instead of staying
                        // on the fallback gradient forever.
                        failureAttempts++
                        Log.d("LIVE_WALLPAPER", "Retrying failed video ($failureAttempts/$MAX_PLAYBACK_RETRIES): ${target.path}")
                        startPlayback(holder, target.path!!)
                    }
                }
                return
            }

            lastTarget = target
            failedVideoPath = null
            failureAttempts = 0
            releasePlayer()
            stopGif()

            when {
                target.path == null -> drawGradient(holder)
                target.isVideo -> startPlayback(holder, target.path)
                else -> drawStaticPhoto(holder, target.path)
            }
        }

        private fun markPlaybackFailed(videoPath: String) {
            if (failedVideoPath != videoPath) {
                failedVideoPath = videoPath
                failureAttempts = 1
            } else {
                failureAttempts++
            }
        }

        // Animated images (GIF/animated WEBP) are not playable by MediaPlayer.
        // Users pick them through the same flows as videos, copies may be
        // renamed to .mp4, and MediaStore content URIs carry no extension, so
        // detect by extension AND magic bytes.
        private fun isAnimatedImage(path: String): Boolean {
            val lower = path.lowercase()
            if (lower.endsWith(".gif") || lower.endsWith(".webp")) return true
            return try {
                val header = ByteArray(6)
                val read = if (path.startsWith("content://")) {
                    applicationContext.contentResolver.openInputStream(android.net.Uri.parse(path))?.use {
                        it.read(header)
                    } ?: return false
                } else {
                    java.io.FileInputStream(File(path.removePrefix("file://"))).use {
                        it.read(header)
                    }
                }
                read == 6 && String(header, Charsets.US_ASCII).startsWith("GIF8")
            } catch (e: Exception) {
                false
            }
        }

        private fun startPlayback(holder: SurfaceHolder, videoPath: String) {
            try {
                if (isAnimatedImage(videoPath)) {
                    Log.d("LIVE_WALLPAPER", "Animated image detected, animating instead of MediaPlayer: $videoPath")
                    startGifAnimation(holder, videoPath)
                    return
                }

                val isContentUri = videoPath.startsWith("content://")
                if (!isContentUri) {
                    val videoFile = File(videoPath.removePrefix("file://"))
                    if (!videoFile.exists()) {
                        Log.e("LIVE_WALLPAPER", "Video file missing: $videoPath")
                        markPlaybackFailed(videoPath)
                        drawGradient(holder)
                        return
                    }

                    // Guard against corrupt/truncated files: feeding those to
                    // MediaPlayer can cause a native codec crash that kills the
                    // process (try/catch can't catch it). If invalid, draw a frame.
                    if (!isPlayable(videoFile.absolutePath)) {
                        Log.e("LIVE_WALLPAPER", "Video not playable, drawing static frame: $videoPath")
                        markPlaybackFailed(videoPath)
                        drawGradient(holder)
                        return
                    }
                } else if (!isPlayable(videoPath)) {
                    Log.e("LIVE_WALLPAPER", "Content video not playable, drawing static frame: $videoPath")
                    markPlaybackFailed(videoPath)
                    drawGradient(holder)
                    return
                }

                val current = player
                if (current != null) {
                    if (lastTarget?.path == videoPath) {
                        // The surface may have been recreated (exit picker, redraw...).
                        // Re-attach the same player so the video keeps showing.
                        try {
                            current.setSurface(holder.surface)
                            if (!current.isPlaying) current.start()
                        } catch (e: Exception) {
                            Log.e("LIVE_WALLPAPER", "re-attach error", e)
                        }
                        return
                    }
                    releasePlayer()
                }

                val mediaPlayer = MediaPlayer()
                if (isContentUri) {
                    mediaPlayer.setDataSource(applicationContext, android.net.Uri.parse(videoPath))
                } else {
                    mediaPlayer.setDataSource(File(videoPath.removePrefix("file://")).absolutePath)
                }
                mediaPlayer.setSurface(holder.surface)
                mediaPlayer.setVolume(0f, 0f)
                mediaPlayer.setOnErrorListener { mp, what, extra ->
                    // Once MediaPlayer hits the ERROR state, seekTo/start on it
                    // will keep failing with -38 (INVALID_OPERATION) forever.
                    // The only clean recovery is to release it and create a fresh
                    // player.
                    Log.e("LIVE_WALLPAPER", "MediaPlayer error $what/$extra, recreating")
                    val now = SystemClock.elapsedRealtime()
                    val fastRepeat = (now - lastErrorTime) < 1500
                    lastErrorTime = now
                    if (fastRepeat) consecutiveErrors++ else consecutiveErrors = 1
                    try {
                        mp.release()
                    } catch (_: Exception) {
                    }
                    if (player === mp) player = null
                    if (consecutiveErrors > 3) {
                        // Repeated fast errors => the file is genuinely broken.
                        markPlaybackFailed(videoPath)
                        handler?.post {
                            surfaceHolder?.let { drawGradient(it) }
                        }
                    } else {
                        handler?.post {
                            surfaceHolder?.let { startPlayback(it, videoPath) }
                        }
                    }
                    true
                }
                mediaPlayer.setOnCompletionListener { mp ->
                    Log.d("LIVE_WALLPAPER", "Video completed, restarting")
                    try {
                        mp.seekTo(0)
                        mp.start()
                    } catch (e: Exception) {
                        Log.e("LIVE_WALLPAPER", "restart on completion failed", e)
                    }
                }
                mediaPlayer.prepare()
                // NOTE: we intentionally DON'T call setLooping(true). On some
                // devices looping suppresses the completion listener without
                // actually looping, which freezes the video at the end. Instead
                // we rely on onCompletionListener to restart, plus a periodic
                // stall check in refreshRunnable.
                mediaPlayer.start()
                consecutiveErrors = 0
                failedVideoPath = null
                failureAttempts = 0
                player = mediaPlayer
                Log.d("LIVE_WALLPAPER", "Playback started: $videoPath")
            } catch (e: Exception) {
                Log.e("LIVE_WALLPAPER", "startPlayback error", e)
                markPlaybackFailed(videoPath)
                drawGradient(holder)
            }
        }

        private fun isPlayable(path: String): Boolean {
            return try {
                val retriever = MediaMetadataRetriever()
                if (path.startsWith("content://")) {
                    retriever.setDataSource(applicationContext, android.net.Uri.parse(path))
                } else {
                    retriever.setDataSource(path)
                }
                val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                retriever.release()
                val bigEnough = path.startsWith("content://") || File(path).length() >= MIN_VIDEO_BYTES
                duration > 0 && bigEnough
            } catch (e: Exception) {
                Log.e("LIVE_WALLPAPER", "isPlayable check failed", e)
                false
            }
        }

        private fun drawStaticPhoto(holder: SurfaceHolder, rawPath: String) {
            // Animated images get a real animation loop instead of a frozen
            // first frame.
            if (isAnimatedImage(rawPath)) {
                Log.d("LIVE_WALLPAPER", "Animated image in static slot, animating: $rawPath")
                startGifAnimation(holder, rawPath)
                return
            }

            // Remote URLs (search results / remote packs) must be materialized
            // to a local file before BitmapFactory can touch them.
            val path = if (rawPath.startsWith("http://") || rawPath.startsWith("https://")) {
                materializeRemote(rawPath) ?: run {
                    Log.e("LIVE_WALLPAPER", "Remote photo download failed: $rawPath")
                    drawGradient(holder)
                    return
                }
            } else {
                rawPath
            }

            val bitmap = decodePhoto(path, holder.surfaceFrame.width(), holder.surfaceFrame.height())
            if (bitmap == null) {
                Log.e("LIVE_WALLPAPER", "Photo decode failed, drawing gradient: $path")
                drawGradient(holder)
                return
            }
            drawStaticBitmap(holder, bitmap)
        }

        private fun drawStaticBitmap(holder: SurfaceHolder, bitmap: Bitmap) {
            try {
                val canvas = holder.lockCanvas()
                if (canvas == null) {
                    bitmap.recycle()
                    return
                }
                val w = canvas.width.toFloat()
                val h = canvas.height.toFloat()
                val scale = maxOf(w / bitmap.width, h / bitmap.height)
                val dw = bitmap.width * scale
                val dh = bitmap.height * scale
                val left = (w - dw) / 2f
                val top = (h - dh) / 2f
                val paint = Paint().apply {
                    isAntiAlias = true
                    isFilterBitmap = true
                }
                canvas.drawBitmap(bitmap, null, RectF(left, top, left + dw, top + dh), paint)
                holder.unlockCanvasAndPost(canvas)
                bitmap.recycle()
            } catch (e: Exception) {
                Log.e("LIVE_WALLPAPER", "drawStaticPhoto error", e)
                bitmap.recycle()
                drawGradient(holder)
            }
        }

        private fun decodePhoto(path: String, reqW: Int, reqH: Int): Bitmap? {
            return try {
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                if (path.startsWith("content://")) {
                    this@IrisLiveWallpaperService.contentResolver.openInputStream(android.net.Uri.parse(path))?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }
                } else {
                    val file = File(path.removePrefix("file://"))
                    if (file.exists()) BitmapFactory.decodeFile(file.absolutePath, options) else null
                }
                if (options.outWidth <= 0 || options.outHeight <= 0) return null

                var inSampleSize = 1
                if (reqW > 0 && reqH > 0) {
                    while (options.outWidth / (inSampleSize * 2) >= reqW &&
                        options.outHeight / (inSampleSize * 2) >= reqH
                    ) {
                        inSampleSize *= 2
                    }
                }
                val decodeOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }

                if (path.startsWith("content://")) {
                    this@IrisLiveWallpaperService.contentResolver.openInputStream(android.net.Uri.parse(path))?.use {
                        BitmapFactory.decodeStream(it, null, decodeOptions)
                    }
                } else {
                    val file = File(path.removePrefix("file://"))
                    if (file.exists()) BitmapFactory.decodeFile(file.absolutePath, decodeOptions) else null
                }
            } catch (e: Exception) {
                Log.e("LIVE_WALLPAPER", "decodePhoto failed", e)
                null
            }
        }

        private fun drawGradient(holder: SurfaceHolder) {
            try {
                val canvas = holder.lockCanvas() ?: return
                canvas.drawColor(Color.rgb(15, 18, 26))
                val paint = Paint()
                paint.shader = android.graphics.LinearGradient(
                    0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(),
                    Color.rgb(46, 34, 84), Color.rgb(15, 18, 26),
                    android.graphics.Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
                holder.unlockCanvasAndPost(canvas)
            } catch (e: Exception) {
                Log.e("LIVE_WALLPAPER", "drawGradient error", e)
            }
        }

        private fun releasePlayer() {
            try {
                player?.let {
                    if (it.isPlaying) it.stop()
                    it.release()
                }
            } catch (_: Exception) {
            }
            player = null
        }

        // ---- Animated image (GIF / animated WEBP) rendering ----

        private fun startGifAnimation(holder: SurfaceHolder, path: String) {
            stopGif()
            try {
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    val source = if (path.startsWith("content://")) {
                        android.graphics.ImageDecoder.createSource(
                            applicationContext.contentResolver,
                            android.net.Uri.parse(path)
                        )
                    } else {
                        val file = File(path.removePrefix("file://"))
                        if (!file.exists()) {
                            Log.e("LIVE_WALLPAPER", "Animated image missing: $path")
                            markPlaybackFailed(path)
                            drawGradient(holder)
                            return
                        }
                        android.graphics.ImageDecoder.createSource(file)
                    }
                    val frame = holder.surfaceFrame
                    val drawable = android.graphics.ImageDecoder.decodeDrawable(source) { decoder, info, _ ->
                        // Downscale huge sources to roughly the surface size.
                        if (frame.width() > 0 && frame.height() > 0 &&
                            info.size.width > frame.width() * 2 && info.size.height > frame.height() * 2
                        ) {
                            decoder.setTargetSampleSize(2)
                        }
                    }
                    if (drawable is android.graphics.drawable.AnimatedImageDrawable) {
                        gifDrawable = drawable
                    } else {
                        // Not actually animated (static WEBP, single-frame GIF):
                        // show the frame instead of failing.
                        Log.d("LIVE_WALLPAPER", "Static image in animated slot: $path")
                        val bmp = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                        if (bmp != null) {
                            drawStaticBitmap(holder, bmp)
                        } else {
                            markPlaybackFailed(path)
                            drawGradient(holder)
                        }
                        return
                    }
                } else {
                    val stream = if (path.startsWith("content://")) {
                        applicationContext.contentResolver.openInputStream(android.net.Uri.parse(path))
                    } else {
                        val file = File(path.removePrefix("file://"))
                        if (!file.exists()) {
                            Log.e("LIVE_WALLPAPER", "Animated image missing: $path")
                            markPlaybackFailed(path)
                            drawGradient(holder)
                            return
                        }
                        file.inputStream()
                    }
                    gifMovie = stream?.use { android.graphics.Movie.decodeStream(it) }
                    if (gifMovie == null || gifMovie?.duration() == 0) {
                        // Movie only handles GIFs; for anything else (WEBP...)
                        // fall back to the first frame.
                        Log.d("LIVE_WALLPAPER", "Movie decode failed, trying first frame: $path")
                        val bmp = decodePhoto(path, holder.surfaceFrame.width(), holder.surfaceFrame.height())
                        if (bmp != null) {
                            drawStaticBitmap(holder, bmp)
                        } else {
                            markPlaybackFailed(path)
                            drawGradient(holder)
                        }
                        return
                    }
                }
                failedVideoPath = null
                failureAttempts = 0
                gifStartedAtMs = SystemClock.elapsedRealtime()
                gifPlaying = true
                handler?.post(gifFrameRunnable)
                Log.d("LIVE_WALLPAPER", "Gif animation started: $path")
            } catch (e: Exception) {
                Log.e("LIVE_WALLPAPER", "startGifAnimation error", e)
                markPlaybackFailed(path)
                drawGradient(holder)
            }
        }

        private fun drawGifFrame(holder: SurfaceHolder) {
            try {
                val canvas = holder.lockCanvas() ?: return
                val w = canvas.width.toFloat()
                val h = canvas.height.toFloat()

                val drawable = gifDrawable
                val movie = gifMovie
                when {
                    drawable != null -> {
                        if (!drawable.isRunning) drawable.start()
                        val iw = drawable.intrinsicWidth
                        val ih = drawable.intrinsicHeight
                        if (iw > 0 && ih > 0) {
                            val scale = maxOf(w / iw, h / ih)
                            val dw = iw * scale
                            val dh = ih * scale
                            val left = (w - dw) / 2f
                            val top = (h - dh) / 2f
                            drawable.setBounds(left.toInt(), top.toInt(), (left + dw).toInt(), (top + dh).toInt())
                        }
                        drawable.draw(canvas)
                    }
                    movie != null -> {
                        val duration = maxOf(movie.duration(), 1)
                        movie.setTime(((SystemClock.elapsedRealtime() - gifStartedAtMs) % duration).toInt())
                        val mw = movie.width().toFloat()
                        val mh = movie.height().toFloat()
                        val scale = maxOf(w / mw, h / mh)
                        canvas.save()
                        canvas.scale(scale, scale)
                        movie.draw(canvas, (w / scale - mw) / 2f, (h / scale - mh) / 2f, null)
                        canvas.restore()
                    }
                    else -> {
                        holder.unlockCanvasAndPost(canvas)
                        return
                    }
                }
                holder.unlockCanvasAndPost(canvas)
            } catch (e: Exception) {
                Log.e("LIVE_WALLPAPER", "drawGifFrame error", e)
            }
        }

        private fun pauseGif() {
            gifPlaying = false
            handler?.removeCallbacks(gifFrameRunnable)
        }

        private fun stopGif() {
            pauseGif()
            try {
                gifDrawable?.stop()
            } catch (_: Exception) {
            }
            gifDrawable = null
            gifMovie = null
        }

        // ---- Remote materialization ----

        private fun materializeRemote(url: String): String? {
            return try {
                val cacheDir = File(applicationContext.filesDir, "remote_cache").apply { mkdirs() }
                val hash = java.security.MessageDigest.getInstance("MD5")
                    .digest(url.toByteArray())
                    .joinToString("") { "%02x".format(it) }
                val ext = url.substringBefore('#').substringBefore('?').substringAfterLast('.', "").take(4)
                val out = File(cacheDir, "$hash.${ext.ifEmpty { "jpg" }}")
                if (!out.exists() || out.length() == 0L) {
                    val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                    connection.connectTimeout = 15_000
                    connection.readTimeout = 30_000
                    connection.inputStream.use { input ->
                        out.outputStream().use { o -> input.copyTo(o) }
                    }
                }
                out.absolutePath
            } catch (e: Exception) {
                Log.e("LIVE_WALLPAPER", "materializeRemote failed", e)
                null
            }
        }
    }

    companion object {
        private const val REFRESH_INTERVAL_MS = 30_000L
        private const val MAX_PLAYBACK_RETRIES = 10
        private const val MIN_VIDEO_BYTES = 50_000L
        private const val GIF_FRAME_INTERVAL_MS = 33L
    }
}