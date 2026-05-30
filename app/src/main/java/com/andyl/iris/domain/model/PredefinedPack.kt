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
            categoryQuery = "nature landscapes"
        ),
        PredefinedPack(
            id = "urban_weather",
            name = "Neon Symphony",
            description = "The vibrant soul of the city, from neon nights to rainy days.",
            previewUrl = "https://images.unsplash.com/photo-1514565131-fce0801e5785?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "urban cyberpunk city"
        ),
        PredefinedPack(
            id = "anime_weather",
            name = "Anime Vibe",
            description = "Hand-painted style landscapes inspired by legendary animated films.",
            previewUrl = "https://images.unsplash.com/photo-1542273917363-3b1817f69a2d?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "anime landscape art"
        ),
        PredefinedPack(
            id = "minimal_weather",
            name = "Structural Silence",
            description = "Minimalist architecture and clean lines for a calm experience.",
            previewUrl = "https://images.pexels.com/photos/262367/pexels-photo-262367.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "minimalist architecture"
        ),
        PredefinedPack(
            id = "waifu_weather",
            name = "Ethereal Waifus",
            description = "Anime heroines that adapt to the shifting weather and sky.",
            previewUrl = "https://images.pexels.com/photos/15942493/pexels-photo-15942493.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "anime girl art background"
        ),
        PredefinedPack(
            id = "urban_muse",
            name = "Urban Muse",
            description = "Elegant portraits that mirror the local weather and atmospheric mood.",
            previewUrl = "https://images.pexels.com/photos/157675/fashion-men-s-fashion-suit-steampunk-157675.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "girl portrait model fashion"
        ),
        PredefinedPack(
            id = "river_monumental",
            name = "River: Monumental",
            description = "The passion of El Más Grande, matching your weather.",
            previewUrl = "https://images.unsplash.com/photo-1599148564010-09886a048a1c?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "River Plate stadium football stadium"
        ),
        PredefinedPack(
            id = "gamer_sanctum",
            name = "Gamer Sanctum",
            description = "High-tech setups that evolve with your day.",
            previewUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "gaming setup neon rgb"
        ),
        PredefinedPack(
            id = "enchanted_forest",
            name = "Enchanted Forest",
            description = "Fairy-tale woods that change with the elements.",
            previewUrl = "https://images.pexels.com/photos/1179229/pexels-photo-1179229.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "fantasy forest magical"
        ),
        PredefinedPack(
            id = "cyber_cosplay",
            name = "Neon Cosplay",
            description = "Stunning cyberpunk characters in every condition.",
            previewUrl = "https://images.pexels.com/photos/3642302/pexels-photo-3642302.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "cyberpunk cosplay girl"
        ),
        // --- 10 NEW WEATHER PACKS ---
        PredefinedPack(
            id = "goddess_weather",
            name = "Goddess of Nature",
            description = "Divine feminine energy in harmony with the sky.",
            previewUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "beautiful goddess fantasy girl"
        ),
        PredefinedPack(
            id = "mecha_waifu_weather",
            name = "Mecha Maiden",
            description = "Futuristic androids adapting to the environment.",
            previewUrl = "https://images.pexels.com/photos/15747683/pexels-photo-15747683.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "mecha anime girl robot"
        ),
        PredefinedPack(
            id = "street_muse_weather",
            name = "Street Muse",
            description = "Urban style portraits that follow the elements.",
            previewUrl = "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "street style girl urban"
        ),
        PredefinedPack(
            id = "magical_sorceress_weather",
            name = "Starlight Sorceress",
            description = "Mystical magic girls under shifting skies.",
            previewUrl = "https://images.unsplash.com/photo-1560942485-b2a11cc13456?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "magical anime girl magic"
        ),
        PredefinedPack(
            id = "viking_legend_weather",
            name = "Nordic Legend",
            description = "Shield-maidens facing the storms of time.",
            previewUrl = "https://images.pexels.com/photos/17696225/pexels-photo-17696225.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "viking girl cosplay"
        ),
        PredefinedPack(
            id = "data_runner_weather",
            name = "Data Runner",
            description = "Cyberpunk hackers in the neon rain.",
            previewUrl = "https://images.pexels.com/photos/12431767/pexels-photo-12431767.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "cyberpunk gamer girl"
        ),
        PredefinedPack(
            id = "shadow_empress_weather",
            name = "Shadow Empress",
            description = "Dark fantasy waifus in a shifting realm.",
            previewUrl = "https://images.unsplash.com/photo-1613373123746-10b1a4725b7b?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "dark fantasy anime waifu"
        ),
        PredefinedPack(
            id = "warrior_path_weather",
            name = "Warrior's Path",
            description = "Legendary heroines in the heat of battle.",
            previewUrl = "https://images.pexels.com/photos/18251268/pexels-photo-18251268.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "fantasy warrior woman armor"
        ),
        PredefinedPack(
            id = "ethereal_elf_weather",
            name = "Ethereal Elf",
            description = "Forest spirits living through the seasons.",
            previewUrl = "https://images.pexels.com/photos/18529329/pexels-photo-18529329.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "elf girl fantasy nature"
        ),
        PredefinedPack(
            id = "abyssal_beauty_weather",
            name = "Abyssal Beauty",
            description = "Underwater fantasies that change with the day.",
            previewUrl = "https://images.unsplash.com/photo-1583244532610-2ca22e111d4b?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "underwater girl fantasy"
        )
    )

    val weeklyPacks = listOf(
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
            previewUrl = "https://images.unsplash.com/photo-1543946207-39bd91e70ca7?auto=format&fit=crop&w=800&q=80",
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
            id = "otaku_journey",
            name = "Otaku Journey",
            description = "A unique anime-style heroine for every day of your week.",
            previewUrl = "https://images.unsplash.com/photo-1613373123746-10b1a4725b7b?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "anime girl illustration manga",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "river_weekly",
            name = "River Passion",
            description = "Daily pride for the Millionaire. Proudly Monumental.",
            previewUrl = "https://images.unsplash.com/photo-1614728263952-84ea256f9679?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "River Plate fans football soccer"
        ),
        PredefinedPack(
            id = "cosplay_spotlight",
            name = "Cosplay Weekly",
            description = "The best characters brought to life, day by day.",
            previewUrl = "https://images.pexels.com/photos/1390530/pexels-photo-1390530.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "cosplay character girl",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "gaming_legends",
            name = "Gaming Legends",
            description = "Iconic heroes from your favorite video games.",
            previewUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "video game character wallpaper",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "aesthetic_girls",
            name = "Aesthetic Flow",
            description = "Trendy fashion and style vibes for a modern week.",
            previewUrl = "https://images.pexels.com/photos/1926769/pexels-photo-1926769.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "aesthetic girl style fashion",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "mythical_creatures",
            name = "Mythical World",
            description = "Dragons and magical beasts for your daily journey.",
            previewUrl = "https://images.unsplash.com/photo-1618336753974-aae8e04506aa?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "dragon mythical fantasy creature",
            type = PackType.WEEKLY
        ),
        // --- 10 NEW WEEKLY PACKS ---
        PredefinedPack(
            id = "waifu_calendar",
            name = "Waifu Haven",
            description = "A new anime heroine for every day of your week.",
            previewUrl = "https://images.pexels.com/photos/15942493/pexels-photo-15942493.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "anime waifu high quality",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "cosplay_masters",
            name = "Master Cosplay",
            description = "Legendary cosplayers in stunning professional photos.",
            previewUrl = "https://images.pexels.com/photos/1390530/pexels-photo-1390530.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "professional cosplay photography",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "retro_quest",
            name = "Retro Quest",
            description = "Classic gaming pixel art for a nostalgic week.",
            previewUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "pixel art gaming scenery",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "manga_shores",
            name = "Manga Shores",
            description = "Summer vibes with your favorite anime characters.",
            previewUrl = "https://images.unsplash.com/photo-1560942485-b2a11cc13456?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "anime girl beach summer",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "vogue_pulse",
            name = "Vogue Pulse",
            description = "High fashion urban portraits for a stylish week.",
            previewUrl = "https://images.unsplash.com/photo-1529139513065-07b2ee0a9ec9?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "urban fashion girl model",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "rpg_journey",
            name = "RPG Journey",
            description = "Epic fantasy environments from massive RPGs.",
            previewUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "fantasy rpg environment world",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "neural_waifu",
            name = "Neural Waifu",
            description = "Cybernetic anime waifus from a neon future.",
            previewUrl = "https://images.pexels.com/photos/3642302/pexels-photo-3642302.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "cyberpunk anime waifu blue neon",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "valkyrie_hall",
            name = "Valkyrie Hall",
            description = "Mythical Norse warriors protecting your screen.",
            previewUrl = "https://images.pexels.com/photos/15124036/pexels-photo-15124036.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "valkyrie warrior woman",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "jpop_idol",
            name = "Neon Idol",
            description = "Cute J-Pop inspired anime idols for your daily joy.",
            previewUrl = "https://images.unsplash.com/photo-1514525253361-bee8718a300a?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "anime girl idol stage",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "mystic_portraits",
            name = "Mystic Portraits",
            description = "Enchanting and mysterious girls in dark aesthetics.",
            previewUrl = "https://images.unsplash.com/photo-1509248961158-e54f6934749c?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "mysterious girl dark aesthetic",
            type = PackType.WEEKLY
        )
    )

    val timePacks = listOf(
        PredefinedPack(
            id = "day_night_cycle",
            name = "Atmospheric Sky",
            description = "Strict time-based overrides to perfectly match the clock.",
            previewUrl = "https://images.unsplash.com/photo-1467810563316-b5476525c0f9?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "sky landscape",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "digital_canvas",
            name = "Digital Canvas",
            description = "Abstract art and color gradients that shift through the day.",
            previewUrl = "https://images.pexels.com/photos/2832382/pexels-photo-2832382.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "abstract gradient art",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "micro_world",
            name = "Micro World",
            description = "Stunning macro photography revealing hidden textures.",
            previewUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "macro nature texture",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "pixel_art_time",
            name = "8-Bit Daybreak",
            description = "Charming pixel art landscapes that follow the sun.",
            previewUrl = "https://images.pexels.com/photos/1205301/pexels-photo-1205301.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "pixel art landscape 8bit",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "retro_wave",
            name = "Retro Wave",
            description = "Syntwave and 80s retro-futurism for your daily cycle.",
            previewUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "vaporwave synthwave retro aesthetic",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "waifu_emotions",
            name = "Waifu Moods",
            description = "Anime girls matching the feeling of your day.",
            previewUrl = "https://images.pexels.com/photos/15942493/pexels-photo-15942493.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "anime girl art",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "lofi_beats",
            name = "Lofi Vibes",
            description = "Chill study and relax scenes for your routine.",
            previewUrl = "https://images.unsplash.com/photo-1516280440614-37939bbdd4f1?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "lofi aesthetic chill",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "cyber_city_cycle",
            name = "Cyber City 2077",
            description = "A futuristic city day/night cycle for your screen.",
            previewUrl = "https://images.pexels.com/photos/2387418/pexels-photo-2387418.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "futuristic city cyberpunk night",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "river_history",
            name = "River Glory",
            description = "The rich history of the club on your screen.",
            previewUrl = "https://images.unsplash.com/photo-1599148564010-09886a048a1c?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "River Plate history legends",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "minimal_gradient_time",
            name = "Pure Gradients",
            description = "Smooth color transitions for your daily journey.",
            previewUrl = "https://images.pexels.com/photos/1242348/pexels-photo-1242348.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "minimalist gradient background",
            isTimeBased = true
        ),
        // --- 10 NEW TIME PACKS ---
        PredefinedPack(
            id = "battlestation_cycle",
            name = "Gamer Setup",
            description = "Epic gaming rooms that light up as night falls.",
            previewUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "gaming setup night room",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "event_horizon_cosplay",
            name = "Cosplay Sunset",
            description = "Cinematic cosplay portraits in golden hour light.",
            previewUrl = "https://images.pexels.com/photos/14545300/pexels-photo-14545300.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "cosplay sunset outdoor",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "anime_nightfall",
            name = "Anime Nightfall",
            description = "Stunning anime skies that follow your local clock.",
            previewUrl = "https://images.unsplash.com/photo-1506704990130-b8f051223a67?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "anime night sky stars",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "fashion_pulse",
            name = "Street Fashion",
            description = "Trendy street style that shifts through the day.",
            previewUrl = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "fashion girl street daylight",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "soft_waifu_morning",
            name = "Morning Waifu",
            description = "Gentle anime art to start your day with light.",
            previewUrl = "https://images.pexels.com/photos/15942493/pexels-photo-15942493.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "anime girl morning sunlight",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "glitch_city_time",
            name = "Glitch City",
            description = "Cyberpunk glitch aesthetics for a futuristic cycle.",
            previewUrl = "https://images.pexels.com/photos/2387418/pexels-photo-2387418.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "cyberpunk glitch aesthetic",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "dreamy_waifu_cycle",
            name = "Ethereal Dreams",
            description = "Fantasy anime girls in a cycle of magic.",
            previewUrl = "https://images.pexels.com/photos/15747683/pexels-photo-15747683.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "ethereal anime girl dream",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "cyber_portraits_time",
            name = "Neon Future",
            description = "Glow-in-the-dark portraits that light up at night.",
            previewUrl = "https://images.pexels.com/photos/3642302/pexels-photo-3642302.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "neon portrait girl future",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "vr_sanctuary",
            name = "Virtual Sanctuary",
            description = "Digital waifus in a virtual reality time cycle.",
            previewUrl = "https://images.unsplash.com/photo-1592477976562-f32724623091?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "virtual reality girl anime",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "cinematic_games_time",
            name = "Gaming Cinema",
            description = "The most beautiful game landscapes ever made.",
            previewUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cinematic video game landscape",
            isTimeBased = true
        )
    )

    val randomPacks = listOf(
        PredefinedPack(
            id = "random_discovery",
            name = "Discovery Mix",
            description = "A complete variety of high-quality photos.",
            previewUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "wallpaper",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "waifu_surprise",
            name = "Waifu Surprise",
            description = "Random anime girl art for your screen.",
            previewUrl = "https://images.pexels.com/photos/15942493/pexels-photo-15942493.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "anime girl art illustration",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "gamer_gear_random",
            name = "Gamer Gear",
            description = "Random high-end hardware and setups.",
            previewUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "gaming hardware neon",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "nature_best_random",
            name = "Nature's Best",
            description = "Random breathtaking landscapes from the world.",
            previewUrl = "https://images.pexels.com/photos/3225517/pexels-photo-3225517.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "nature wallpaper 4k",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "urban_street_random",
            name = "Urban Street",
            description = "Random city life and architecture photography.",
            previewUrl = "https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "city street photography",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "cosplay_fantasy_random",
            name = "Cosplay Fantasy",
            description = "Random amazing cosplays from every realm.",
            previewUrl = "https://images.pexels.com/photos/1390530/pexels-photo-1390530.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "fantasy cosplay girl",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "minimal_art_random",
            name = "Minimal Art",
            description = "Random clean aesthetics and abstract art.",
            previewUrl = "https://images.unsplash.com/photo-1494438639946-1ebd1d20bf85?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "minimalist art aesthetic",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "everything_river_random",
            name = "Everything River",
            description = "Random River Plate content for fans.",
            previewUrl = "https://images.unsplash.com/photo-1614728263952-84ea256f9679?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "River Plate Argentina fans",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "abstract_dreams_random",
            name = "Abstract Dreams",
            description = "Random digital creations and color flows.",
            previewUrl = "https://images.pexels.com/photos/2832382/pexels-photo-2832382.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "abstract digital art",
            isFullRandom = true
        ),
        PredefinedPack(
            id = "infinite_variety_random",
            name = "Infinite Mix",
            description = "Literally anything of high quality.",
            previewUrl = "https://images.unsplash.com/photo-1493612276216-ee3925520721?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "wallpaper amazing high resolution",
            isFullRandom = true
        )
    )

    val packs = weatherPacks + weeklyPacks + timePacks + randomPacks
}
