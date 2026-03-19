package com.andyl.iris.data.wallpaper.repository

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import com.andyl.iris.domain.model.WallpaperId
import com.andyl.iris.domain.repository.WallpaperRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import com.andyl.iris.domain.model.ScaleMode
import androidx.core.graphics.scale

class WallpaperRepositoryImpl(
    private val context: Context
) : WallpaperRepository {

    override suspend fun applyWallpaper(
        wallpaperId: WallpaperId,
        scaleMode: ScaleMode
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val uri = wallpaperId.value.toUri()
            val wallpaperManager = WallpaperManager.getInstance(context)

            val metrics = context.resources.displayMetrics
            val screenWidth = metrics.widthPixels.toFloat()
            val screenHeight = metrics.heightPixels.toFloat()

            // 1. Decodificar con sample size para no reventar la RAM
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            options.inSampleSize = calculateInSampleSize(options, screenWidth.toInt(), screenHeight.toInt())
            options.inJustDecodeBounds = false

            context.contentResolver.openInputStream(uri)?.use { input ->
                val originalBitmap = BitmapFactory.decodeStream(input, null, options)
                    ?: throw Exception("Bitmap nulo")

                // 2. Aplicar el escalado según el modo de Iris
                val finalBitmap = when (scaleMode) {
                    ScaleMode.CROP -> centerCrop(originalBitmap, screenWidth, screenHeight)
                    ScaleMode.STRETCH -> stretchFill(originalBitmap, screenWidth, screenHeight)
                    ScaleMode.FIT -> centerFit(originalBitmap, screenWidth, screenHeight)
                }

                wallpaperManager.setBitmap(finalBitmap)

                // Limpieza de memoria (importante en wallpapers)
                if (finalBitmap != originalBitmap) originalBitmap.recycle()
            } ?: throw Exception("No se pudo abrir el stream")
        }
    }

    // --- FUNCIONES DE TRANSFORMACIÓN ---

    private fun centerCrop(source: Bitmap, targetW: Float, targetH: Float): Bitmap {
        val sourceW = source.width.toFloat()
        val sourceH = source.height.toFloat()

        val scale = Math.max(targetW / sourceW, targetH / sourceH)
        val matrix = Matrix().apply { setScale(scale, scale) }

        val dx = (targetW - sourceW * scale) / 2f
        val dy = (targetH - sourceH * scale) / 2f
        matrix.postTranslate(dx, dy)

        val result = Bitmap.createBitmap(targetW.toInt(), targetH.toInt(), Bitmap.Config.ARGB_8888)
        Canvas(result).drawBitmap(source, matrix, null)
        return result
    }

    private fun stretchFill(source: Bitmap, targetW: Float, targetH: Float): Bitmap {
        // Estirado directo sin mantener relación de aspecto
        return source.scale(targetW.toInt(), targetH.toInt())
    }

    private fun centerFit(source: Bitmap, targetW: Float, targetH: Float): Bitmap {
        val sourceW = source.width.toFloat()
        val sourceH = source.height.toFloat()

        val scale = Math.min(targetW / sourceW, targetH / sourceH)
        val matrix = Matrix().apply { setScale(scale, scale) }

        val dx = (targetW - sourceW * scale) / 2f
        val dy = (targetH - sourceH * scale) / 2f
        matrix.postTranslate(dx, dy)

        val result = Bitmap.createBitmap(targetW.toInt(), targetH.toInt(), Bitmap.Config.ARGB_8888)
        Canvas(result).drawBitmap(source, matrix, null)
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

