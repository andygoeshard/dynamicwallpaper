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

    private val CACHE_EXPIRY = TimeUnit.DAYS.toMillis(15) // Cacheamos por 15 días para máximo ahorro

    override suspend fun searchImages(query: String, forceRefresh: Boolean): Result<List<ImageResult>> {
        val cleanQuery = query.lowercase().trim()
        if (cleanQuery.isEmpty()) return Result.success(emptyList())

        // 1. BUSQUEDA SEMÁNTICA LOCAL (Impacto 0 si ya hay datos similares)
        val localResults = cacheDao.predictImages(cleanQuery)
        
        // Si tenemos suficientes resultados locales (ej. 15), evitamos la API por completo
        if (!forceRefresh && localResults.size >= 15) {
            Log.d("IRIS_ROBUST", "🚀 ZERO API IMPACT: Found ${localResults.size} semantic matches for '$cleanQuery'")
            return Result.success(localResults.map { it.toDomain() })
        }

        // 2. SOLO SI ES NECESARIO, CONSUMIMOS CUOTA
        Log.w("IRIS_ROBUST", "📡 API REQUEST: Insufficient local data for '$cleanQuery'. Fetching...")
        
        return coroutineScope {
            val uDef = async { unsplashDataSource.searchPhotos(cleanQuery).getOrNull()?.results ?: emptyList() }
            val pDef = async { pexelsDataSource.searchPhotos(cleanQuery).getOrNull()?.photos ?: emptyList() }

            val uRes = uDef.await()
            val pRes = pDef.await()
            
            // Guardamos con descripción para futuras búsquedas semánticas
            val combined = mutableListOf<ImageResult>()
            uRes.forEach { combined.add(it.toDomain()) }
            pRes.forEach { combined.add(it.toDomain()) }

            if (combined.isNotEmpty()) {
                cacheDao.insertImages(combined.map { it.toEntity(cleanQuery) })
            }
            
            // Mezclamos local + nuevo para dar la mejor experiencia
            val finalResult = (combined + localResults.map { it.toDomain() }).distinctBy { it.id }.shuffled()
            Result.success(finalResult)
        }
    }

    override suspend fun getRandomImages(query: String, count: Int): Result<List<ImageResult>> {
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
        urlFull = src.original,
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
