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
import com.andyl.iris.domain.model.ImageResult
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

    override suspend fun invoke(pack: PredefinedPack): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("IRIS_INSTALL", "🚀 Starting High-Variety Multi-Provider Install: ${pack.name}")
            
            val weatherRulesToDownload = mutableListOf<PredefinedRule>()
            val dailyRulesToDownload = mutableListOf<PredefinedDailyRule>()
            val fixedRulesToDownload = mutableListOf<PredefinedFixedTimeRule>()

            val baseQuery = if (pack.isFullRandom) "" else pack.categoryQuery
            val usedImageIds = java.util.Collections.synchronizedSet(mutableSetOf<String>())

            when {
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

                pack.type == PackType.WEEKLY -> {
                    imageRepository.getRandomImages(baseQuery, count = 30).onSuccess { images ->
                        val shuffledImages = images.shuffled()
                        val days = listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
                        days.forEachIndexed { i, day ->
                            val image = shuffledImages.getOrNull(i)
                            image?.let {
                                dailyRulesToDownload.add(PredefinedDailyRule(day, it.urlFull))
                            }
                        }
                    }
                }

                else -> {
                    Weather.all().map { weather ->
                        async {
                            val q = buildWeatherQuery(baseQuery, weather)
                            imageRepository.getRandomImages(q, count = 15).onSuccess { pool ->
                                TimeOfDay.entries.forEach { time ->
                                    val image = pool.filter { it.id !in usedImageIds }.shuffled().firstOrNull() ?: pool.shuffled().firstOrNull()
                                    image?.let {
                                        usedImageIds.add(it.id)
                                        weatherRulesToDownload.add(PredefinedRule(weather, time, it.urlFull))
                                    }
                                }
                            }
                        }
                    }.awaitAll()
                }
            }

            val allUrls = (weatherRulesToDownload.map { it.imageUrl } + 
                          dailyRulesToDownload.map { it.imageUrl } + 
                          fixedRulesToDownload.map { it.imageUrl }).distinct()

            if (allUrls.isEmpty()) return@withContext Result.failure(Exception("No images found"))

            val downloadedFiles = allUrls.map { url ->
                async {
                    val file = downloadUseCase.execute(url, "iris_pack_${pack.id}_${url.hashCode()}")
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

            localFixedRules.keys.forEach { time -> AlarmHelper.scheduleFixedTimeAlarm(context, time) }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("IRIS_INSTALL", "💥 Install failed", e)
            Result.failure(e)
        }
    }

    private fun buildCombinedQuery(base: String, term: String) = if (base.isEmpty()) term else "$base $term"
    private fun buildWeatherQuery(base: String, weather: Weather) = if (base.isEmpty()) weather.queryTerm else "$base ${weather.queryTerm}"
}
