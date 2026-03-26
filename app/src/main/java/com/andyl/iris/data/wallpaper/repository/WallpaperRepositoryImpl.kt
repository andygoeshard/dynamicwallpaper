package com.andyl.iris.data.wallpaper.repository

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.Log
import com.andyl.iris.domain.model.WallpaperId
import com.andyl.iris.domain.repository.WallpaperRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import com.andyl.iris.domain.model.ScaleMode
import androidx.core.graphics.scale
import androidx.core.graphics.createBitmap

class WallpaperRepositoryImpl(
    private val context: Context
) : WallpaperRepository {

    override suspend fun applyWallpaper(
        wallpaperId: WallpaperId,
        scaleMode: ScaleMode,
        target: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val uri = wallpaperId.value.toUri()
            val wallpaperManager = WallpaperManager.getInstance(context)

            val androidFlags = when (target) {
                1 -> WallpaperManager.FLAG_SYSTEM
                2 -> WallpaperManager.FLAG_LOCK
                else -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            }

            val metrics = context.resources.displayMetrics
            val screenWidth = metrics.widthPixels.toFloat()
            val screenHeight = metrics.heightPixels.toFloat()

            // 2. Decodificar con sample size inteligente
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            options.inSampleSize = calculateInSampleSize(options, screenWidth.toInt(), screenHeight.toInt())
            options.inJustDecodeBounds = false

            context.contentResolver.openInputStream(uri)?.use { input ->
                val originalBitmap = BitmapFactory.decodeStream(input, null, options)
                    ?: throw Exception("No se pudo decodificar la imagen")

                // 3. Transformación de escala
                val finalBitmap = when (scaleMode) {
                    ScaleMode.CROP -> centerCrop(originalBitmap, screenWidth, screenHeight)
                    ScaleMode.STRETCH -> stretchFill(originalBitmap, screenWidth, screenHeight)
                    ScaleMode.FIT -> centerFit(originalBitmap, screenWidth, screenHeight)
                }

                // 4. APLICAR SEGÚN EL TARGET
                // null en el segundo parámetro es para el Rect de visibilidad (por defecto full)
                // true es para que Android maneje el backup del wallpaper
                wallpaperManager.setBitmap(finalBitmap, null, true, androidFlags)

                Log.d("WALLPAPER_REPO", "Fondo aplicado a flags: $androidFlags")

                // 5. Limpieza agresiva de memoria
                if (finalBitmap != originalBitmap) {
                    originalBitmap.recycle()
                }
                // No reciclamos finalBitmap inmediatamente porque setBitmap es asíncrono
                // pero al salir del scope el GC hará lo suyo.
            } ?: throw Exception("Stream de URI no disponible")
        }.onFailure { e ->
            Log.e("WALLPAPER_REPO", "Error aplicando wallpaper: ${e.message}")
        }
    }

    // --- FUNCIONES DE TRANSFORMACIÓN ---

    private fun centerCrop(source: Bitmap, targetW: Float, targetH: Float): Bitmap {
        val sourceW = source.width.toFloat()
        val sourceH = source.height.toFloat()

        val scale = (targetW / sourceW).coerceAtLeast(targetH / sourceH)
        val matrix = Matrix().apply { setScale(scale, scale) }

        val dx = (targetW - sourceW * scale) / 2f
        val dy = (targetH - sourceH * scale) / 2f
        matrix.postTranslate(dx, dy)

        val result = createBitmap(targetW.toInt(), targetH.toInt())
        Canvas(result).drawBitmap(source, matrix, null)
        return result
    }

    private fun stretchFill(source: Bitmap, targetW: Float, targetH: Float): Bitmap {
        return source.scale(targetW.toInt(), targetH.toInt())
    }

    private fun centerFit(source: Bitmap, targetW: Float, targetH: Float): Bitmap {
        val sourceW = source.width.toFloat()
        val sourceH = source.height.toFloat()

        val scale = (targetW / sourceW).coerceAtMost(targetH / sourceH)
        val matrix = Matrix().apply { setScale(scale, scale) }

        val dx = (targetW - sourceW * scale) / 2f
        val dy = (targetH - sourceH * scale) / 2f
        matrix.postTranslate(dx, dy)

        val result = createBitmap(targetW.toInt(), targetH.toInt())
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

