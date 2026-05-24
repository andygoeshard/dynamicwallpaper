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
            Log.d("IRIS_INSTALL", "Installing High-Consistency Pack: ${pack.name}")
            
            val weatherRulesToDownload = mutableListOf<PredefinedRule>()
            val dailyRulesToDownload = mutableListOf<PredefinedDailyRule>()
            val fixedRulesToDownload = mutableListOf<PredefinedFixedTimeRule>()

            val baseQuery = if (pack.isFullRandom) "" else pack.categoryQuery
            val usedImageIds = mutableSetOf<String>()

            when {
                // 1. TIME-BASED OVERRIDE PACKS
                pack.isTimeBased -> {
                    val timeMapping = mapOf(
                        "06:00" to TimeOfDay.DAWN,
                        "10:00" to TimeOfDay.DAY,
                        "18:00" to TimeOfDay.DUSK,
                        "22:00" to TimeOfDay.NIGHT
                    )
                    
                    val results = timeMapping.map { (time, timeOfDay) ->
                        async {
                            val searchQuery = if (baseQuery.isEmpty()) timeOfDay.queryTerm else "$baseQuery ${timeOfDay.queryTerm}"
                            // Fetch a small pool to ensure uniqueness across slots
                            val images = unsplashRemoteDataSource.getRandomPhotos(searchQuery, count = 3).getOrNull() ?: emptyList()
                            time to images
                        }
                    }.awaitAll().toMap()

                    timeMapping.keys.forEach { time ->
                        val pool = results[time] ?: emptyList()
                        val image = pool.find { it.id !in usedImageIds } ?: pool.firstOrNull()
                        image?.let {
                            usedImageIds.add(it.id)
                            fixedRulesToDownload.add(PredefinedFixedTimeRule(time, "${it.urls.full}&ar=9:16&fit=crop"))
                        }
                    }
                }

                // 2. WEEKLY PACKS
                pack.type == PackType.WEEKLY -> {
                    val days = listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
                    val results = days.map { day ->
                        async {
                            val searchQuery = if (baseQuery.isEmpty()) day else "$baseQuery $day"
                            val images = unsplashRemoteDataSource.getRandomPhotos(searchQuery, count = 3).getOrNull() ?: emptyList()
                            day to images
                        }
                    }.awaitAll().toMap()
                    
                    days.forEach { day ->
                        val pool = results[day] ?: emptyList()
                        val image = pool.find { it.id !in usedImageIds } ?: pool.firstOrNull()
                        image?.let {
                            usedImageIds.add(it.id)
                            dailyRulesToDownload.add(PredefinedDailyRule(day, "${it.urls.full}&ar=9:16&fit=crop"))
                        }
                    }
                }

                // 3. WEATHER PACKS
                else -> {
                    val weatherPairs = Weather.all().flatMap { w -> TimeOfDay.entries.map { t -> w to t } }
                    val queryResultsCache = mutableMapOf<String, kotlinx.coroutines.Deferred<List<UnsplashImage>>>()

                    // Pre-fetch pools for each unique query
                    weatherPairs.forEach { (weather, time) ->
                        val searchQuery = buildWeatherQuery(baseQuery, weather, time)
                        if (!queryResultsCache.containsKey(searchQuery)) {
                            queryResultsCache[searchQuery] = async {
                                unsplashRemoteDataSource.getRandomPhotos(searchQuery, count = 5).getOrNull() ?: emptyList()
                            }
                        }
                    }

                    weatherPairs.forEach { (weather, time) ->
                        val searchQuery = buildWeatherQuery(baseQuery, weather, time)
                        val pool = queryResultsCache[searchQuery]?.await() ?: emptyList()
                        
                        val image = pool.find { it.id !in usedImageIds } ?: pool.firstOrNull()
                        image?.let {
                            usedImageIds.add(it.id)
                            weatherRulesToDownload.add(PredefinedRule(weather, time, "${it.urls.full}&ar=9:16&fit=crop"))
                        }
                    }
                }
            }

            // --- DOWNLOAD & INSTALL ---
            val allUrls = (weatherRulesToDownload.map { it.imageUrl } + 
                          dailyRulesToDownload.map { it.imageUrl } + 
                          fixedRulesToDownload.map { it.imageUrl }).distinct()

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

            if (localWeatherRules.isEmpty() && localDailyRules.isEmpty() && localFixedRules.isEmpty()) {
                return@withContext Result.failure(Exception("Installation failed: No images found."))
            }

            val newId = "predefined_${pack.id}_${System.currentTimeMillis()}"
            val config = WallpaperConfig(
                id = newId,
                name = pack.name,
                rules = localWeatherRules,
                dailyRules = localDailyRules,
                fixedTimeRules = localFixedRules,
                activePackId = newId,
                scaleMode = ScaleMode.CROP
            )

            preferencesRepository.setWallpaperConfig(config)
            preferencesRepository.setActivePackId(newId)
            applyUseCase(newId)

            localFixedRules.keys.forEach { time ->
                AlarmHelper.scheduleFixedTimeAlarm(context, time)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("IRIS_INSTALL", "Install failed", e)
            Result.failure(e)
        }
    }

    private fun buildWeatherQuery(base: String, weather: Weather, time: TimeOfDay): String {
        val weatherTerm = weather.queryTerm
        val timeTerm = time.queryTerm
        return if (base.isEmpty()) "$weatherTerm $timeTerm" else "$base $weatherTerm $timeTerm"
    }
}
