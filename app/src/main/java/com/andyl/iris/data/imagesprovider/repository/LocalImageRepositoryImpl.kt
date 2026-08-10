package com.andyl.iris.data.imagesprovider.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.andyl.iris.domain.model.ImageResult
import com.andyl.iris.domain.repository.LocalImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalImageRepositoryImpl(private val context: Context) : LocalImageRepository {
    override suspend fun getLocalImages(): List<ImageResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ImageResult>()

        // --- IMAGES ---
        runCatching {
            val imageProjection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME
            )
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageProjection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()
                    results.add(
                        ImageResult(
                            id = "img_$id",
                            urlSmall = contentUri,
                            urlFull = contentUri,
                            provider = "local",
                            alt = "Local Image"
                        )
                    )
                }
            }
        }.onFailure { it.printStackTrace() }

        // --- VIDEOS (gallery) ---
        runCatching {
            val videoProjection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME
            )
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoProjection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()
                    results.add(
                        ImageResult(
                            id = "vid_$id",
                            urlSmall = contentUri,
                            urlFull = contentUri,
                            provider = "localvideo",
                            alt = cursor.getString(nameColumn),
                            isVideo = true
                        )
                    )
                }
            }
        }.onFailure { it.printStackTrace() }

        // --- VIDEOS (downloaded / included in app storage) ---
        val dir = File(context.filesDir, "live_video")
        if (dir.exists()) {
            dir.walkTopDown()
                .filter { it.isFile && it.extension.equals("mp4", ignoreCase = true) }
                .forEach { file ->
                    results.add(
                        ImageResult(
                            id = "file_${file.absolutePath}",
                            urlSmall = file.absolutePath,
                            urlFull = file.absolutePath,
                            provider = "localvideo",
                            alt = file.name,
                            isVideo = true
                        )
                    )
                }
        }

        results
    }
}
