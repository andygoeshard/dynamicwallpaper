package com.andyl.iris.domain.usecase.impl

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class DownloadWallpaperUseCase(private val client: HttpClient, private val context: Context) {

    suspend fun execute(url: String, fileName: String? = null): Result<File> = withContext(Dispatchers.IO) {
        try {
            Log.d("DownloadWallpaper", "Downloading from: $url")
            val response: HttpResponse = client.get(url)
            
            if (response.status.value !in 200..299) {
                Log.e("DownloadWallpaper", "Failed to download: ${response.status}")
                return@withContext Result.failure(Exception("Failed to download: ${response.status}"))
            }
            
            val bytes = response.readBytes()

            val folder = File(context.filesDir, "wallpapers")
            if (!folder.exists()) {
                val created = folder.mkdirs()
                Log.d("DownloadWallpaper", "Folder created: $created")
            }

            val actualFileName = fileName ?: "iris_${UUID.randomUUID()}"
            val file = File(folder, "$actualFileName.jpg")
            
            file.writeBytes(bytes)
            
            // También guardamos en la galería para que sea accesible desde el selector de archivos del sistema
            saveToGallery(bytes, "$actualFileName.jpg")
            
            Log.d("DownloadWallpaper", "File saved at: ${file.absolutePath}")
            Result.success(file)
        } catch (e: Exception) {
            Log.e("DownloadWallpaper", "Error downloading wallpaper", e)
            Result.failure(e)
        }
    }

    private fun saveToGallery(bytes: ByteArray, displayName: String) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Iris")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(bytes)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)
                }
            }
        } catch (e: Exception) {
            Log.e("DownloadWallpaper", "Error saving to gallery", e)
        }
    }
}
