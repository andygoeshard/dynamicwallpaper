package com.andyl.iris.service

import android.content.Context
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import java.io.File

class IrisLiveWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = LiveVideoEngine()

    private inner class LiveVideoEngine : Engine() {

        private var player: MediaPlayer? = null
        private var surfaceHolder: SurfaceHolder? = null
        private var lastPath: String? = null
        private var handler: Handler? = null
        private var isVisible = true
        private var consecutiveErrors = 0
        private var lastErrorTime = 0L
        private var staticShownForPath: String? = null

        private val refreshRunnable = object : Runnable {
            override fun run() {
                try {
                    val holder = surfaceHolder
                    if (holder != null && currentVideoPath() != lastPath) {
                        Log.d("LIVE_WALLPAPER", "Video path changed, switching...")
                        releasePlayer()
                        startPlayback(holder)
                    } else {
                        // Safety net: if the video stopped while we're visible,
                        // restart it (covers devices where completion/error
                        // listeners don't fire reliably).
                        val current = player
                        if (isVisible && current != null && !current.isPlaying) {
                            Log.d("LIVE_WALLPAPER", "Video stalled, restarting...")
                            try {
                                current.seekTo(0)
                                current.start()
                            } catch (e: Exception) {
                                Log.e("LIVE_WALLPAPER", "restart stalled video failed", e)
                            }
                        }
                    }
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
            startPlayback(holder)
            handler?.removeCallbacks(refreshRunnable)
            handler?.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS)
        }

        override fun onSurfaceRedrawNeeded(holder: SurfaceHolder) {
            super.onSurfaceRedrawNeeded(holder)
            startPlayback(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            Log.d("LIVE_WALLPAPER", "Visibility: $visible")
            isVisible = visible
            try {
                if (visible) {
                    val holder = surfaceHolder
                    if (holder != null && currentVideoPath() != lastPath) {
                        releasePlayer()
                        startPlayback(holder)
                    } else {
                        player?.let { if (!it.isPlaying) it.start() }
                    }
                } else {
                    player?.let { if (it.isPlaying) it.pause() }
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
        }

        override fun onDestroy() {
            Log.d("LIVE_WALLPAPER", "Engine onDestroy")
            handler?.removeCallbacks(refreshRunnable)
            handler = null
            releasePlayer()
            super.onDestroy()
        }

        private fun currentVideoPath(): String? =
            getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
                .getString("live_video_path", null)

        private fun startPlayback(holder: SurfaceHolder) {
            try {
                val videoPath = currentVideoPath()
                if (videoPath.isNullOrBlank()) return
                val videoFile = File(videoPath)
                if (!videoFile.exists()) return

                // Guard against corrupt/truncated files: feeding those to
                // MediaPlayer can cause a native codec crash that kills the
                // process (try/catch can't catch it). If invalid, draw a frame.
                if (!isPlayable(videoFile)) {
                    Log.e("LIVE_WALLPAPER", "Video not playable, drawing static frame: $videoPath")
                    drawStaticFrame(holder)
                    return
                }

                val current = player
                if (current != null) {
                    if (lastPath == videoPath) {
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

                lastPath = videoPath
                val mediaPlayer = MediaPlayer()
                mediaPlayer.setDataSource(videoFile.absolutePath)
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
                        handler?.post {
                            surfaceHolder?.let { drawStaticFrame(it) }
                        }
                    } else {
                        handler?.post {
                            surfaceHolder?.let { startPlayback(it) }
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
                player = mediaPlayer
                Log.d("LIVE_WALLPAPER", "Playback started: $videoPath")
            } catch (e: Exception) {
                Log.e("LIVE_WALLPAPER", "startPlayback error", e)
                drawStaticFrame(holder)
            }
        }

        private fun isPlayable(file: File): Boolean {
            return try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(file.absolutePath)
                val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                retriever.release()
                duration > 0 && file.length() >= 100_000
            } catch (e: Exception) {
                Log.e("LIVE_WALLPAPER", "isPlayable check failed", e)
                false
            }
        }

        private fun drawStaticFrame(holder: SurfaceHolder) {
            val currentPath = currentVideoPath()
            if (staticShownForPath == currentPath) return

            // Error containment: never show plain black — draw a themed gradient.
            // Do NOT fall back to last_applied_wallpaper (it's a stale static image
            // unrelated to the video).
            drawGradient(holder)
            staticShownForPath = currentPath
        }

        private fun drawGradient(holder: SurfaceHolder) {
            try {
                val canvas = holder.lockCanvas() ?: return
                canvas.drawColor(Color.rgb(15, 18, 26))
                val paint = android.graphics.Paint()
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
    }

    companion object {
        private const val REFRESH_INTERVAL_MS = 30_000L
    }
}
