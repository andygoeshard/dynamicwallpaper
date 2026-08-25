package com.andyl.iris

import com.andyl.iris.domain.model.ScaleMode
import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.model.WallpaperConfig
import com.andyl.iris.domain.model.WallpaperId
import com.andyl.iris.domain.model.WallpaperRule
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.domain.usecase.impl.ResolveWallpaperUseCaseImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveWallpaperUseCaseTest {

    private val useCase = ResolveWallpaperUseCaseImpl()

    private fun config(
        rules: List<WallpaperRule> = emptyList(),
        daily: Map<String, String> = emptyMap(),
        fixed: Map<String, String> = emptyMap(),
        temperature: Map<String, String> = emptyMap(),
        enabledWeathers: Set<Weather> = Weather.all()
    ) = WallpaperConfig(
        id = "1",
        name = "Test",
        rules = rules,
        dailyRules = daily,
        fixedTimeRules = fixed,
        temperatureRules = temperature,
        enabledWeathers = enabledWeathers,
        activePackId = "1",
        scaleMode = ScaleMode.CROP
    )

    @Test
    fun weatherMatchWinsWhenEnabled() = runBlocking {
        val rule = WallpaperRule(Weather.Rain, TimeOfDay.DAY, WallpaperId("rain-day"))
        val config = config(rules = listOf(rule), enabledWeathers = setOf(Weather.Rain))

        val result = useCase(Weather.Rain, TimeOfDay.DAY, config, temperature = null)

        assertEquals(1, result.size)
        assertEquals("rain-day", result.first().wallpaperId.value)
    }

    @Test
    fun disabledWeatherFallsBackToTimeMatch() = runBlocking {
        // Weather not enabled: the weather match is skipped, but the same rule
        // still matches via the time-of-day fallback.
        val rule = WallpaperRule(Weather.Rain, TimeOfDay.DAY, WallpaperId("rain-day"))
        val config = config(rules = listOf(rule), enabledWeathers = emptySet())

        val result = useCase(Weather.Rain, TimeOfDay.DAY, config, temperature = null)

        assertEquals(1, result.size)
        assertEquals("rain-day", result.first().wallpaperId.value)
    }

    @Test
    fun weatherMatchIsPreferredWhenEnabled() = runBlocking {
        // With the weather enabled, the exact weather+time match wins over the
        // time-only fallback: it should return the matching rule (same result
        // here, but exercising the primary path).
        val rule = WallpaperRule(Weather.Rain, TimeOfDay.DAY, WallpaperId("rain-day"))
        val config = config(rules = listOf(rule), enabledWeathers = setOf(Weather.Rain))

        val result = useCase(Weather.Rain, TimeOfDay.DAY, config, temperature = null)

        assertEquals("rain-day", result.first().wallpaperId.value)
    }

    @Test
    fun timeOfDayFallbackMatchesWithoutWeather() = runBlocking {
        val rule = WallpaperRule(Weather.Clear, TimeOfDay.NIGHT, WallpaperId("night"))
        val config = config(rules = listOf(rule), enabledWeathers = setOf(Weather.Rain))

        // Weather differs from the rule, so it should fall back to time-only match.
        val result = useCase(Weather.Storm, TimeOfDay.NIGHT, config, temperature = null)

        assertEquals(1, result.size)
        assertEquals("night", result.first().wallpaperId.value)
    }

    @Test
    fun anyValidRuleIsLastResort() = runBlocking {
        val rule = WallpaperRule(Weather.Clear, TimeOfDay.DAY, WallpaperId("fallback"))
        val config = config(rules = listOf(rule), enabledWeathers = emptySet())

        val result = useCase(Weather.Snow, TimeOfDay.NIGHT, config, temperature = null)

        assertEquals(1, result.size)
        assertEquals("fallback", result.first().wallpaperId.value)
    }

    @Test
    fun dailyRuleOverridesWeatherRules() = runBlocking {
        val today = java.time.LocalDate.now().dayOfWeek.name.lowercase()
        val weatherRule = WallpaperRule(Weather.Rain, TimeOfDay.DAY, WallpaperId("rain-day"))
        val config = config(
            rules = listOf(weatherRule),
            daily = mapOf(today to "daily-uri")
        )

        val result = useCase(Weather.Rain, TimeOfDay.DAY, config, temperature = null)

        assertEquals("daily-uri", result.first().wallpaperId.value)
    }

    @Test
    fun temperatureRuleMatchesRange() = runBlocking {
        val config = config(temperature = mapOf("WARM-DAY" to "temp-day"))

        val result = useCase(Weather.Clear, TimeOfDay.DAY, config, temperature = 25.0)

        assertEquals("temp-day", result.first().wallpaperId.value)
    }

    @Test
    fun emptyConfigReturnsEmpty() = runBlocking {
        val result = useCase(Weather.Clear, TimeOfDay.DAY, config(), temperature = null)
        assertTrue(result.isEmpty())
    }
}