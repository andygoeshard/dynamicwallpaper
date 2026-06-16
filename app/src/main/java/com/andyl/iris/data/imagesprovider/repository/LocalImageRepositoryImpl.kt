package com.andyl.iris.data.imagesprovider.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.andyl.iris.domain.model.ImageResult
import com.andyl.iris.domain.repository.LocalImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalImageRepositoryImpl(private val context: Context) : LocalImageRepository {
    override suspend fun getLocalImages(): List<ImageResult> = withContext(Dispatchers.IO) {
        val images = mutableListOf<ImageResult>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                ).toString()
                
                images.add(
                    ImageResult(
                        id = id.toString(),
                        urlSmall = contentUri,
                        urlFull = contentUri,
                        provider = "local",
                        alt = "Local Image"
                    )
                )
            }
        }
        images
    }
}
