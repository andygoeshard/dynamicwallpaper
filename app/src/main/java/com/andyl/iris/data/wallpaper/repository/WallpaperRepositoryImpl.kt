package com.andyl.iris.data.wallpaper.repository

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.Log
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

    override suspend fun applyWallpaper(
        wallpaperId: WallpaperId,
        scaleMode: ScaleMode,
        target: Int,
        cropX: Float?,
        cropY: Float?,
        cropScale: Float?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val path = wallpaperId.value
            Log.d("IRIS_WALLPAPER", ">>> START APPLYING: $path | Mode: $scaleMode | Crop: $cropX, $cropY, $cropScale")
            
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

                // 4. APPLY TO SYSTEM
                wallpaperManager.setBitmap(finalBitmap, null, true, androidFlags)
                Log.d("IRIS_WALLPAPER", ">>> SUCCESS applying to target $androidFlags")

                // 5. CLEANUP
                if (finalBitmap != originalBitmap) {
                    originalBitmap.recycle()
                }
                finalBitmap.recycle() // Recycle the final one too after sending to manager
            }
        }.onFailure { e ->
            Log.e("IRIS_WALLPAPER", ">>> FATAL ERROR: ${e.message}", e)
        }
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
