package com.andyl.iris.data.imagesprovider.repository

import android.util.Log
import com.andyl.iris.data.database.dao.ImageCacheDao
import com.andyl.iris.data.database.entity.CachedImage
import com.andyl.iris.data.imagesprovider.datasource.PexelsRemoteDataSource
import com.andyl.iris.data.imagesprovider.datasource.PixabayRemoteDataSource
import com.andyl.iris.data.imagesprovider.datasource.UnsplashRemoteDataSource
import com.andyl.iris.domain.model.ImageResult
import com.andyl.iris.domain.repository.ImageRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.TimeUnit

class UnifiedImageRepositoryImpl(
    private val unsplashDataSource: UnsplashRemoteDataSource,
    private val pexelsDataSource: PexelsRemoteDataSource,
    private val pixabayDataSource: PixabayRemoteDataSource,
    private val cacheDao: ImageCacheDao
) : ImageRepository {

    private val CACHE_EXPIRY = TimeUnit.DAYS.toMillis(15)

    override suspend fun searchImages(query: String, forceRefresh: Boolean): Result<List<ImageResult>> {
        val cleanQuery = query.lowercase().trim()
        if (cleanQuery.isEmpty()) return Result.success(emptyList())

        // 1. SEMANTIC LOCAL SEARCH
        val localResults = cacheDao.predictImages(cleanQuery)
        
        if (!forceRefresh && localResults.size >= 40) {
            Log.d("IRIS_ROBUST", "🎯 SEMANTIC HIT: Found ${localResults.size} matches for '$cleanQuery'")
            return Result.success(localResults.map { it.toDomain() })
        }

        Log.w("IRIS_ROBUST", "📡 API REQUEST: Fetching fresh results for '$cleanQuery'")
        
        return coroutineScope {
            val uDef = async { unsplashDataSource.searchPhotos(cleanQuery).getOrNull()?.results?.map { it.toDomain() } ?: emptyList() }
            val pDef = async { pexelsDataSource.searchPhotos(cleanQuery).getOrNull()?.photos?.map { it.toDomain() } ?: emptyList() }
            val pixDef = async { pixabayDataSource.searchPhotos(cleanQuery).getOrNull()?.hits?.map { it.toDomain() } ?: emptyList() }

            val uRes = uDef.await()
            val pRes = pDef.await()
            val pixRes = pixDef.await()
            
            // Interleave results from all 3 providers
            val combined = interleave(uRes, pRes, pixRes)

            if (combined.isNotEmpty()) {
                cacheDao.insertImages(combined.map { it.toEntity(cleanQuery) })
            }
            
            // Mix local with new for maximum variety
            val finalResult = (combined + localResults.map { it.toDomain() }).distinctBy { it.id }.shuffled()
            Result.success(finalResult)
        }
    }

    override suspend fun getRandomImages(query: String, count: Int): Result<List<ImageResult>> {
        return searchImages(query, forceRefresh = false)
    }

    private fun interleave(vararg lists: List<ImageResult>): List<ImageResult> {
        val result = mutableListOf<ImageResult>()
        val iterators = lists.map { it.iterator() }
        var hasMore = true
        while (hasMore) {
            hasMore = false
            for (it in iterators) {
                if (it.hasNext()) {
                    result.add(it.next())
                    hasMore = true
                }
            }
        }
        return result
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

    private fun com.andyl.iris.data.imagesprovider.dto.PixabayHit.toDomain() = ImageResult(
        id = "pix_$id",
        urlSmall = webformatURL,
        urlFull = largeImageURL,
        provider = "pixabay",
        alt = tags
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
