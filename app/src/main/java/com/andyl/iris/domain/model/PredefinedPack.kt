package com.andyl.iris.domain.model

data class PredefinedPack(
    val id: String,
    val name: String,
    val description: String,
    val previewUrl: String,
    val categoryQuery: String, 
    val type: PackType = PackType.WEATHER,
    val isTimeBased: Boolean = false,
    val isFullRandom: Boolean = false,
    val isPremium: Boolean = false
)

enum class PackType {
    WEATHER, WEEKLY, TEMPERATURE
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
            categoryQuery = "gaming setup neon rgb room",
            isPremium = true
        ),
        PredefinedPack(
            id = "enchanted_forest",
            name = "Enchanted Forest",
            description = "Fairy-tale woods that change with the elements.",
            previewUrl = "https://images.pexels.com/photos/1179229/pexels-photo-1179229.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "fantasy magical forest mystery",
            isPremium = true
        ),
        PredefinedPack(
            id = "lofi_study_waifu",
            name = "Lofi Study",
            description = "Chill anime girls studying in a room that reflects the sky outside.",
            previewUrl = "https://images.unsplash.com/photo-1516280440614-37939bbdd4f1?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "lofi anime girl study room window rain",
            isPremium = true
        ),
        PredefinedPack(
            id = "space_nebula_weather",
            name = "Deep Space",
            description = "Cosmic nebulae and stars that follow the earthly weather.",
            previewUrl = "https://images.pexels.com/photos/1103970/pexels-photo-1103970.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "galaxy nebula space cosmos deep",
            isPremium = true
        ),
        // --- ADDED PACKS TO REACH 30 ---
        PredefinedPack(
            id = "nordic_legends",
            name = "Nordic Legends",
            description = "Viking warriors and cold mythical landscapes.",
            previewUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "viking warrior nordic fantasy snow",
            isPremium = true
        ),
        PredefinedPack(
            id = "tropical_escape",
            name = "Tropical Escape",
            description = "Crystal clear waters and golden sands for your screen.",
            previewUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "tropical beach paradise ocean sunny",
            isPremium = true
        ),
        PredefinedPack(
            id = "mountain_majesty",
            name = "Mountain Majesty",
            description = "Highest peaks and snowy summits from around the globe.",
            previewUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "mountain peaks snowy summit clouds",
            isPremium = true
        ),
        PredefinedPack(
            id = "abstract_flow",
            name = "Abstract Flow",
            description = "Mesmerizing fluid art and colorful abstract waves.",
            previewUrl = "https://images.unsplash.com/photo-1541701494587-cb58502866ab?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "abstract fluid art colorful liquid",
            isPremium = true
        ),
        PredefinedPack(
            id = "street_fashion",
            name = "Street Fashion",
            description = "Urban style and trendy outfits in the middle of the city.",
            previewUrl = "https://images.unsplash.com/photo-1529139513065-07b2ee0a9ec9?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "urban street fashion style model",
            isPremium = true
        ),
        PredefinedPack(
            id = "wild_safari",
            name = "Wild Safari",
            description = "Majestic lions, elephants and tigers in their natural habitat.",
            previewUrl = "https://images.unsplash.com/photo-1546182990-dffeafbe841d?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "wildlife safari animals lion tiger",
            isPremium = true
        ),
        PredefinedPack(
            id = "dark_fantasy",
            name = "Dark Fantasy",
            description = "Gothic castles, dark magic and mysterious realms.",
            previewUrl = "https://images.unsplash.com/photo-1613373123746-10b1a4725b7b?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "dark fantasy gothic magic castle",
            isPremium = true
        ),
        PredefinedPack(
            id = "steampunk_world",
            name = "Steampunk World",
            description = "Gears, goggles and industrial Victorian aesthetics.",
            previewUrl = "https://images.unsplash.com/photo-1514565131-fce0801e5785?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "steampunk victorian gears machinery",
            isPremium = true
        ),
        PredefinedPack(
            id = "vaporwave_sunset",
            name = "Vaporwave Sunset",
            description = "Retro 80s colors and neon palms in a digital sky.",
            previewUrl = "https://images.unsplash.com/photo-1614850523296-e8c041de4398?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "vaporwave aesthetic neon retro sunset",
            isPremium = true
        ),
        PredefinedPack(
            id = "underwater_wonders",
            name = "Underwater Wonders",
            description = "Vibrant coral reefs and majestic sea creatures.",
            previewUrl = "https://images.unsplash.com/photo-1583244532610-2ca22e111d4b?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "underwater coral reef fish sea ocean",
            isPremium = true
        ),
        PredefinedPack(
            id = "epic_castles",
            name = "Epic Castles",
            description = "Grand medieval citadels and forgotten ruins.",
            previewUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "medieval castle fortress ruins fantasy",
            isPremium = true
        ),
        PredefinedPack(
            id = "mecha_force",
            name = "Mecha Force",
            description = "Giant robots and futuristic war machines.",
            previewUrl = "https://images.pexels.com/photos/2599244/pexels-photo-2599244.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "mecha robot giant futuristic sci-fi",
            isPremium = true
        ),
        PredefinedPack(
            id = "desert_mirage",
            name = "Desert Mirage",
            description = "Endless golden dunes and star-filled desert nights.",
            previewUrl = "https://images.unsplash.com/photo-1473580044384-7ba9967e16a0?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "desert sand dunes sahara sunny",
            isPremium = true
        ),
        PredefinedPack(
            id = "rain_mood",
            name = "Rainy Mood",
            description = "Melancholic and cozy rain on windows and city streets.",
            previewUrl = "https://images.unsplash.com/photo-1515694346937-94d85e41e6f0?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "rain window street moody wet",
            isPremium = true
        ),
        PredefinedPack(
            id = "macro_world",
            name = "Macro World",
            description = "Incredible close-ups of flowers, insects and textures.",
            previewUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "macro photography nature insects flowers",
            isPremium = true
        ),
        PredefinedPack(
            id = "minimalist_calm",
            name = "Minimalist Calm",
            description = "Simple landscapes and clean compositions for focus.",
            previewUrl = "https://images.unsplash.com/photo-1494438639946-1ebd1d20bf85?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "minimalist landscape simple nature clean",
            isPremium = true
        ),
        PredefinedPack(
            id = "digital_art",
            name = "Digital Art",
            description = "Stunning digital illustrations and creative paintings.",
            previewUrl = "https://images.unsplash.com/photo-1558591710-4b4a1ae0f04d?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "digital art illustration painting creative",
            isPremium = true
        ),
        PredefinedPack(
            id = "cosmic_girls",
            name = "Cosmic Girls",
            description = "Anime heroines wandering through the starry void.",
            previewUrl = "https://images.unsplash.com/photo-1614728263952-84ea256f9679?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "anime girl space galaxy stars fantasy",
            isPremium = true
        ),
        PredefinedPack(
            id = "cyberpunk_women",
            name = "Cyberpunk Vanguards",
            description = "Futuristic women in a world of neon and high-tech shadows.",
            previewUrl = "https://images.unsplash.com/photo-1605810230434-7631ac76ec81?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cyberpunk woman neon futuristic city",
            isPremium = true
        ),
        // --- 5 NEW FREE PACKS ---
        PredefinedPack(
            id = "golden_hour",
            name = "Golden Hour",
            description = "Warm sunset tones that match the evening sky.",
            previewUrl = "https://images.unsplash.com/photo-1507400492013-162706c8c05e?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "golden hour sunset warm light landscape"
        ),
        PredefinedPack(
            id = "ocean_waves",
            name = "Ocean Rhythm",
            description = "Powerful waves and serene seascapes that shift with the weather.",
            previewUrl = "https://images.unsplash.com/photo-1505118380757-91f5f5632de0?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "ocean waves sea water blue nature"
        ),
        PredefinedPack(
            id = "cherry_blossom",
            name = "Sakura Dream",
            description = "Delicate cherry blossoms for a peaceful spring mood.",
            previewUrl = "https://images.unsplash.com/photo-1522383225653-ed111181a951?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cherry blossom sakura pink spring flower"
        ),
        PredefinedPack(
            id = "northern_lights",
            name = "Aurora Borealis",
            description = "Mesmerizing northern lights dancing across arctic skies.",
            previewUrl = "https://images.unsplash.com/photo-1531366936337-7c912a4589a7?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "aurora borealis northern lights sky night"
        ),
        PredefinedPack(
            id = "coffee_vibes",
            name = "Coffee Ritual",
            description = "Cozy café scenes and steaming cups for a warm start.",
            previewUrl = "https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "coffee cup cafe warm cozy morning"
        ),
        // --- 5 NEW PREMIUM WEATHER PACKS ---
        PredefinedPack(
            id = "japanese_garden",
            name = "Zen Garden",
            description = "Tranquil Japanese gardens with koi ponds and stone paths.",
            previewUrl = "https://images.unsplash.com/photo-1528360983277-13d401cdc186?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "japanese garden zen koi pond stone",
            isPremium = true
        ),
        PredefinedPack(
            id = "retro_cars",
            name = "Retro Rides",
            description = "Classic vintage cars in nostalgic settings.",
            previewUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "vintage classic car retro automobile",
            isPremium = true
        ),
        PredefinedPack(
            id = "waterfalls",
            name = "Cascade Dreams",
            description = "Majestic waterfalls from the world's most stunning locations.",
            previewUrl = "https://images.unsplash.com/photo-1432405972618-c6b0cfba8356?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "waterfall nature cascade tropical lush",
            isPremium = true
        ),
        PredefinedPack(
            id = "gothic_architecture",
            name = "Gothic Grandeur",
            description = "Dark cathedrals, rose windows and medieval stone arches.",
            previewUrl = "https://images.unsplash.com/photo-1548625149-fc4a29cf7092?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "gothic cathedral architecture stained glass dark",
            isPremium = true
        ),
        PredefinedPack(
            id = "sakura_night",
            name = "Night Blossom",
            description = "Cherry blossoms illuminated by moonlight and lanterns.",
            previewUrl = "https://images.unsplash.com/photo-1522383225653-ed111181a951?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cherry blossom night moon lantern japanese",
            isPremium = true
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
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "pixel_rpg_weekly",
            name = "Pixel Quest",
            description = "Retro RPG landscapes and characters for a nostalgic week.",
            previewUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "pixel art rpg landscape fantasy 8bit",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "cozy_weekly",
            name = "Cozy Retreat",
            description = "Warm interiors and peaceful cabin vibes for every day.",
            previewUrl = "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cozy interior cabin warm aesthetic",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "classic_cars_weekly",
            name = "Vintage Rides",
            description = "Iconic classic cars and automotive beauty every day.",
            previewUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "classic car vintage automobile photography",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "flowers_weekly",
            name = "Bloom & Flow",
            description = "Stunning floral photography to keep your week fresh.",
            previewUrl = "https://images.unsplash.com/photo-1490750967868-88aa4486c946?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "flowers bloom floral nature aesthetic",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "animals_weekly",
            name = "Animal Kingdom",
            description = "Beautiful wildlife and adorable animals to start your day.",
            previewUrl = "https://images.unsplash.com/photo-1543946207-39bd91e70ca7?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "wildlife animals nature photography",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "travel_weekly",
            name = "World Explorer",
            description = "Iconic landmarks and hidden gems from across the globe.",
            previewUrl = "https://images.pexels.com/photos/1008155/pexels-photo-1008155.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "travel landmarks city world architecture",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "otaku_journey",
            name = "Otaku Journey",
            description = "A unique anime-style heroine for every day of your week.",
            previewUrl = "https://images.unsplash.com/photo-1613373123746-10b1a4725b7b?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "anime girl character illustration",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        // --- 5 NEW FREE WEEKLY PACKS ---
        PredefinedPack(
            id = "sunsets_weekly",
            name = "Sunset Diary",
            description = "A different stunning sunset for each day.",
            previewUrl = "https://images.unsplash.com/photo-1495616811223-4d98c6e9c869?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "sunset sky orange colorful evening",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "puppies_weekly",
            name = "Puppy Love",
            description = "Adorable puppies to brighten every morning.",
            previewUrl = "https://images.unsplash.com/photo-1587300003388-59208cc962cb?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cute puppy dog happy adorable",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "space_weekly",
            name = "Cosmos Weekly",
            description = "A new planet or galaxy for each day of the week.",
            previewUrl = "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "space planet galaxy cosmos stars",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "food_weekly",
            name = "Foodie Week",
            description = "Mouth-watering dishes to start your day hungry for life.",
            previewUrl = "https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "food delicious cooking culinary aesthetic",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "sports_weekly",
            name = "Game Day",
            description = "Action-packed sports moments for an energetic week.",
            previewUrl = "https://images.unsplash.com/photo-1461896836934-bd45ba6880cd?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "sports action football basketball soccer",
            type = PackType.WEEKLY
        ),
        // --- 20 NEW PREMIUM WEEKLY PACKS ---
        PredefinedPack(
            id = "sushi_weekly",
            name = "Sushi Art",
            description = "Exquisite Japanese cuisine plated like masterpieces.",
            previewUrl = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "sushi japanese food aesthetic plating",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "drone_weekly",
            name = "Sky View",
            description = "Breathtaking drone photography from around the world.",
            previewUrl = "https://images.unsplash.com/photo-1473968512647-3e447244af8f?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "aerial drone landscape bird view",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "neon_weekly",
            name = "Neon Nights",
            description = "Electric neon signs and glowing cityscapes every night.",
            previewUrl = "https://images.unsplash.com/photo-1514565131-fce0801e5785?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "neon city night lights urban glow",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "ballet_weekly",
            name = "Graceful Moves",
            description = "Elegant ballet dancers frozen in perfect poses.",
            previewUrl = "https://images.unsplash.com/photo-1508807526345-15e9b5f4eaff?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "ballet dancer grace elegant dance",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "canyon_weekly",
            name = "Canyon Depth",
            description = "Deep red canyons and dramatic rock formations.",
            previewUrl = "https://images.unsplash.com/photo-1474044159687-1ee9f3a51722?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "canyon red rock formation nature",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "astronaut_weekly",
            name = "Space Walker",
            description = "Astronauts exploring the final frontier each day.",
            previewUrl = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "astronaut space suit nasa exploration",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "libro_weekly",
            name = "Book Nook",
            description = "Cozy reading corners and stacked bookshelves.",
            previewUrl = "https://images.unsplash.com/photo-1507842217343-583bb7270b66?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "books library reading cozy aesthetic",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "vintage_weekly",
            name = "Retro Rewind",
            description = "Nostalgic vintage scenes from decades past.",
            previewUrl = "https://images.unsplash.com/photo-1496027155967-d856b59c5d6a?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "vintage retro nostalgia old film",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "waterfall_weekly",
            name = "Cascade Week",
            description = "Powerful waterfalls from every continent.",
            previewUrl = "https://images.unsplash.com/photo-1432405972618-c6b0cfba8356?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "waterfall cascade nature tropical",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "street_food_weekly",
            name = "Street Eats",
            description = "Global street food culture captured in vivid detail.",
            previewUrl = "https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "street food market global cuisine",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "butterflies_weekly",
            name = "Wing Beauty",
            description = "Delicate butterflies in vibrant natural settings.",
            previewUrl = "https://images.unsplash.com/photo-1452570053594-1b985d6ea890?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "butterfly nature colorful wings macro",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "mountain_lake_weekly",
            name = "Alpine Mirror",
            description = "Crystal-clear mountain lakes reflecting snowy peaks.",
            previewUrl = "https://images.unsplash.com/photo-1472396961693-142e6e269027?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "mountain lake reflection alpine nature",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "tattoo_weekly",
            name = "Ink Stories",
            description = "Stunning tattoo art and body painting masterpieces.",
            previewUrl = "https://images.unsplash.com/photo-1590246814883-57cffad2ca9d?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "tattoo art ink body painting design",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "lighthouse_weekly",
            name = "Beacon Light",
            description = "Iconic lighthouses standing guard over stormy seas.",
            previewUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "lighthouse sea ocean coast beacon",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "chocolate_weekly",
            name = "Cocoa Bliss",
            description = "Decadent chocolate creations for a sweet week.",
            previewUrl = "https://images.unsplash.com/photo-1481391319762-47dff72954d9?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "chocolate dessert sweet cocoa luxury",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "aurora_weekly",
            name = "Northern Glow",
            description = "Dancing auroras over Scandinavian landscapes.",
            previewUrl = "https://images.unsplash.com/photo-1531366936337-7c912a4589a7?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "aurora borealis northern lights norway",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "robot_weekly",
            name = "Mech Week",
            description = "Futuristic robots and AI companions every day.",
            previewUrl = "https://images.unsplash.com/photo-1558618666-fcd25c85f82e?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "robot futuristic ai technology mechanical",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "coral_weekly",
            name = "Reef Life",
            description = "Vibrant coral reefs teeming with marine life.",
            previewUrl = "https://images.unsplash.com/photo-1583244532610-2ca22e111d4b?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "coral reef underwater ocean fish marine",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "graffiti_weekly",
            name = "Wall Stories",
            description = "Powerful street art telling stories on city walls.",
            previewUrl = "https://images.unsplash.com/photo-1499750310107-5fef28a66643?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "graffiti street art urban wall color",
            type = PackType.WEEKLY,
            isPremium = true
        ),
        PredefinedPack(
            id = "sakura_weekly",
            name = "Cherry Path",
            description = "Walk under blooming cherry trees every day.",
            previewUrl = "https://images.unsplash.com/photo-1522383225653-ed111181a951?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cherry blossom sakura japan path pink",
            type = PackType.WEEKLY,
            isPremium = true
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
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "digital_canvas",
            name = "Digital Canvas",
            description = "Abstract art and color gradients that shift through the day.",
            previewUrl = "https://images.pexels.com/photos/2832382/pexels-photo-2832382.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "abstract gradient digital art",
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "cyber_city_cycle",
            name = "Cyber City 2077",
            description = "A futuristic city day/night cycle for your screen.",
            previewUrl = "https://images.pexels.com/photos/2387418/pexels-photo-2387418.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "futuristic city cyberpunk neon 4k",
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "lofi_beats",
            name = "Lofi Vibes",
            description = "Chill study and relax scenes for your routine.",
            previewUrl = "https://images.unsplash.com/photo-1516280440614-37939bbdd4f1?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "lofi aesthetic chill study room",
            isTimeBased = true,
            isPremium = true
        ),
        // --- 5 NEW FREE TIME PACKS ---
        PredefinedPack(
            id = "city_lights_time",
            name = "City Pulse",
            description = "The city transitions from dawn rush to nightlife.",
            previewUrl = "https://images.unsplash.com/photo-1480714378408-67cf0d13bc1b?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "city skyline urban day night cycle",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "ocean_tides_time",
            name = "Ocean Tides",
            description = "Calm morning shores turning to dramatic sunset waves.",
            previewUrl = "https://images.unsplash.com/photo-1505118380757-91f5f5632de0?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "ocean tide beach morning sunset water",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "forest_moods_time",
            name = "Forest Moods",
            description = "Misty dawn forests evolving into golden afternoon light.",
            previewUrl = "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "forest trees morning mist golden light",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "desert_dreams_time",
            name = "Desert Dreams",
            description = "From cool desert dawn to scorching midday dunes.",
            previewUrl = "https://images.unsplash.com/photo-1473580044384-7ba9967e16a0?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "desert dunes sunrise hot sand sahara",
            isTimeBased = true
        ),
        PredefinedPack(
            id = "mountain_dawn_time",
            name = "Mountain Dawn",
            description = "Alpine peaks catching the first and last light of day.",
            previewUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "mountain peak dawn sunrise sunset alpine",
            isTimeBased = true
        ),
        // --- 20 NEW PREMIUM TIME PACKS ---
        PredefinedPack(
            id = "tokyo_cycle_time",
            name = "Tokyo Cycle",
            description = "Neon-lit Shibuya from morning calm to midnight glow.",
            previewUrl = "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "tokyo japan neon city street day night",
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "cherry_time",
            name = "Blossom Clock",
            description = "Cherry trees from dawn pink to sunset gold.",
            previewUrl = "https://images.unsplash.com/photo-1522383225653-ed111181a951?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cherry blossom tree pink sunrise sunset",
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "nyc_time",
            name = "Big Apple Time",
            description = "Manhattan skyline through every hour of the day.",
            previewUrl = "https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "new york city manhattan skyline tower",
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "volcano_time",
            name = "Volcanic Glow",
            description = "Active volcanoes from peaceful dawn to fiery night eruption.",
            previewUrl = "https://images.unsplash.com/photo-1462275646964-a0e3c11f18a6?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "volcano lava eruption fire night glow",
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "ice_age_time",
            name = "Frozen World",
            description = "Glacial landscapes shifting from blue dawn to white noon.",
            previewUrl = "https://images.unsplash.com/photo-1477346611705-65d1883cee1e?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "glacier ice frozen arctic blue cold",
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "rainy_cafe_time",
            name = "Rainy Café",
            description = "Cozy café windows with rain from morning to night.",
            previewUrl = "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cafe rain window cozy coffee warm",
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "pirate_time",
            name = "Pirate Seas",
            description = "High seas adventures from dawn sailing to moonlit storms.",
            previewUrl = "https://images.unsplash.com/photo-1534224039826-c7a0eda0e6b3?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "pirate ship sea ocean adventure storm",
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "candy_time",
            name = "Sweet Dreams",
            description = "Colorful candyland from sunrise sprinkles to midnight chocolate.",
            previewUrl = "https://images.unsplash.com/photo-1587132137056-bfbf0166836e?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "candy colorful sweet dessert bright",
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "ancient_ruins_time",
            name = "Lost Temples",
            description = "Ancient ruins from misty dawn to torch-lit night.",
            previewUrl = "https://images.unsplash.com/photo-1564507592333-c60657eea523?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "ancient temple ruins mystical history",
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "fireworks_time",
            name = "Sky Paint",
            description = "Fireworks and sparklers lighting up every evening.",
            previewUrl = "https://images.unsplash.com/photo-1467810563316-b5476525c0f9?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "fireworks celebration night sky sparkler",
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "underwater_time",
            name = "Deep Blue Hour",
            description = "Underwater worlds from sunlit shallows to midnight depths.",
            previewUrl = "https://images.unsplash.com/photo-1583244532610-2ca22e111d4b?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "underwater ocean deep blue coral fish",
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "space_station_time",
            name = "Orbital View",
            description = "Earth from the ISS through 16 sunrises a day.",
            previewUrl = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "space station ISS earth orbit sunrise",
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "autumn_path_time",
            name = "Autumn Trail",
            description = "Fall forest paths from golden morning to amber sunset.",
            previewUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "autumn forest path fall leaves orange trail",
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "northern_time",
            name = "Arctic Glow",
            description = "Northern lights from green dusk to purple midnight.",
            previewUrl = "https://images.unsplash.com/photo-1531366936337-7c912a4589a7?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "aurora northern lights arctic night sky",
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "sahara_time",
            name = "Sand Clock",
            description = "Sahara dunes from cool blue dawn to scorching noon.",
            previewUrl = "https://images.unsplash.com/photo-1473580044384-7ba9967e16a0?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "sahara desert sand dune heat sunrise",
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "waterfall_time",
            name = "Cascade Clock",
            description = "Waterfalls from misty dawn to rainbow afternoon.",
            previewUrl = "https://images.unsplash.com/photo-1432405972618-c6b0cfba8356?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "waterfall cascade mist rainbow nature",
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "gothic_time",
            name = "Dark Hour",
            description = "Gothic architecture from foggy dawn to moonlit night.",
            previewUrl = "https://images.unsplash.com/photo-1548625149-fc4a29cf7092?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "gothic cathedral dark fog night moon",
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "cyber_time",
            name = "Neon Clock",
            description = "Cyberpunk cityscapes from morning haze to neon night.",
            previewUrl = "https://images.unsplash.com/photo-1605810230434-7631ac76ec81?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "cyberpunk city neon futuristic night",
            isTimeBased = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "zen_time",
            name = "Zen Hour",
            description = "Japanese gardens from misty morning to lantern-lit evening.",
            previewUrl = "https://images.unsplash.com/photo-1528360983277-13d401cdc186?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "zen garden japanese lantern evening mist",
            isTimeBased = true,
            isPremium = true
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
            isFullRandom = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "random_discovery",
            name = "Discovery Mix",
            description = "A complete variety of high-quality photos.",
            previewUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "wallpaper 4k photography",
            isFullRandom = true,
            isPremium = true
        ),
        PredefinedPack(
            id = "waifu_surprise",
            name = "Waifu Surprise",
            description = "Random anime girl art for your screen.",
            previewUrl = "https://images.pexels.com/photos/15942493/pexels-photo-15942493.jpeg?auto=compress&cs=tinysrgb&w=800",
            categoryQuery = "anime girl art illustration",
            isFullRandom = true,
            isPremium = true
        )
    )

    val temperaturePacks = listOf(
        PredefinedPack(
            id = "snow_city_temp",
            name = "Snowy Streets",
            description = "City streets covered in fresh snow for freezing days.",
            previewUrl = "https://images.unsplash.com/photo-1491002052546-bf38f186af56?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "snow city street winter cold frozen",
            type = PackType.TEMPERATURE
        ),
        PredefinedPack(
            id = "rain_window_temp",
            name = "Rainy Window",
            description = "Rain droplets on glass for cool damp weather.",
            previewUrl = "https://images.unsplash.com/photo-1515694346937-94d85e41e6f0?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "rain window glass droplets cool moody",
            type = PackType.TEMPERATURE
        ),
        PredefinedPack(
            id = "breeze_fields_temp",
            name = "Breezy Fields",
            description = "Wheat fields swaying in gentle wind for mild days.",
            previewUrl = "https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "wheat field wind nature green warm fresh",
            type = PackType.TEMPERATURE
        ),
        PredefinedPack(
            id = "frozen_peaks_temp",
            name = "Frozen Peaks",
            description = "Icy mountain summits that activate in freezing temperatures.",
            previewUrl = "https://images.unsplash.com/photo-1477346611705-65d1883cee1e?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "snow mountain ice frozen winter peak cold",
            type = PackType.TEMPERATURE,
            isPremium = true
        ),
        PredefinedPack(
            id = "autumn_forest_temp",
            name = "Autumn Blaze",
            description = "Golden and crimson forests for cool weather days.",
            previewUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "autumn fall forest orange leaves trees",
            type = PackType.TEMPERATURE,
            isPremium = true
        ),
        PredefinedPack(
            id = "spring_meadow_temp",
            name = "Spring Meadow",
            description = "Fresh wildflower fields for pleasant mild temperatures.",
            previewUrl = "https://images.unsplash.com/photo-1490750967868-88aa4486c946?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "spring meadow wildflower green fresh nature",
            type = PackType.TEMPERATURE,
            isPremium = true
        ),
        PredefinedPack(
            id = "summer_beach_temp",
            name = "Summer Blaze",
            description = "Sun-drenched beaches and turquoise waters for hot days.",
            previewUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "summer beach sun tropical ocean hot paradise",
            type = PackType.TEMPERATURE,
            isPremium = true
        ),
        PredefinedPack(
            id = "storm_chaser_temp",
            name = "Storm Chaser",
            description = "Dramatic thunderstorms and lightning for warm humid days.",
            previewUrl = "https://images.unsplash.com/photo-1472145246862-b24cf25c4a36?auto=format&fit=crop&w=800&q=80",
            categoryQuery = "thunderstorm lightning dramatic sky dark cloud",
            type = PackType.TEMPERATURE,
            isPremium = true
        )
    )

    val packs = weatherPacks + weeklyPacks + timePacks + randomPacks + temperaturePacks
}
