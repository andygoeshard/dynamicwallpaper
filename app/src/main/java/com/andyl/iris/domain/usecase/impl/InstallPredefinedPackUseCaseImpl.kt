package com.andyl.iris.domain.usecase.impl

import android.util.Log
import com.andyl.iris.data.imagesprovider.datasource.UnsplashRemoteDataSource
import com.andyl.iris.domain.model.PredefinedPack
import com.andyl.iris.domain.model.PredefinedRule
import com.andyl.iris.domain.model.PredefinedDailyRule
import com.andyl.iris.domain.model.PredefinedFixedTimeRule
import com.andyl.iris.domain.model.PackType
import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.model.WallpaperConfig
import com.andyl.iris.domain.model.WallpaperId
import com.andyl.iris.domain.model.WallpaperRule
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.domain.model.ScaleMode
import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.domain.usecase.contract.ApplyDynamicWallpaperUseCase
import com.andyl.iris.domain.usecase.contract.InstallPredefinedPackUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

import com.andyl.iris.domain.helper.AlarmHelper
import android.content.Context
import com.andyl.iris.data.imagesprovider.dto.UnsplashImage

class InstallPredefinedPackUseCaseImpl(
    private val context: Context,
    private val downloadUseCase: DownloadWallpaperUseCase,
    private val preferencesRepository: UserPreferencesRepository,
    private val applyUseCase: ApplyDynamicWallpaperUseCase,
    private val unsplashRemoteDataSource: UnsplashRemoteDataSource
) : InstallPredefinedPackUseCase {

    override suspend fun invoke(pack: PredefinedPack): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("IRIS_INSTALL", "🚀 Starting installation of pack: ${pack.name}")
            
            val weatherRulesToDownload = mutableListOf<PredefinedRule>()
            val dailyRulesToDownload = mutableListOf<PredefinedDailyRule>()
            val fixedRulesToDownload = mutableListOf<PredefinedFixedTimeRule>()

            val baseQuery = if (pack.isFullRandom) "" else pack.categoryQuery
            val usedImageIds = java.util.Collections.synchronizedSet(mutableSetOf<String>())

            when {
                // 1. TIME-BASED OVERRIDE PACKS
                pack.isTimeBased -> {
                    val timeMapping = mapOf(
                        "06:00" to TimeOfDay.DAWN,
                        "10:00" to TimeOfDay.DAY,
                        "18:00" to TimeOfDay.DUSK,
                        "22:00" to TimeOfDay.NIGHT
                    )
                    
                    timeMapping.map { (time, timeOfDay) ->
                        async {
                            val searchQuery = buildCombinedQuery(baseQuery, timeOfDay.queryTerm)
                            unsplashRemoteDataSource.getRandomPhotos(searchQuery, count = 10)
                                .onSuccess { images ->
                                    val image = images.find { it.id !in usedImageIds } ?: images.firstOrNull()
                                    image?.let {
                                        usedImageIds.add(it.id)
                                        fixedRulesToDownload.add(PredefinedFixedTimeRule(time, "${it.urls.full}&ar=9:16&fit=crop"))
                                    }
                                }
                        }
                    }.awaitAll()
                }

                // 2. WEEKLY PACKS
                pack.type == PackType.WEEKLY -> {
                    val days = listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
                    days.map { day ->
                        async {
                            val searchQuery = buildCombinedQuery(baseQuery, day)
                            unsplashRemoteDataSource.getRandomPhotos(searchQuery, count = 10)
                                .onSuccess { images ->
                                    val image = images.find { it.id !in usedImageIds } ?: images.firstOrNull()
                                    image?.let {
                                        usedImageIds.add(it.id)
                                        dailyRulesToDownload.add(PredefinedDailyRule(day, "${it.urls.full}&ar=9:16&fit=crop"))
                                    }
                                }
                        }
                    }.awaitAll()
                }

                // 3. WEATHER PACKS
                else -> {
                    val weatherPairs = Weather.all().flatMap { w -> TimeOfDay.entries.map { t -> w to t } }
                    val queryResultsCache = mutableMapOf<String, List<UnsplashImage>>()

                    // Fetch in batches to avoid rate limit issues if possible, but keep it async for speed
                    val uniqueQueries = weatherPairs.map { (w, t) -> buildWeatherQuery(baseQuery, w, t) }.distinct()
                    
                    uniqueQueries.map { q ->
                        async {
                            unsplashRemoteDataSource.getRandomPhotos(q, count = 15)
                                .onSuccess { images -> 
                                    synchronized(queryResultsCache) { queryResultsCache[q] = images }
                                }
                                .onFailure { Log.e("IRIS_INSTALL", "Failed to fetch images for query: $q", it) }
                        }
                    }.awaitAll()

                    weatherPairs.forEach { (weather, time) ->
                        val q = buildWeatherQuery(baseQuery, weather, time)
                        val pool = queryResultsCache[q] ?: emptyList()
                        
                        // Try to find a unique image
                        val image = pool.filter { it.id !in usedImageIds }.shuffled().firstOrNull() 
                            ?: pool.shuffled().firstOrNull()
                            
                        if (image != null) {
                            usedImageIds.add(image.id)
                            weatherRulesToDownload.add(PredefinedRule(weather, time, "${image.urls.full}&ar=9:16&fit=crop"))
                            Log.d("IRIS_CONSISTENCY", "✅ Assigned ${image.id} to ${weather.queryTerm}-${time.queryTerm}")
                        } else {
                            Log.e("IRIS_CONSISTENCY", "❌ No images found for ${weather.queryTerm}-${time.queryTerm} using query: $q")
                        }
                    }
                }
            }

            // --- DOWNLOAD & INSTALL ---
            val allUrls = (weatherRulesToDownload.map { it.imageUrl } + 
                          dailyRulesToDownload.map { it.imageUrl } + 
                          fixedRulesToDownload.map { it.imageUrl }).distinct()

            if (allUrls.isEmpty()) {
                Log.e("IRIS_INSTALL", "No URLs to download. Installation aborted.")
                return@withContext Result.failure(Exception("No images found for this pack."))
            }

            Log.d("IRIS_INSTALL", "Downloading ${allUrls.size} unique images...")

            val downloadedFiles = allUrls.map { url ->
                async {
                    val fileName = "iris_pack_${pack.id}_${url.hashCode()}"
                    val file = downloadUseCase.execute(url, fileName)
                    url to file?.absolutePath
                }
            }.awaitAll().toMap()

            val localWeatherRules = weatherRulesToDownload.mapNotNull { rule ->
                downloadedFiles[rule.imageUrl]?.let { path -> WallpaperRule(rule.weather, rule.timeOfDay, WallpaperId(path)) }
            }

            val localDailyRules = dailyRulesToDownload.mapNotNull { rule ->
                downloadedFiles[rule.imageUrl]?.let { path -> rule.dayName to path }
            }.toMap()
            
            val localFixedRules = fixedRulesToDownload.mapNotNull { rule ->
                downloadedFiles[rule.imageUrl]?.let { path -> rule.time to path }
            }.toMap()

            val config = WallpaperConfig(
                id = "predefined_${pack.id}_${System.currentTimeMillis()}",
                name = pack.name,
                rules = localWeatherRules,
                dailyRules = localDailyRules,
                fixedTimeRules = localFixedRules,
                activePackId = pack.id,
                scaleMode = ScaleMode.CROP
            )

            preferencesRepository.setWallpaperConfig(config)
            preferencesRepository.setActivePackId(config.id)
            applyUseCase(config.id)

            localFixedRules.keys.forEach { time ->
                AlarmHelper.scheduleFixedTimeAlarm(context, time)
            }
            
            Log.d("IRIS_INSTALL", "✅ Pack installed successfully: ${pack.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("IRIS_INSTALL", "💥 Critical failure during install", e)
            Result.failure(e)
        }
    }

    private fun buildCombinedQuery(base: String, term: String): String {
        return if (base.isEmpty()) term else "$base $term"
    }

    private fun buildWeatherQuery(base: String, weather: Weather, time: TimeOfDay): String {
        // Broaden the search by using only base and weather if base is specific enough
        return if (base.isEmpty()) {
            "${weather.queryTerm} ${time.queryTerm}"
        } else {
            "$base ${weather.queryTerm} ${time.queryTerm}"
        }
    }
}
