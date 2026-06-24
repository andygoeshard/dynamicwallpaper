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
            description = "Breathtaking nature views that evolve with weather conditions.",
            previewUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cinematic nature landscape scenery"
        ),
        PredefinedPack(
            id = "argentina_weather",
            name = "Selección Argentina",
            description = "La pasión de las tres estrellas en tu fondo de pantalla.",
            previewUrl = "https://images.unsplash.com/photo-1670417973059-009141071295?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "Argentina national football team Messi Scaloneta"
        ),
        PredefinedPack(
            id = "cats_weather",
            name = "Feline Grace",
            description = "Adorable cats and kittens matching the outdoor atmosphere.",
            previewUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cute cat kitten photography aesthetic"
        ),
        PredefinedPack(
            id = "urban_weather",
            name = "Neon Symphony",
            description = "The vibrant soul of the city, from neon nights to rainy days.",
            previewUrl = "https://images.unsplash.com/photo-1514565131-fce0801e5785?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cyberpunk city urban neon night"
        ),
        PredefinedPack(
            id = "anime_weather",
            name = "Anime Vibe",
            description = "Hand-painted style landscapes inspired by legendary animated films.",
            previewUrl = "https://images.unsplash.com/photo-1542273917363-3b1817f69a2d?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "anime style landscape art scenery"
        ),
        PredefinedPack(
            id = "minimal_weather",
            name = "Structural Silence",
            description = "Minimalist architecture and clean lines for a calm experience.",
            previewUrl = "https://images.pexels.com/photos/262367/pexels-photo-262367.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "minimalist architecture clean lines modern"
        ),
        PredefinedPack(
            id = "women_empowered",
            name = "Empowered Women",
            description = "Strong and confident portraits mirroring the local weather mood.",
            previewUrl = "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "strong confident woman portrait fashion"
        ),
        PredefinedPack(
            id = "cyber_samurai",
            name = "Cyber Samurai",
            description = "The fusion of ancient honor and future tech in every storm.",
            previewUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cyberpunk samurai neon katana"
        ),
        PredefinedPack(
            id = "gamer_sanctum",
            name = "Gamer Sanctum",
            description = "High-tech setups that evolve with your day.",
            previewUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "gaming setup neon rgb room"
        ),
        PredefinedPack(
            id = "enchanted_forest",
            name = "Enchanted Forest",
            description = "Fairy-tale woods that change with the elements.",
            previewUrl = "https://images.pexels.com/photos/1179229/pexels-photo-1179229.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "fantasy magical forest mystery"
        ),
        PredefinedPack(
            id = "lofi_study_waifu",
            name = "Lofi Study",
            description = "Chill anime girls studying in a room that reflects the sky outside.",
            previewUrl = "https://images.unsplash.com/photo-1516280440614-37939bbdd4f1?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "lofi anime girl study room window rain"
        ),
        PredefinedPack(
            id = "space_nebula_weather",
            name = "Deep Space",
            description = "Cosmic nebulae and stars that follow the earthly weather.",
            previewUrl = "https://images.pexels.com/photos/1103970/pexels-photo-1103970.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "galaxy nebula space cosmos deep"
        ),
        // --- ADDED PACKS TO REACH 30 ---
        PredefinedPack(
            id = "nordic_legends",
            name = "Nordic Legends",
            description = "Viking warriors and cold mythical landscapes.",
            previewUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "viking warrior nordic fantasy snow"
        ),
        PredefinedPack(
            id = "tropical_escape",
            name = "Tropical Escape",
            description = "Crystal clear waters and golden sands for your screen.",
            previewUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "tropical beach paradise ocean sunny"
        ),
        PredefinedPack(
            id = "mountain_majesty",
            name = "Mountain Majesty",
            description = "Highest peaks and snowy summits from around the globe.",
            previewUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "mountain peaks snowy summit clouds"
        ),
        PredefinedPack(
            id = "abstract_flow",
            name = "Abstract Flow",
            description = "Mesmerizing fluid art and colorful abstract waves.",
            previewUrl = "https://images.unsplash.com/photo-1541701494587-cb58502866ab?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "abstract fluid art colorful liquid"
        ),
        PredefinedPack(
            id = "street_fashion",
            name = "Street Fashion",
            description = "Urban style and trendy outfits in the middle of the city.",
            previewUrl = "https://images.unsplash.com/photo-1529139513065-07b2ee0a9ec9?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "urban street fashion style model"
        ),
        PredefinedPack(
            id = "wild_safari",
            name = "Wild Safari",
            description = "Majestic lions, elephants and tigers in their natural habitat.",
            previewUrl = "https://images.unsplash.com/photo-1546182990-dffeafbe841d?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "wildlife safari animals lion tiger"
        ),
        PredefinedPack(
            id = "dark_fantasy",
            name = "Dark Fantasy",
            description = "Gothic castles, dark magic and mysterious realms.",
            previewUrl = "https://images.unsplash.com/photo-1613373123746-10b1a4725b7b?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "dark fantasy gothic magic castle"
        ),
        PredefinedPack(
            id = "steampunk_world",
            name = "Steampunk World",
            description = "Gears, goggles and industrial Victorian aesthetics.",
            previewUrl = "https://images.unsplash.com/photo-1514565131-fce0801e5785?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "steampunk victorian gears machinery"
        ),
        PredefinedPack(
            id = "vaporwave_sunset",
            name = "Vaporwave Sunset",
            description = "Retro 80s colors and neon palms in a digital sky.",
            previewUrl = "https://images.unsplash.com/photo-1614850523296-e8c041de4398?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "vaporwave aesthetic neon retro sunset"
        ),
        PredefinedPack(
            id = "underwater_wonders",
            name = "Underwater Wonders",
            description = "Vibrant coral reefs and majestic sea creatures.",
            previewUrl = "https://images.unsplash.com/photo-1583244532610-2ca22e111d4b?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "underwater coral reef fish sea ocean"
        ),
        PredefinedPack(
            id = "epic_castles",
            name = "Epic Castles",
            description = "Grand medieval citadels and forgotten ruins.",
            previewUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "medieval castle fortress ruins fantasy"
        ),
        PredefinedPack(
            id = "mecha_force",
            name = "Mecha Force",
            description = "Giant robots and futuristic war machines.",
            previewUrl = "https://images.pexels.com/photos/2599244/pexels-photo-2599244.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "mecha robot giant futuristic sci-fi"
        ),
        PredefinedPack(
            id = "desert_mirage",
            name = "Desert Mirage",
            description = "Endless golden dunes and star-filled desert nights.",
            previewUrl = "https://images.unsplash.com/photo-1473580044384-7ba9967e16a0?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "desert sand dunes sahara sunny"
        ),
        PredefinedPack(
            id = "rain_mood",
            name = "Rainy Mood",
            description = "Melancholic and cozy rain on windows and city streets.",
            previewUrl = "https://images.unsplash.com/photo-1515694346937-94d85e41e6f0?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "rain window street moody wet"
        ),
        PredefinedPack(
            id = "macro_world",
            name = "Macro World",
            description = "Incredible close-ups of flowers, insects and textures.",
            previewUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "macro photography nature insects flowers"
        ),
        PredefinedPack(
            id = "minimalist_calm",
            name = "Minimalist Calm",
            description = "Simple landscapes and clean compositions for focus.",
            previewUrl = "https://images.unsplash.com/photo-1494438639946-1ebd1d20bf85?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "minimalist landscape simple nature clean"
        ),
        PredefinedPack(
            id = "digital_art",
            name = "Digital Art",
            description = "Stunning digital illustrations and creative paintings.",
            previewUrl = "https://images.unsplash.com/photo-1558591710-4b4a1ae0f04d?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "digital art illustration painting creative"
        ),
        PredefinedPack(
            id = "cosmic_girls",
            name = "Cosmic Girls",
            description = "Anime heroines wandering through the starry void.",
            previewUrl = "https://images.unsplash.com/photo-1614728263952-84ea256f9679?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "anime girl space galaxy stars fantasy"
        ),
        PredefinedPack(
            id = "cyberpunk_women",
            name = "Cyberpunk Vanguards",
            description = "Futuristic women in a world of neon and high-tech shadows.",
            previewUrl = "https://images.unsplash.com/photo-1605810230434-7631ac76ec81?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cyberpunk woman neon futuristic city"
        )
    )

    val weeklyPacks = listOf(
        PredefinedPack(
            id = "argentina_weekly",
            name = "Pasión Albiceleste",
            description = "Un recuerdo glorioso de la selección para cada día de la semana.",
            previewUrl = "https://images.unsplash.com/photo-1671370242203-b186ca1b5597?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "Argentina national football team Qatar 2022 champions",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "kids_weekly",
            name = "Childhood Wonders",
            description = "Pure joy and innocence to brighten your daily routine.",
            previewUrl = "https://images.unsplash.com/photo-1471286174890-9c112ffca5b4?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "happy children playing laughing nature",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "street_art_weekly",
            name = "Urban Canvas",
            description = "Amazing street art and graffiti from around the world.",
            previewUrl = "https://images.unsplash.com/photo-1499750310107-5fef28a66643?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "street art graffiti urban wall photography",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "pixel_rpg_weekly",
            name = "Pixel Quest",
            description = "Retro RPG landscapes and characters for a nostalgic week.",
            previewUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "pixel art rpg landscape fantasy 8bit",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "cozy_weekly",
            name = "Cozy Retreat",
            description = "Warm interiors and peaceful cabin vibes for every day.",
            previewUrl = "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cozy interior cabin warm aesthetic",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "classic_cars_weekly",
            name = "Vintage Rides",
            description = "Iconic classic cars and automotive beauty every day.",
            previewUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "classic car vintage automobile photography",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "flowers_weekly",
            name = "Bloom & Flow",
            description = "Stunning floral photography to keep your week fresh.",
            previewUrl = "https://images.unsplash.com/photo-1490750967868-88aa4486c946?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "flowers bloom floral nature aesthetic",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "animals_weekly",
            name = "Animal Kingdom",
            description = "Beautiful wildlife and adorable animals to start your day.",
            previewUrl = "https://images.unsplash.com/photo-1543946207-39bd91e70ca7?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "wildlife animals nature photography",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "travel_weekly",
            name = "World Explorer",
            description = "Iconic landmarks and hidden gems from across the globe.",
            previewUrl = "https://images.pexels.com/photos/1008155/pexels-photo-1008155.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "travel landmarks city world architecture",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "otaku_journey",
            name = "Otaku Journey",
            description = "A unique anime-style heroine for every day of your week.",
            previewUrl = "https://images.unsplash.com/photo-1613373123746-10b1a4725b7b?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "anime girl character illustration",
            type = PackType.WEEKLY
        )
    )

    val timePacks = listOf(
        PredefinedPack(
            id = "argentina_glory",
            name = "Selección: Gloria Eterna",
            description = "Los momentos más emocionantes de la albiceleste a través del tiempo.",
            previewUrl = "https://images.unsplash.com/photo-1671370242203-b186ca1b5597?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "Argentina national team trophy celebration Messi",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "day_night_cycle",
            name = "Atmospheric Sky",
            description = "Strict time-based overrides to perfectly match the clock.",
            previewUrl = "https://images.unsplash.com/photo-1467810563316-b5476525c0f9?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "atmospheric sky landscape horizon",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "digital_canvas",
            name = "Digital Canvas",
            description = "Abstract art and color gradients that shift through the day.",
            previewUrl = "https://images.pexels.com/photos/2832382/pexels-photo-2832382.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "abstract gradient digital art",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "cyber_city_cycle",
            name = "Cyber City 2077",
            description = "A futuristic city day/night cycle for your screen.",
            previewUrl = "https://images.pexels.com/photos/2387418/pexels-photo-2387418.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "futuristic city cyberpunk neon 4k",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "lofi_beats",
            name = "Lofi Vibes",
            description = "Chill study and relax scenes for your routine.",
            previewUrl = "https://images.unsplash.com/photo-1516280440614-37939bbdd4f1?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "lofi aesthetic chill study room",
            isTimeBased = true
        )
    )

    val randomPacks = listOf(
        PredefinedPack(
            id = "seleccion_argentina_random",
            name = "Argentina Mix",
            description = "Un popurrí de la selección, desde jugadores hasta hinchada.",
            previewUrl = "https://images.unsplash.com/photo-1670359740263-1f19069634e9?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "Argentina national football team fans stadium",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "cats_random",
            name = "Random Cats",
            description = "A never-ending stream of adorable feline friends.",
            previewUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cute cat kitten photography",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "random_discovery",
            name = "Discovery Mix",
            description = "A complete variety of high-quality photos.",
            previewUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "wallpaper 4k photography",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "waifu_surprise",
            name = "Waifu Surprise",
            description = "Random anime girl art for your screen.",
            previewUrl = "https://images.pexels.com/photos/15942493/pexels-photo-15942493.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "anime girl art illustration",
            isFullRandom = true
        )
    )

    val packs = weatherPacks + weeklyPacks + timePacks + randomPacks
}
