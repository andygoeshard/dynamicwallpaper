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
            categoryQuery = "anime landscape art",
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

        // --- WEEKLY BASED PACKS ---
        PredefinedPack(
            id = "cozy_weekly",
            name = "Cozy Retreat",
            description = "Warm interiors and peaceful cabin vibes for every day.",
            previewUrl = "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cozy interior cabin aesthetic",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "neon_weekly",
            name = "Neon Dreams",
            description = "Vibrant neon lights and glowing city nights.",
            previewUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "neon lights night city",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "cyberpunk_weekly",
            name = "Cyberpunk Future",
            description = "High-tech, low-life aesthetic from a futuristic world.",
            previewUrl = "https://images.unsplash.com/photo-1515630278258-407f66498911?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cyberpunk city street futuristic",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "animals_weekly",
            name = "Animal Kingdom",
            description = "Beautiful wildlife and adorable animals to start your day.",
            previewUrl = "https://images.unsplash.com/photo-1474511320721-9a6ee3ef716d?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "wildlife animals nature",
            type = PackType.WEEKLY
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
