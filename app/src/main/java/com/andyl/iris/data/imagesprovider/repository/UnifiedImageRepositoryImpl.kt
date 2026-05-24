package com.andyl.iris.data.imagesprovider.repository

import android.util.Log
import com.andyl.iris.data.database.dao.ImageCacheDao
import com.andyl.iris.data.database.entity.CachedImage
import com.andyl.iris.data.imagesprovider.datasource.PexelsRemoteDataSource
import com.andyl.iris.data.imagesprovider.datasource.UnsplashRemoteDataSource
import com.andyl.iris.domain.model.ImageResult
import com.andyl.iris.domain.repository.ImageRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.TimeUnit

class UnifiedImageRepositoryImpl(
    private val unsplashDataSource: UnsplashRemoteDataSource,
    private val pexelsDataSource: PexelsRemoteDataSource,
    private val cacheDao: ImageCacheDao
) : ImageRepository {

    private val CACHE_EXPIRY = TimeUnit.DAYS.toMillis(15)

    override suspend fun searchImages(query: String, forceRefresh: Boolean): Result<List<ImageResult>> {
        val cleanQuery = query.lowercase().trim()
        if (cleanQuery.isEmpty()) return Result.success(emptyList())

        // 1. SEMANTIC LOCAL SEARCH (Broadened threshold)
        val localResults = cacheDao.predictImages(cleanQuery)
        
        // Satisfied only if we have a healthy pool of unique images
        if (!forceRefresh && localResults.size >= 40) {
            Log.d("IRIS_ROBUST", "🎯 SEMANTIC HIT: Found ${localResults.size} matches for '$cleanQuery'")
            return Result.success(localResults.map { it.toDomain() })
        }

        Log.w("IRIS_ROBUST", "📡 API REQUEST: Fetching fresh results for '$cleanQuery'")
        
        return coroutineScope {
            val uDef = async { unsplashDataSource.searchPhotos(cleanQuery).getOrNull()?.results ?: emptyList() }
            val pDef = async { pexelsDataSource.searchPhotos(cleanQuery).getOrNull()?.photos ?: emptyList() }

            val uRes = uDef.await()
            val pRes = pDef.await()
            
            val combined = mutableListOf<ImageResult>()
            uRes.forEach { combined.add(it.toDomain()) }
            pRes.forEach { combined.add(it.toDomain()) }

            if (combined.isNotEmpty()) {
                cacheDao.insertImages(combined.map { it.toEntity(cleanQuery) })
            }
            
            // Mix local with new for maximum variety
            val finalResult = (combined + localResults.map { it.toDomain() }).distinctBy { it.id }.shuffled()
            Result.success(finalResult)
        }
    }

    override suspend fun getRandomImages(query: String, count: Int): Result<List<ImageResult>> {
        // Broaden the search by returning more results than requested to ensure variety in caller
        return searchImages(query, forceRefresh = false)
    }

    private fun com.andyl.iris.data.imagesprovider.dto.UnsplashImage.toDomain() = ImageResult(
        id = "u_$id",
        urlSmall = urls.small,
        urlFull = urls.full,
        provider = "unsplash",
        alt = alt_description
    )

    private fun com.andyl.iris.data.imagesprovider.dto.PexelsPhoto.toDomain() = ImageResult(
        id = "p_$id",
        urlSmall = src.medium,
        urlFull = src.portrait,
        provider = "pexels",
        alt = alt
    )

    private fun CachedImage.toDomain() = ImageResult(
        id = imageId,
        urlSmall = imageUrlSmall,
        urlFull = imageUrlFull,
        provider = provider,
        alt = description
    )

    private fun ImageResult.toEntity(query: String) = CachedImage(
        query = query,
        provider = provider,
        imageUrlSmall = urlSmall,
        imageUrlFull = urlFull,
        imageId = id,
        description = alt
    )
}
