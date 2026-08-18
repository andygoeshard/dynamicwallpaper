package com.andyl.iris.data.wallpaper.repository

import android.app.WallpaperManager
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import android.provider.MediaStore
import android.util.Log
import com.andyl.iris.domain.helper.isVideoUri
import com.andyl.iris.domain.model.WallpaperId
import com.andyl.iris.domain.repository.WallpaperRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import com.andyl.iris.domain.model.ScaleMode
import androidx.core.graphics.createBitmap
import java.io.File
import java.io.InputStream

class WallpaperRepositoryImpl(
    private val context: Context
) : WallpaperRepository {

    private val overlayPrefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    private fun resolveGalleryUri(path: String): String {
        if (!path.startsWith("gallery://")) return path
        val bucketId = overlayPrefs.getString("random_gallery_bucket", null)
        val uris = mutableListOf<String>()
        val projection = arrayOf(MediaStore.Images.Media._ID)

        fun queryUris(selection: String?, selectionArgs: Array<String>?) {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    uris.add(
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(idColumn)).toString()
                    )
                }
            }
        }

        if (!bucketId.isNullOrBlank()) {
            queryUris("${MediaStore.Images.Media.BUCKET_ID} = ?", arrayOf(bucketId))
        }
        if (uris.isEmpty()) {
            // Empty or unavailable album: fall back to all photos.
            queryUris(null, null)
        }
        return if (uris.isEmpty()) path else uris.random()
    }

    override suspend fun applyWallpaper(
        wallpaperId: WallpaperId,
        scaleMode: ScaleMode,
        target: Int,
        cropX: Float?,
        cropY: Float?,
        cropScale: Float?
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val path = resolveGalleryUri(wallpaperId.value)
            Log.d("IRIS_WALLPAPER", ">>> START APPLYING: $path | Mode: $scaleMode | Crop: $cropX, $cropY, $cropScale")

            if (isVideoUri(path)) {
                Log.d("IRIS_WALLPAPER", ">>> Video detected, enabling live video: $path")
                overlayPrefs.edit()
                    .putString("live_video_path", path)
                    .putBoolean("live_video_enabled", true)
                    .apply()
                return@runCatching path
            }

            val wallpaperManager = WallpaperManager.getInstance(context)

            val androidFlags = when (target) {
                1 -> WallpaperManager.FLAG_SYSTEM
                2 -> WallpaperManager.FLAG_LOCK
                else -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            }

            val metrics = context.resources.displayMetrics
            val screenWidth = metrics.widthPixels.toFloat()
            val screenHeight = metrics.heightPixels.toFloat()

            // 1. SAFE STREAM RETRIEVAL
            fun getInputStream(): InputStream {
                return if (path.startsWith("/") || path.startsWith("file://")) {
                    val cleanPath = path.removePrefix("file://")
                    val file = File(cleanPath)
                    if (!file.exists()) {
                        Log.e("IRIS_WALLPAPER", "File does not exist: $cleanPath")
                        throw Exception("File not found")
                    }
                    file.inputStream()
                } else {
                    context.contentResolver.openInputStream(path.toUri()) 
                        ?: throw Exception("ContentResolver null stream")
                }
            }

            // 2. PRE-DECODE FOR SIZE
            val options = BitmapFactory.Options().apply { 
                inJustDecodeBounds = true 
            }
            getInputStream().use {
                BitmapFactory.decodeStream(it, null, options)
            }

            // Important: Manual crop needs full resolution if possible
            options.inSampleSize = if (cropScale != null) 1 else calculateInSampleSize(options, screenWidth.toInt(), screenHeight.toInt())
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            // 3. FULL DECODE AND SCALE
            getInputStream().use { input ->
                val originalBitmap = BitmapFactory.decodeStream(input, null, options)
                    ?: throw Exception("Bitmap decode failed")

                val finalBitmap = if (cropX != null && cropY != null && cropScale != null) {
                    applyManualCrop(originalBitmap, screenWidth, screenHeight, cropX, cropY, cropScale)
                } else {
                    when (scaleMode) {
                        ScaleMode.CROP -> centerCrop(originalBitmap, screenWidth, screenHeight)
                        ScaleMode.STRETCH -> stretchFill(originalBitmap, screenWidth, screenHeight)
                        ScaleMode.FIT -> centerFit(originalBitmap, screenWidth, screenHeight)
                    }
                }

                val bitmapWithOverlay = if (overlayPrefs.getBoolean("overlay_text_enabled", false)) {
                    val text = overlayPrefs.getString("overlay_text", null)
                    if (!text.isNullOrBlank()) {
                        drawTextOverlay(finalBitmap, text)
                    } else {
                        finalBitmap
                    }
                } else {
                    finalBitmap
                }

                // 4. APPLY TO SYSTEM
                wallpaperManager.setBitmap(bitmapWithOverlay, null, true, androidFlags)
                Log.d("IRIS_WALLPAPER", ">>> SUCCESS applying to target $androidFlags")

                // 5. CLEANUP
                if (bitmapWithOverlay != originalBitmap) {
                    originalBitmap.recycle()
                }
                if (bitmapWithOverlay != finalBitmap) {
                    finalBitmap.recycle()
                }
                bitmapWithOverlay.recycle()

                path
            }
        }.onFailure { e ->
            Log.e("IRIS_WALLPAPER", ">>> FATAL ERROR: ${e.message}", e)
        }
    }

    private fun drawTextOverlay(bitmap: Bitmap, text: String): Bitmap {
        val result = createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(bitmap, 0f, 0f, highQualityPaint)

        val fontSize = bitmap.width * 0.045f
        val textPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            textSize = fontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(8f, 0f, 4f, android.graphics.Color.BLACK)
        }

        val scrimPaint = Paint().apply {
            color = android.graphics.Color.argb(140, 0, 0, 0)
        }

        val scrimHeight = fontSize * 2.6f
        val scrimTop = bitmap.height * 0.82f
        canvas.drawRect(0f, scrimTop - scrimHeight, bitmap.width.toFloat(), bitmap.height * 0.92f, scrimPaint)
        canvas.drawText(text, bitmap.width / 2f, scrimTop, textPaint)

        return result
    }

    override suspend fun cropAndSaveWallpaper(
        wallpaperId: WallpaperId,
        cropX: Float,
        cropY: Float,
        cropScale: Float
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val path = wallpaperId.value
            val metrics = context.resources.displayMetrics
            val screenWidth = metrics.widthPixels.toFloat()
            val screenHeight = metrics.heightPixels.toFloat()

            fun getInputStream(): InputStream {
                return if (path.startsWith("/") || path.startsWith("file://")) {
                    File(path.removePrefix("file://")).inputStream()
                } else {
                    context.contentResolver.openInputStream(path.toUri()) ?: throw Exception("Stream null")
                }
            }

            val options = BitmapFactory.Options().apply { 
                inSampleSize = 1 
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            getInputStream().use { input ->
                val original = BitmapFactory.decodeStream(input, null, options) ?: throw Exception("Decode fail")
                val cropped = applyManualCrop(original, screenWidth, screenHeight, cropX, cropY, cropScale)
                
                val outputFile = File(context.filesDir, "iris_cropped_${System.currentTimeMillis()}.jpg")
                outputFile.outputStream().use { out ->
                    cropped.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
                
                if (cropped != original) cropped.recycle()
                original.recycle()
                
                outputFile.absolutePath
            }
        }
    }

    private val highQualityPaint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
        isDither = true
    }

    private fun applyManualCrop(source: Bitmap, targetW: Float, targetH: Float, cropX: Float, cropY: Float, cropScale: Float): Bitmap {
        val sourceW = source.width.toFloat()
        val sourceH = source.height.toFloat()
        
        val fitScale = (targetW / sourceW).coerceAtMost(targetH / sourceH)
        val initialDx = (targetW - sourceW * fitScale) / 2f
        val initialDy = (targetH - sourceH * fitScale) / 2f
        
        val matrix = Matrix().apply { 
            postScale(fitScale, fitScale)
            postTranslate(initialDx, initialDy)
            
            postScale(cropScale, cropScale, targetW / 2f, targetH / 2f)
            postTranslate(cropX, cropY)
        }

        val result = createBitmap(targetW.toInt(), targetH.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(android.graphics.Color.BLACK)
        canvas.drawBitmap(source, matrix, highQualityPaint)
        return result
    }

    private fun centerCrop(source: Bitmap, targetW: Float, targetH: Float): Bitmap {
        val sourceW = source.width.toFloat()
        val sourceH = source.height.toFloat()
        val scale = (targetW / sourceW).coerceAtLeast(targetH / sourceH)
        val matrix = Matrix().apply { setScale(scale, scale) }
        val dx = (targetW - sourceW * scale) / 2f
        val dy = (targetH - sourceH * scale) / 2f
        matrix.postTranslate(dx, dy)
        val result = createBitmap(targetW.toInt(), targetH.toInt(), Bitmap.Config.ARGB_8888)
        Canvas(result).drawBitmap(source, matrix, highQualityPaint)
        return result
    }

    private fun stretchFill(source: Bitmap, targetW: Float, targetH: Float): Bitmap {
        val result = createBitmap(targetW.toInt(), targetH.toInt(), Bitmap.Config.ARGB_8888)
        val matrix = Matrix().apply { 
            setScale(targetW / source.width, targetH / source.height)
        }
        Canvas(result).drawBitmap(source, matrix, highQualityPaint)
        return result
    }

    private fun centerFit(source: Bitmap, targetW: Float, targetH: Float): Bitmap {
        val sourceW = source.width.toFloat()
        val sourceH = source.height.toFloat()
        val scale = (targetW / sourceW).coerceAtMost(targetH / sourceH)
        val matrix = Matrix().apply { setScale(scale, scale) }
        val dx = (targetW - sourceW * scale) / 2f
        val dy = (targetH - sourceH * scale) / 2f
        matrix.postTranslate(dx, dy)
        val result = createBitmap(targetW.toInt(), targetH.toInt(), Bitmap.Config.ARGB_8888)
        Canvas(result).drawBitmap(source, matrix, highQualityPaint)
        return result
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
