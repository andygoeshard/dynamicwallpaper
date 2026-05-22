package com.andyl.iris.domain.usecase.impl

import android.util.Log
import com.andyl.iris.data.imagesprovider.datasource.UnsplashRemoteDataSource
import com.andyl.iris.domain.model.PredefinedPack
import com.andyl.iris.domain.model.PredefinedRule
import com.andyl.iris.domain.model.PredefinedDailyRule
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

class InstallPredefinedPackUseCaseImpl(
    private val downloadUseCase: DownloadWallpaperUseCase,
    private val preferencesRepository: UserPreferencesRepository,
    private val applyUseCase: ApplyDynamicWallpaperUseCase,
    private val unsplashRemoteDataSource: UnsplashRemoteDataSource
) : InstallPredefinedPackUseCase {

    override suspend fun invoke(pack: PredefinedPack): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("IRIS_INSTALL", "Installing: ${pack.name} (Type: ${pack.type})")
            
            val weatherRulesToDownload = mutableListOf<PredefinedRule>()
            val dailyRulesToDownload = mutableListOf<PredefinedDailyRule>()

            if (pack.isRandom && pack.randomQuery != null) {
                val randomImages = unsplashRemoteDataSource.getRandomPhotos(pack.randomQuery, count = 30).getOrNull()
                if (!randomImages.isNullOrEmpty()) {
                    if (pack.type == PackType.WEEKLY) {
                        // Assign random images to days
                        val days = listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
                        days.forEachIndexed { index, day ->
                            val url = randomImages.getOrNull(index)?.urls?.full ?: ""
                            val optimizedUrl = if (url.contains("?")) "$url&ar=9:16&fit=crop" else "$url?ar=9:16&fit=crop"
                            dailyRulesToDownload.add(PredefinedDailyRule(day, optimizedUrl))
                        }
                    } else {
                        // Assign random images to weather/time
                        var imgIndex = 0
                        Weather.all().forEach { w ->
                            TimeOfDay.entries.forEach { t ->
                                val url = randomImages.getOrNull(imgIndex % randomImages.size)?.urls?.full ?: ""
                                val optimizedUrl = if (url.contains("?")) "$url&ar=9:16&fit=crop" else "$url?ar=9:16&fit=crop"
                                weatherRulesToDownload.add(PredefinedRule(w, t, optimizedUrl))
                                imgIndex++
                            }
                        }
                    }
                } else {
                    return@withContext Result.failure(Exception("Failed to fetch images from Unsplash"))
                }
            } else {
                weatherRulesToDownload.addAll(pack.weatherRules)
                dailyRulesToDownload.addAll(pack.dailyRules)
            }

            // Parallel Downloads
            val weatherDeferred = weatherRulesToDownload.map { rule ->
                async {
                    val weatherName = rule.weather.javaClass.simpleName ?: "Clear"
                    val fileName = "iris_${pack.id}_${weatherName}_${rule.timeOfDay.name}_${System.currentTimeMillis()}"
                    val file = downloadUseCase.execute(rule.imageUrl, fileName)
                    if (file != null) {
                        WallpaperRule(rule.weather, rule.timeOfDay, WallpaperId(file.absolutePath))
                    } else null
                }
            }

            val dailyDeferred = dailyRulesToDownload.map { rule ->
                async {
                    val fileName = "iris_${pack.id}_daily_${rule.dayName}_${System.currentTimeMillis()}"
                    val file = downloadUseCase.execute(rule.imageUrl, fileName)
                    if (file != null) {
                        rule.dayName to file.absolutePath
                    } else null
                }
            }

            val localWeatherRules = weatherDeferred.awaitAll().filterNotNull()
            val localDailyRules = dailyDeferred.awaitAll().filterNotNull().toMap()

            if (localWeatherRules.isEmpty() && localDailyRules.isEmpty()) {
                return@withContext Result.failure(Exception("Download failed"))
            }

            val newId = "pack_${pack.id}_${System.currentTimeMillis()}"
            val config = WallpaperConfig(
                id = newId,
                name = pack.name,
                rules = localWeatherRules,
                dailyRules = localDailyRules,
                activePackId = newId,
                scaleMode = ScaleMode.CROP
            )

            preferencesRepository.setWallpaperConfig(config)
            preferencesRepository.setActivePackId(newId)
            applyUseCase(newId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("IRIS_INSTALL", "Install failed", e)
            Result.failure(e)
        }
    }
}
