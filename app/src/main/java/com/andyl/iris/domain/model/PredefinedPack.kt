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
        // --- WEATHER BASED PACKS (Unsplash + Pexels Mix) ---
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
            name = "Anime Vibe",
            description = "Hand-painted style landscapes inspired by legendary animated films.",
            previewUrl = "https://images.unsplash.com/photo-1542273917363-3b1817f69a2d?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "anime landscape art",
            type = PackType.WEATHER
        ),
        PredefinedPack(
            id = "minimal_weather",
            name = "Structural Silence",
            description = "Minimalist architecture and clean lines for a calm experience.",
            previewUrl = "https://images.pexels.com/photos/262367/pexels-photo-262367.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "minimalist architecture",
            type = PackType.WEATHER
        ),
        PredefinedPack(
            id = "waifu_weather",
            name = "Ethereal Waifus",
            description = "Anime heroines that adapt to the shifting weather and sky.",
            previewUrl = "https://images.pexels.com/photos/15942493/pexels-photo-15942493.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "anime girl art background",
            type = PackType.WEATHER
        ),
        PredefinedPack(
            id = "fcb_fem_weather",
            name = "Barça Femení: Blaugrana Sky",
            description = "The champions of everything, matching your local weather conditions.",
            previewUrl = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "FC Barcelona Femení football",
            type = PackType.WEATHER
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
            id = "galaxy_weekly",
            name = "Galactic Odyssey",
            description = "Explore the deep mysteries of space and nebulae every day.",
            previewUrl = "https://images.pexels.com/photos/1103970/pexels-photo-1103970.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "galaxy nebula space",
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
        PredefinedPack(
            id = "travel_weekly",
            name = "World Explorer",
            description = "Iconic landmarks and hidden gems from across the globe.",
            previewUrl = "https://images.pexels.com/photos/1008155/pexels-photo-1008155.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "travel landmarks city",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "fcb_fem_weekly",
            name = "Barça Femení: Daily Passion",
            description = "Celebrate the talent and glory of the world's best team every day.",
            previewUrl = "https://images.unsplash.com/photo-1522778119026-d647f0596c20?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "FC Barcelona Femení team",
            type = PackType.WEEKLY
        ),

        // --- TIME-BASED OVERRIDE PACKS ---
        PredefinedPack(
            id = "day_night_cycle",
            name = "Atmospheric Sky",
            description = "Strict time-based overrides to perfectly match the clock.",
            previewUrl = "https://images.unsplash.com/photo-1467810563316-b5476525c0f9?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "sky landscape",
            type = PackType.WEATHER,
            isTimeBased = true
        ),
        PredefinedPack(
            id = "abstract_flow",
            name = "Digital Canvas",
            description = "Abstract art and color gradients that shift through the day.",
            previewUrl = "https://images.pexels.com/photos/2832382/pexels-photo-2832382.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "abstract gradient art",
            type = PackType.WEATHER,
            isTimeBased = true
        ),
        PredefinedPack(
            id = "macro_pulse",
            name = "Micro World",
            description = "Stunning macro photography revealing hidden textures.",
            previewUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "macro nature texture",
            type = PackType.WEATHER,
            isTimeBased = true
        ),
        // --- NEW PEXELS FOCUSED & TRENDY PACKS ---
        PredefinedPack(
            id = "waifu_weekly",
            name = "Waifu Haven",
            description = "Anime-style character illustrations to accompany your week.",
            previewUrl = "https://images.pexels.com/photos/15942493/pexels-photo-15942493.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "anime girl illustration art",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "cyberpunk_streets_weekly",
            name = "Neon Syndicate",
            description = "Hardcore cyberpunk cityscapes and robotic aesthetics.",
            previewUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cyberpunk cyborg neon city",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "pixel_art_time",
            name = "8-Bit Daybreak",
            description = "Charming pixel art landscapes that follow the sun.",
            previewUrl = "https://images.pexels.com/photos/1205301/pexels-photo-1205301.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "pixel art landscape 8bit",
            type = PackType.WEATHER,
            isTimeBased = true
        ),
        PredefinedPack(
            id = "vaporwave_vibe_time",
            name = "Retro Wave",
            description = "Syntwave and 80s retro-futurism for your daily cycle.",
            previewUrl = "https://images.unsplash.com/photo-1614850523296-e8c041de4398?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "vaporwave synthwave retro aesthetic",
            type = PackType.WEATHER,
            isTimeBased = true
        ),
        PredefinedPack(
            id = "random_discovery",
            name = "Discovery Mix",
            description = "A complete variety of high-quality photos from all over.",
            previewUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "wallpaper",
            type = PackType.WEATHER,
            isFullRandom = true
        )
    )
}
