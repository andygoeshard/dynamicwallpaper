package com.andyl.iris.domain.model

data class PredefinedPack(
    val id: String,
    val name: String,
    val description: String,
    val previewUrl: String,
    val categoryQuery: String, 
    val type: PackType = PackType.WEATHER,
    val isTimeBased: Boolean = false,
    val isFullRandom: Boolean = false
)

enum class PackType {
    WEATHER, WEEKLY
}

data class PredefinedRule(
    val weather: Weather,
    val timeOfDay: TimeOfDay,
    val imageUrl: String
)

data class PredefinedDailyRule(
    val dayName: String,
    val imageUrl: String
)

data class PredefinedFixedTimeRule(
    val time: String, // HH:mm
    val imageUrl: String
)

object PredefinedPacks {
    val packs = listOf(
        // --- WEATHER BASED PACKS (3 + 1 Random) ---
        PredefinedPack(
            id = "nature_weather",
            name = "Cinematic Landscapes",
            description = "Breathtaking nature views that evolve with weather conditions.",
            previewUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "nature landscapes",
            type = PackType.WEATHER
        ),
        PredefinedPack(
            id = "urban_weather",
            name = "Neon Symphony",
            description = "The vibrant soul of the city, from neon nights to rainy days.",
            previewUrl = "https://images.unsplash.com/photo-1514565131-fce0801e5785?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "urban cyberpunk city",
            type = PackType.WEATHER
        ),
        PredefinedPack(
            id = "anime_weather",
            name = "Ghibli Scenery",
            description = "Hand-painted style landscapes inspired by legendary anime films.",
            previewUrl = "https://images.unsplash.com/photo-1542273917363-3b1817f69a2d?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "ghibli scenery anime",
            type = PackType.WEATHER
        ),
        PredefinedPack(
            id = "random_weather_discovery",
            name = "Weather Discovery",
            description = "Complete variety. A different high-quality landscape for every condition.",
            previewUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "wallpaper",
            type = PackType.WEATHER,
            isFullRandom = true
        ),

        // --- WEEKLY BASED PACKS (3 + 1 Random) ---
        PredefinedPack(
            id = "world_tour_weekly",
            name = "Global Journey",
            description = "Travel to a different iconic world capital every day.",
            previewUrl = "https://images.unsplash.com/photo-1467269204594-9661b134dd2b?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "city architecture travel",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "wildlife_weekly",
            name = "Wild Sanctuary",
            description = "A majestic animal encounter for each day of the week.",
            previewUrl = "https://images.unsplash.com/photo-1546182990-dffeafbe841d?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "wildlife animals nature",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "cosmic_weekly",
            name = "Cosmic Voyage",
            description = "Explore the vast mysteries of deep space every day.",
            previewUrl = "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "galaxy nebula space cosmos",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "random_weekly_surprise",
            name = "Weekly Surprise",
            description = "A unique set of random high-quality wallpapers for your week.",
            previewUrl = "https://images.unsplash.com/photo-1493612276216-ee3925520721?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "nature abstract",
            type = PackType.WEEKLY,
            isFullRandom = true
        ),

        // --- TIME-BASED OVERRIDE PACKS (3 + 1 Random) ---
        PredefinedPack(
            id = "day_night_cycle",
            name = "Day & Night Cycle",
            description = "Strict time-based overrides to perfectly match the clock.",
            previewUrl = "https://images.unsplash.com/photo-1467810563316-b5476525c0f9?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "sky landscape",
            type = PackType.WEATHER,
            isTimeBased = true
        ),
        PredefinedPack(
            id = "celestial_colors",
            name = "Celestial Colors",
            description = "Purely clock-driven sky shifts, ignoring weather for consistency.",
            previewUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "minimalist sky gradient",
            type = PackType.WEATHER,
            isTimeBased = true
        ),
        PredefinedPack(
            id = "minimal_flow",
            name = "Minimalist Flow",
            description = "Calm and simple aesthetic shifts that follow your daily routine.",
            previewUrl = "https://images.unsplash.com/photo-1494438639946-1ebd1d20bf85?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "minimalist abstract",
            type = PackType.WEATHER,
            isTimeBased = true
        ),
        PredefinedPack(
            id = "random_time_discovery",
            name = "Time Discovery",
            description = "Surprise yourself with random art that changes throughout the day.",
            previewUrl = "https://images.unsplash.com/photo-1518531933037-91b2f5f229cc?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "abstract art",
            type = PackType.WEATHER,
            isTimeBased = true,
            isFullRandom = true
        )
    )
}
