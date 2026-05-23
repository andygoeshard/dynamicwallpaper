package com.andyl.iris.domain.model

data class PredefinedPack(
    val id: String,
    val name: String,
    val description: String,
    val previewUrl: String,
    val weatherRules: List<PredefinedRule> = emptyList(),
    val dailyRules: List<PredefinedDailyRule> = emptyList(),
    val isRandom: Boolean = false,
    val randomQuery: String? = null,
    val type: PackType = PackType.WEATHER
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

object PredefinedPacks {
    val packs = listOf(
        PredefinedPack(
            id = "nature_weather_1",
            name = "Cinematic Nature",
            description = "Epic landscapes that shift with the weather and time.",
            previewUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?q=80&w=1000",
            weatherRules = createNatureWeatherRules(),
            type = PackType.WEATHER
        ),
        PredefinedPack(
            id = "urban_weather_1",
            name = "Neon Soul",
            description = "City vibes, neon lights, and cyberpunk aesthetics.",
            previewUrl = "https://images.unsplash.com/photo-1514565131-fce0801e5785?q=80&w=1000",
            weatherRules = createUrbanWeatherRules(),
            type = PackType.WEATHER
        ),
        PredefinedPack(
            id = "random_nature_weather",
            name = "Nature Discovery",
            description = "Random epic landscapes. Every install is a new adventure.",
            previewUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?q=80&w=1000",
            isRandom = true,
            randomQuery = "nature landscape",
            type = PackType.WEATHER
        ),
        PredefinedPack(
            id = "weekly_tour",
            name = "World Tour",
            description = "Travel the globe! A famous city for every day of the week.",
            previewUrl = "https://images.unsplash.com/photo-1518391846015-55a9cc003b25?q=80&w=1000",
            type = PackType.WEEKLY,
            dailyRules = listOf(
                PredefinedDailyRule("monday", "https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?q=80&w=1600&ar=9:16&fit=crop"), // London
                PredefinedDailyRule("tuesday", "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?q=80&w=1600&ar=9:16&fit=crop"), // Paris
                PredefinedDailyRule("wednesday", "https://images.unsplash.com/photo-1525625239513-445c475ca421?q=80&w=1600&ar=9:16&fit=crop"), // Tokyo
                PredefinedDailyRule("thursday", "https://images.unsplash.com/photo-1485738422979-f5c462d49f74?q=80&w=1600&ar=9:16&fit=crop"), // New York
                PredefinedDailyRule("friday", "https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?q=80&w=1600&ar=9:16&fit=crop"), // Sydney
                PredefinedDailyRule("saturday", "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?q=80&w=1600&ar=9:16&fit=crop"), // Dubai
                PredefinedDailyRule("sunday", "https://images.unsplash.com/photo-1552832230-c0197dd311b5?q=80&w=1600&ar=9:16&fit=crop") // Rome
            )
        ),
        PredefinedPack(
            id = "random_animals_weekly",
            name = "Animal Kingdom",
            description = "A surprise majestic animal for every day of the week.",
            previewUrl = "https://images.unsplash.com/photo-1437622368342-7a3d73a34c8f?q=80&w=1000",
            isRandom = true,
            randomQuery = "wildlife animal",
            type = PackType.WEEKLY
        ),
        PredefinedPack(
            id = "cyberpunk_weekly",
            name = "Cyber Week",
            description = "Glitch art and futuristic neon for every day.",
            previewUrl = "https://images.unsplash.com/photo-1605810230434-7631ac76ec81?q=80&w=1000",
            type = PackType.WEEKLY,
            dailyRules = listOf(
                PredefinedDailyRule("monday", "https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=1600&ar=9:16&fit=crop"),
                PredefinedDailyRule("tuesday", "https://images.unsplash.com/photo-1511447333015-45b65e60f6d5?q=80&w=1600&ar=9:16&fit=crop"),
                PredefinedDailyRule("wednesday", "https://images.unsplash.com/photo-1605810230434-7631ac76ec81?q=80&w=1600&ar=9:16&fit=crop"),
                PredefinedDailyRule("thursday", "https://images.unsplash.com/photo-1563089145-599997674d42?q=80&w=1600&ar=9:16&fit=crop"),
                PredefinedDailyRule("friday", "https://images.unsplash.com/photo-1535223289827-42f1e9919769?q=80&w=1600&ar=9:16&fit=crop"),
                PredefinedDailyRule("saturday", "https://images.unsplash.com/photo-1504333638930-c8787321eba0?q=80&w=1600&ar=9:16&fit=crop"),
                PredefinedDailyRule("sunday", "https://images.unsplash.com/photo-1518770660439-4636190af475?q=80&w=1600&ar=9:16&fit=crop")
            )
        )
    )

    private fun createNatureWeatherRules(): List<PredefinedRule> {
        val rules = mutableListOf<PredefinedRule>()
        val images = mapOf(
            (Weather.Clear to TimeOfDay.DAWN) to "1470252649352-181107a05163",
            (Weather.Clear to TimeOfDay.DAY) to "1506744038136-46273834b3fb",
            (Weather.Clear to TimeOfDay.DUSK) to "1464817732615-68d84ddc30c4",
            (Weather.Clear to TimeOfDay.NIGHT) to "1519681395603-d236d3293bb8",
            
            (Weather.Cloudy to TimeOfDay.DAWN) to "1494500670220-68973359d9c8",
            (Weather.Cloudy to TimeOfDay.DAY) to "1483977399921-6cf38243c391",
            (Weather.Cloudy to TimeOfDay.DUSK) to "1472213984618-c79afcd5dbd0",
            (Weather.Cloudy to TimeOfDay.NIGHT) to "1532978393322-c1c28c667015",
            
            (Weather.Rain to TimeOfDay.DAWN) to "1496034054598-14a13540600a",
            (Weather.Rain to TimeOfDay.DAY) to "1534274988757-a28bf1f53c17",
            (Weather.Rain to TimeOfDay.DUSK) to "1515694346937-94d85e41e6f0",
            (Weather.Rain to TimeOfDay.NIGHT) to "1536329978773-2f9c5934628f",
            
            (Weather.Snow to TimeOfDay.DAWN) to "1516972810927-a9760618053b",
            (Weather.Snow to TimeOfDay.DAY) to "1478265409131-1f65c88f965c",
            (Weather.Snow to TimeOfDay.DUSK) to "1485600105115-440d49a3f3b2",
            (Weather.Snow to TimeOfDay.NIGHT) to "1486496572040-2003c21d6c53",
            
            (Weather.Fog to TimeOfDay.DAWN) to "1443694901371-29471f0c2394",
            (Weather.Fog to TimeOfDay.DAY) to "1487621167305-5d248087c724",
            (Weather.Fog to TimeOfDay.DUSK) to "1505322022379-7c3353ee0373",
            (Weather.Fog to TimeOfDay.NIGHT) to "1534125806660-f13880a2b0e3",
            
            (Weather.Storm to TimeOfDay.DAWN) to "1492014758011-57d22bf73d52",
            (Weather.Storm to TimeOfDay.DAY) to "1511289080750-b1d2d14145b8",
            (Weather.Storm to TimeOfDay.DUSK) to "1441974231531-c6227db76b6e",
            (Weather.Storm to TimeOfDay.NIGHT) to "1441974231531-c6227db76b6e"
        )
        
        Weather.all().forEach { w ->
            TimeOfDay.entries.forEach { t ->
                val id = images[w to t] ?: images[Weather.Clear to t] ?: "1506744038136-46273834b3fb"
                rules.add(PredefinedRule(w, t, "https://images.unsplash.com/photo-$id?q=80&w=1600&ar=9:16&fit=crop"))
            }
        }
        return rules
    }

    private fun createUrbanWeatherRules(): List<PredefinedRule> {
        val rules = mutableListOf<PredefinedRule>()
        val images = mapOf(
            (Weather.Clear to TimeOfDay.DAWN) to "1514565131-fce0801e5785",
            (Weather.Clear to TimeOfDay.DAY) to "1477959858617-67f85cf4f1df",
            (Weather.Clear to TimeOfDay.DUSK) to "1514924013411-cbf25faa35bb",
            (Weather.Clear to TimeOfDay.NIGHT) to "1496568844521-8ec823925274",
            
            (Weather.Cloudy to TimeOfDay.DAWN) to "1520140228032-7e239031d2e1",
            (Weather.Cloudy to TimeOfDay.DAY) to "1514924013411-cbf25faa35bb",
            (Weather.Cloudy to TimeOfDay.DUSK) to "1525230071285-8bb23a07600f",
            (Weather.Cloudy to TimeOfDay.NIGHT) to "1461397821037-c51ad11568c1",
            
            (Weather.Rain to TimeOfDay.DAWN) to "1520699697491-093a3a3a405a",
            (Weather.Rain to TimeOfDay.DAY) to "1428592953211-077101b2021b",
            (Weather.Rain to TimeOfDay.DUSK) to "1533038590605-2593d39c085b",
            (Weather.Rain to TimeOfDay.NIGHT) to "1534313313039-4fd92c20693a",
            
            (Weather.Snow to TimeOfDay.DAWN) to "1547437722-12f714b3d4b3",
            (Weather.Snow to TimeOfDay.DAY) to "1491002052546-bf38f186af56",
            (Weather.Snow to TimeOfDay.DUSK) to "1548654877-38038b320399",
            (Weather.Snow to TimeOfDay.NIGHT) to "1511289080750-b1d2d14145b8",
            
            (Weather.Fog to TimeOfDay.DAWN) to "1520551061730-681b898d970f",
            (Weather.Fog to TimeOfDay.DAY) to "1518391846015-55a9cc003b25",
            (Weather.Fog to TimeOfDay.DUSK) to "1505322022379-7c3353ee0373",
            (Weather.Fog to TimeOfDay.NIGHT) to "1511289080750-b1d2d14145b8",

            (Weather.Storm to TimeOfDay.DAWN) to "1511289080750-b1d2d14145b8",
            (Weather.Storm to TimeOfDay.DAY) to "1511289080750-b1d2d14145b8",
            (Weather.Storm to TimeOfDay.DUSK) to "1511289080750-b1d2d14145b8",
            (Weather.Storm to TimeOfDay.NIGHT) to "1511289080750-b1d2d14145b8"
        )
        
        Weather.all().forEach { w ->
            TimeOfDay.entries.forEach { t ->
                val id = images[w to t] ?: images[Weather.Clear to t] ?: "1514565131-fce0801e5785"
                rules.add(PredefinedRule(w, t, "https://images.unsplash.com/photo-$id?q=80&w=1600&ar=9:16&fit=crop"))
            }
        }
        return rules
    }
}
