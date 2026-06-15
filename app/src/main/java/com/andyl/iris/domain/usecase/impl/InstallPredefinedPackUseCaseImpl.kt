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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

import com.andyl.iris.domain.helper.AlarmHelper
import android.content.Context
import com.andyl.iris.domain.model.ImageResult

class InstallPredefinedPackUseCaseImpl(
    private val context: Context,
    private val downloadUseCase: DownloadWallpaperUseCase,
    private val preferencesRepository: UserPreferencesRepository,
    private val applyUseCase: ApplyDynamicWallpaperUseCase,
    private val imageRepository: ImageRepository
) : InstallPredefinedPackUseCase {

    private val mutex = Mutex()

    override suspend fun invoke(pack: PredefinedPack, targetPackId: String?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("IRIS_VAR", "==================================================")
            Log.d("IRIS_VAR", "🚀 VARIETY ENGINE: Installing '${pack.name}'")
            
            val weatherRulesToDownload = mutableListOf<PredefinedRule>()
            val dailyRulesToDownload = mutableListOf<PredefinedDailyRule>()
            val fixedRulesToDownload = mutableListOf<PredefinedFixedTimeRule>()

            val baseQuery = if (pack.isFullRandom) "wallpaper 4k high resolution" else pack.categoryQuery
            
            // Uniqueness tracking
            val usedIds = mutableSetOf<String>()
            val usedSignatures = mutableSetOf<String>() // To catch same images with different IDs
            
            // 1. FETCH A MASSIVE MASTER POOL (100+ images)
            Log.d("IRIS_VAR", "Building master pool for: $baseQuery")
            // We fetch once with forceRefresh to get a fresh variety, but then we rely on this pool
            val masterPoolResult = imageRepository.searchImages(baseQuery, forceRefresh = true)
            val masterPool = masterPoolResult.getOrNull()?.shuffled() ?: emptyList()
            var poolIdx = 0
            Log.d("IRIS_VAR", "Master pool size: ${masterPool.size}")

            suspend fun getUniqueImage(query: String, slotLabel: String): ImageResult? {
                // We try a specific search for the slot to ensure MATCHING theme (Night, Rainy, etc)
                val specific = imageRepository.searchImages(query).getOrNull() ?: emptyList()
                
                return mutex.withLock {
                    // 1. Try to find a unique image in the specific search results
                    val foundInSpecific = specific.shuffled().find { img ->
                        val sig = img.urlFull.substringBefore("?").takeLast(50)
                        img.id !in usedIds && sig !in usedSignatures
                    }
                    
                    val finalChoice = if (foundInSpecific != null) {
                        Log.d("IRIS_VAR", "  ✅ Slot [$slotLabel]: Found specific matching image (${foundInSpecific.id})")
                        foundInSpecific
                    } else {
                        // 2. Fallback to master pool ONLY if specific search fails or is exhausted
                        // to ensure we ALWAYS have an image for the slot, even if not a perfect match
                        var fallback: ImageResult? = null
                        while (poolIdx < masterPool.size) {
                            val candidate = masterPool[poolIdx++]
                            val sig = candidate.urlFull.substringBefore("?").takeLast(50)
                            if (candidate.id !in usedIds && sig !in usedSignatures) {
                                Log.d("IRIS_VAR", "  ♻️ Slot [$slotLabel]: Using pool fallback (${candidate.id})")
                                fallback = candidate
                                break
                            }
                        }
                        fallback
                    }
                    
                    finalChoice?.let {
                        usedIds.add(it.id)
                        usedSignatures.add(it.urlFull.substringBefore("?").takeLast(50))
                    }
                    finalChoice
                }
            }

            when {
                pack.isTimeBased -> {
                    TimeOfDay.entries.forEach { tod ->
                        getUniqueImage(buildCombinedQuery(baseQuery, tod.queryTerm), tod.name)?.let { img ->
                            val timeStr = when(tod) {
                                TimeOfDay.DAWN -> "06:00"
                                TimeOfDay.DAY -> "10:00"
                                TimeOfDay.DUSK -> "18:00"
                                TimeOfDay.NIGHT -> "22:00"
                            }
                            fixedRulesToDownload.add(PredefinedFixedTimeRule(timeStr, img.urlFull))
                        }
                    }
                }

                pack.type == PackType.WEEKLY -> {
                    listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday").forEach { day ->
                        getUniqueImage(buildCombinedQuery(baseQuery, day), day)?.let { img ->
                            dailyRulesToDownload.add(PredefinedDailyRule(day, img.urlFull))
                        }
                    }
                }

                else -> {
                    val weatherPairs = Weather.all().flatMap { w -> TimeOfDay.entries.map { t -> w to t } }
                    weatherPairs.forEach { (w, t) ->
                        val label = "${w.queryTerm}-${t.name}"
                        getUniqueImage(buildWeatherTimeQuery(baseQuery, w, t), label)?.let { img ->
                            weatherRulesToDownload.add(PredefinedRule(w, t, img.urlFull))
                        }
                    }
                }
            }

            // --- DOWNLOAD ---
            val allToGet = (weatherRulesToDownload.map { it.imageUrl } + 
                            dailyRulesToDownload.map { it.imageUrl } + 
                            fixedRulesToDownload.map { it.imageUrl }).distinct()

            if (allToGet.isEmpty()) return@withContext Result.failure(Exception("Failed to acquire unique images"))

            val downloadedFiles = allToGet.map { url ->
                async {
                    val fileName = "iris_v_${url.hashCode()}"
                    val file = downloadUseCase.execute(url, fileName)
                    url to file?.absolutePath
                }
            }.awaitAll().toMap()

            // --- SAVE ---
            val existing = targetPackId?.let { preferencesRepository.getWallpaperConfig(it) }
            val newWRules = weatherRulesToDownload.mapNotNull { r -> downloadedFiles[r.imageUrl]?.let { p -> WallpaperRule(r.weather, r.timeOfDay, WallpaperId(p)) } }
            val newDRules = dailyRulesToDownload.mapNotNull { r -> downloadedFiles[r.imageUrl]?.let { p -> r.dayName to p } }.toMap()
            val newFRules = fixedRulesToDownload.mapNotNull { r -> downloadedFiles[r.imageUrl]?.let { p -> r.time to p } }.toMap()

            val finalConfig = if (existing != null) {
                existing.copy(
                    rules = if (newWRules.isNotEmpty()) newWRules else existing.rules,
                    dailyRules = existing.dailyRules + newDRules,
                    fixedTimeRules = existing.fixedTimeRules + newFRules
                )
            } else {
                val newId = "pack_${pack.id}_${System.currentTimeMillis()}"
                WallpaperConfig(
                    id = newId,
                    name = pack.name,
                    updateIntervalMinutes = 15,
                    rules = newWRules,
                    dailyRules = newDRules,
                    fixedTimeRules = newFRules,
                    enabledWeathers = Weather.all().toSet(),
                    activePackId = newId,
                    scaleMode = ScaleMode.CROP
                )
            }

            preferencesRepository.setWallpaperConfig(finalConfig)
            preferencesRepository.setActivePackId(finalConfig.id)
            applyUseCase(finalConfig.id)

            finalConfig.fixedTimeRules.keys.forEach { AlarmHelper.scheduleFixedTimeAlarm(context, it) }
            
            Log.d("IRIS_VAR", "✅ SUCCESS: Installed ${usedIds.size} unique images from 3 providers.")
            Log.d("IRIS_VAR", "==================================================")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("IRIS_VAR", "❌ VARIETY ENGINE CRASHED", e)
            Result.failure(e)
        }
    }

    private fun buildCombinedQuery(base: String, term: String) = if (base.isEmpty()) term else "$base $term"
    private fun buildWeatherTimeQuery(base: String, weather: Weather, time: TimeOfDay) = 
        if (base.isEmpty()) "${weather.queryTerm} ${time.queryTerm}" else "$base ${weather.queryTerm} ${time.queryTerm}"
}
