package com.andyl.iris.domain.usecase.impl

import android.util.Log
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
import com.andyl.iris.domain.repository.ImageRepository
import com.andyl.iris.domain.usecase.contract.ApplyDynamicWallpaperUseCase
import com.andyl.iris.domain.usecase.contract.InstallPredefinedPackUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

import com.andyl.iris.domain.helper.AlarmHelper
import android.content.Context

class InstallPredefinedPackUseCaseImpl(
    private val context: Context,
    private val downloadUseCase: DownloadWallpaperUseCase,
    private val preferencesRepository: UserPreferencesRepository,
    private val applyUseCase: ApplyDynamicWallpaperUseCase,
    private val imageRepository: ImageRepository
) : InstallPredefinedPackUseCase {

    override suspend fun invoke(pack: PredefinedPack, targetPackId: String?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("IRIS_INSTALL", "🚀 Installing high-variety pack: ${pack.name} onto target: ${targetPackId ?: "NEW"}")
            
            val weatherRulesToDownload = mutableListOf<PredefinedRule>()
            val dailyRulesToDownload = mutableListOf<PredefinedDailyRule>()
            val fixedRulesToDownload = mutableListOf<PredefinedFixedTimeRule>()

            val baseQuery = if (pack.isFullRandom) "" else pack.categoryQuery
            val usedImageIds = java.util.Collections.synchronizedSet(mutableSetOf<String>())

            when {
                // 1. TIME-BASED PACKS
                pack.isTimeBased -> {
                    TimeOfDay.entries.map { timeOfDay ->
                        async {
                            val q = buildCombinedQuery(baseQuery, timeOfDay.queryTerm)
                            imageRepository.searchImages(q).onSuccess { images ->
                                val image = images.find { it.id !in usedImageIds } ?: images.shuffled().firstOrNull()
                                image?.let {
                                    usedImageIds.add(it.id)
                                    val timeStr = when(timeOfDay) {
                                        TimeOfDay.DAWN -> "06:00"
                                        TimeOfDay.DAY -> "10:00"
                                        TimeOfDay.DUSK -> "18:00"
                                        TimeOfDay.NIGHT -> "22:00"
                                    }
                                    fixedRulesToDownload.add(PredefinedFixedTimeRule(timeStr, it.urlFull))
                                }
                            }
                        }
                    }.awaitAll()
                }

                // 2. WEEKLY PACKS
                pack.type == PackType.WEEKLY -> {
                    imageRepository.getRandomImages(baseQuery, count = 50).onSuccess { images ->
                        val shuffledImages = images.filter { it.id !in usedImageIds }.shuffled()
                        val days = listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
                        days.forEachIndexed { i, day ->
                            val image = shuffledImages.getOrNull(i) ?: images.getOrNull(i)
                            image?.let {
                                usedImageIds.add(it.id)
                                dailyRulesToDownload.add(PredefinedDailyRule(day, it.urlFull))
                            }
                        }
                    }
                }

                // 3. WEATHER PACKS
                else -> {
                    val weatherPairs = Weather.all().flatMap { w -> TimeOfDay.entries.map { t -> w to t } }
                    weatherPairs.map { (weather, time) ->
                        async {
                            val q = buildWeatherTimeQuery(baseQuery, weather, time)
                            imageRepository.searchImages(q).onSuccess { pool ->
                                val image = pool.filter { it.id !in usedImageIds }.shuffled().firstOrNull() ?: pool.shuffled().firstOrNull()
                                image?.let {
                                    usedImageIds.add(it.id)
                                    weatherRulesToDownload.add(PredefinedRule(weather, time, it.urlFull))
                                }
                            }
                        }
                    }.awaitAll()
                }
            }

            // --- DOWNLOAD ---
            val allUrls = (weatherRulesToDownload.map { it.imageUrl } + 
                          dailyRulesToDownload.map { it.imageUrl } + 
                          fixedRulesToDownload.map { it.imageUrl }).distinct()

            if (allUrls.isEmpty()) return@withContext Result.failure(Exception("No images found"))

            val downloadedFiles = allUrls.map { url ->
                async {
                    val fileName = "iris_pack_${pack.id}_${url.hashCode()}"
                    val file = downloadUseCase.execute(url, fileName)
                    url to file?.absolutePath
                }
            }.awaitAll().toMap()

            // --- SMART MERGE LOGIC ---
            val existingConfig = targetPackId?.let { preferencesRepository.getWallpaperConfig(it) }
            
            val newWeatherRules = weatherRulesToDownload.mapNotNull { rule ->
                downloadedFiles[rule.imageUrl]?.let { path -> WallpaperRule(rule.weather, rule.timeOfDay, WallpaperId(path)) }
            }
            val newDailyRules = dailyRulesToDownload.mapNotNull { rule ->
                downloadedFiles[rule.imageUrl]?.let { path -> rule.dayName to path }
            }.toMap()
            val newFixedRules = fixedRulesToDownload.mapNotNull { rule ->
                downloadedFiles[rule.imageUrl]?.let { path -> rule.time to path }
            }.toMap()

            val finalConfig = if (existingConfig != null) {
                // Update existing: overwrite categories provided by the new pack, keep others
                existingConfig.copy(
                    rules = if (newWeatherRules.isNotEmpty()) newWeatherRules else existingConfig.rules,
                    dailyRules = existingConfig.dailyRules + newDailyRules,
                    fixedTimeRules = existingConfig.fixedTimeRules + newFixedRules
                )
            } else {
                // Create new
                val newId = "predefined_${pack.id}_${System.currentTimeMillis()}"
                WallpaperConfig(
                    id = newId,
                    name = pack.name,
                    rules = newWeatherRules,
                    dailyRules = newDailyRules,
                    fixedTimeRules = newFixedRules,
                    activePackId = newId,
                    scaleMode = ScaleMode.CROP
                )
            }

            preferencesRepository.setWallpaperConfig(finalConfig)
            preferencesRepository.setActivePackId(finalConfig.id)
            applyUseCase(finalConfig.id)

            finalConfig.fixedTimeRules.keys.forEach { time ->
                AlarmHelper.scheduleFixedTimeAlarm(context, time)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("IRIS_INSTALL", "Install failed", e)
            Result.failure(e)
        }
    }

    private fun buildCombinedQuery(base: String, term: String) = if (base.isEmpty()) term else "$base $term"
    private fun buildWeatherTimeQuery(base: String, weather: Weather, time: TimeOfDay) = 
        if (base.isEmpty()) "${weather.queryTerm} ${time.queryTerm}" else "$base ${weather.queryTerm} ${time.queryTerm}"
}
