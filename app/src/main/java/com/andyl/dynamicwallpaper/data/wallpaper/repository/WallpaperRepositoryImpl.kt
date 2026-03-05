package com.andyl.dynamicwallpaper.data.wallpaper.repository

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import com.andyl.dynamicwallpaper.domain.model.WallpaperId
import com.andyl.dynamicwallpaper.domain.repository.WallpaperRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri

class WallpaperRepositoryImpl(
    private val context: Context
) : WallpaperRepository {

    override suspend fun applyWallpaper(wallpaperId: WallpaperId): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val uri = wallpaperId.value.toUri()
            val wallpaperManager = WallpaperManager.getInstance(context)

            // 1. Obtener dimensiones de la pantalla para no excedernos
            val metrics = context.resources.displayMetrics
            val reqWidth = metrics.widthPixels
            val reqHeight = metrics.heightPixels

            // 2. Primero leemos solo los metadatos de la imagen (sin cargarla a RAM)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            // 3. Calculamos cuánto debemos reducirla
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false // Ahora sí queremos los píxeles

            // 4. Decodificamos el Bitmap optimizado y lo aplicamos
            context.contentResolver.openInputStream(uri)?.use { input ->
                val bitmap = BitmapFactory.decodeStream(input, null, options)
                bitmap?.let {
                    wallpaperManager.setBitmap(it)
                } ?: throw Exception("El bitmap decodificado es nulo")
            } ?: throw Exception("No se pudo abrir el stream para la URI")
        }
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

