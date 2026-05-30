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
    val weatherPacks = listOf(
        PredefinedPack(
            id = "nature_weather",
            name = "Cinematic Landscapes",
            description = "Nature views that evolve with weather.",
            previewUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "nature landscapes"
        ),
        PredefinedPack(
            id = "urban_weather",
            name = "Neon Symphony",
            description = "Vibrant city soul, from neon to rain.",
            previewUrl = "https://images.unsplash.com/photo-1514565131-fce0801e5785?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "urban cyberpunk city"
        ),
        PredefinedPack(
            id = "anime_weather",
            name = "Anime Vibe",
            description = "Hand-painted style landscapes.",
            previewUrl = "https://images.unsplash.com/photo-1542273917363-3b1817f69a2d?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "anime landscape art"
        ),
        PredefinedPack(
            id = "minimal_weather",
            name = "Structural Silence",
            description = "Minimalist architecture and clean lines.",
            previewUrl = "https://images.pexels.com/photos/262367/pexels-photo-262367.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "minimalist architecture"
        ),
        PredefinedPack(
            id = "waifu_weather",
            name = "Ethereal Waifus",
            description = "Anime heroines that adapt to the sky.",
            previewUrl = "https://images.pexels.com/photos/15942493/pexels-photo-15942493.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "anime girl art background"
        ),
        PredefinedPack(
            id = "urban_muse",
            name = "Urban Muse",
            description = "Portraits that mirror the local mood.",
            previewUrl = "https://images.pexels.com/photos/157675/fashion-men-s-fashion-suit-steampunk-157675.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "girl portrait model fashion"
        ),
        PredefinedPack(
            id = "river_monumental",
            name = "River: Monumental",
            description = "The passion of El Más Grande.",
            previewUrl = "https://images.unsplash.com/photo-1599148564010-09886a048a1c?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "River Plate stadium Argentina football"
        ),
        PredefinedPack(
            id = "gamer_sanctum",
            name = "Gamer Sanctum",
            description = "High-tech setups for your screen.",
            previewUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "gaming setup neon rgb"
        ),
        PredefinedPack(
            id = "enchanted_forest",
            name = "Enchanted Forest",
            description = "Fairy-tale woods in every condition.",
            previewUrl = "https://images.pexels.com/photos/1179229/pexels-photo-1179229.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "fantasy forest magical"
        ),
        PredefinedPack(
            id = "cyber_cosplay",
            name = "Neon Cosplay",
            description = "Stunning cyberpunk characters.",
            previewUrl = "https://images.pexels.com/photos/3642302/pexels-photo-3642302.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "cyberpunk cosplay girl"
        )
    )

    val weeklyPacks = listOf(
        PredefinedPack(
            id = "cozy_weekly",
            name = "Cozy Retreat",
            description = "Warm interiors for every day.",
            previewUrl = "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cozy interior cabin aesthetic",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "galaxy_weekly",
            name = "Galactic Odyssey",
            description = "Deep mysteries of space every day.",
            previewUrl = "https://images.pexels.com/photos/1103970/pexels-photo-1103970.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "galaxy nebula space",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "animals_weekly",
            name = "Animal Kingdom",
            description = "Majestic wildlife to start your day.",
            previewUrl = "https://images.unsplash.com/photo-1543946207-39bd91e70ca7?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "wildlife animals nature",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "travel_weekly",
            name = "World Explorer",
            description = "Iconic landmarks from across the globe.",
            previewUrl = "https://images.pexels.com/photos/1008155/pexels-photo-1008155.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "travel landmarks city",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "otaku_journey",
            name = "Otaku Journey",
            description = "A unique heroine for every day.",
            previewUrl = "https://images.unsplash.com/photo-1613373123746-10b1a4725b7b?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "anime girl illustration manga",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "river_weekly",
            name = "River Passion",
            description = "Daily pride for the Millionaire.",
            previewUrl = "https://images.unsplash.com/photo-1614728263952-84ea256f9679?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "River Plate fans football",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "cosplay_spotlight",
            name = "Cosplay Weekly",
            description = "Best characters brought to life.",
            previewUrl = "https://images.pexels.com/photos/1390530/pexels-photo-1390530.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "cosplay character girl",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "gaming_legends",
            name = "Gaming Legends",
            description = "Iconic heroes from your favorite games.",
            previewUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "video game character wallpaper",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "aesthetic_girls",
            name = "Aesthetic Flow",
            description = "Trendy fashion and style vibes.",
            previewUrl = "https://images.pexels.com/photos/1926769/pexels-photo-1926769.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "aesthetic girl style fashion",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "mythical_creatures",
            name = "Mythical World",
            description = "Dragons and magic for your week.",
            previewUrl = "https://images.unsplash.com/photo-1618336753974-aae8e04506aa?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "dragon mythical fantasy creature",
            type = PackType.WEEKLY
        )
    )

    val timePacks = listOf(
        PredefinedPack(
            id = "day_night_cycle",
            name = "Atmospheric Sky",
            description = "Sky shifts that follow the sun.",
            previewUrl = "https://images.unsplash.com/photo-1467810563316-b5476525c0f9?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "sky landscape",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "digital_canvas",
            name = "Digital Canvas",
            description = "Abstract gradients that shift.",
            previewUrl = "https://images.pexels.com/photos/2832382/pexels-photo-2832382.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "abstract gradient art",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "micro_world",
            name = "Micro World",
            description = "Stunning macro photography.",
            previewUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "macro nature texture",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "pixel_art_time",
            name = "8-Bit Daybreak",
            description = "Charming pixel art landscapes.",
            previewUrl = "https://images.pexels.com/photos/1205301/pexels-photo-1205301.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "pixel art landscape 8bit",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "retro_wave",
            name = "Retro Wave",
            description = "Synthwave retro-futurism.",
            previewUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "vaporwave synthwave retro aesthetic",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "waifu_emotions",
            name = "Waifu Moods",
            description = "Anime girls matching your day.",
            previewUrl = "https://images.pexels.com/photos/15942493/pexels-photo-15942493.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "anime girl art",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "lofi_beats",
            name = "Lofi Vibes",
            description = "Chill study and relax scenes.",
            previewUrl = "https://images.unsplash.com/photo-1516280440614-37939bbdd4f1?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "lofi aesthetic chill",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "cyber_city_cycle",
            name = "Cyber City 2077",
            description = "Futuristic city day/night cycle.",
            previewUrl = "https://images.pexels.com/photos/2387418/pexels-photo-2387418.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "futuristic city cyberpunk night",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "river_history",
            name = "River Glory",
            description = "The history of the club on your screen.",
            previewUrl = "https://images.unsplash.com/photo-1599148564010-09886a048a1c?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "River Plate football club",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "minimal_gradient_time",
            name = "Pure Gradients",
            description = "Smooth color transitions.",
            previewUrl = "https://images.pexels.com/photos/1242348/pexels-photo-1242348.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "minimalist gradient background",
            isTimeBased = true
        )
    )

    val randomPacks = listOf(
        PredefinedPack(
            id = "random_discovery",
            name = "Discovery Mix",
            description = "A complete variety of photos.",
            previewUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "wallpaper",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "waifu_surprise",
            name = "Waifu Surprise",
            description = "Random anime girl art.",
            previewUrl = "https://images.pexels.com/photos/15942493/pexels-photo-15942493.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "anime girl art illustration",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "gamer_gear_random",
            name = "Gamer Gear",
            description = "Random hardware and setups.",
            previewUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "gaming hardware neon",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "nature_best_random",
            name = "Nature's Best",
            description = "Random breathtaking landscapes.",
            previewUrl = "https://images.pexels.com/photos/3225517/pexels-photo-3225517.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "nature wallpaper 4k",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "urban_street_random",
            name = "Urban Street",
            description = "Random city life photography.",
            previewUrl = "https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "city street photography",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "cosplay_fantasy_random",
            name = "Cosplay Fantasy",
            description = "Random amazing cosplays.",
            previewUrl = "https://images.pexels.com/photos/1390530/pexels-photo-1390530.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "fantasy cosplay girl",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "minimal_art_random",
            name = "Minimal Art",
            description = "Random clean aesthetics.",
            previewUrl = "https://images.unsplash.com/photo-1494438639946-1ebd1d20bf85?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "minimalist art aesthetic",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "everything_river_random",
            name = "Everything River",
            description = "Random River Plate content.",
            previewUrl = "https://images.unsplash.com/photo-1614728263952-84ea256f9679?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "River Plate Argentina",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "abstract_dreams_random",
            name = "Abstract Dreams",
            description = "Random digital creations.",
            previewUrl = "https://images.pexels.com/photos/2832382/pexels-photo-2832382.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "abstract digital art",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "infinite_variety_random",
            name = "Infinite Mix",
            description = "Literally anything good.",
            previewUrl = "https://images.unsplash.com/photo-1493612276216-ee3925520721?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "wallpaper amazing high resolution",
            isFullRandom = true
        )
    )

    val packs = weatherPacks + weeklyPacks + timePacks + randomPacks
}
