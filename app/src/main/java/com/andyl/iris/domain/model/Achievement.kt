package com.andyl.iris.domain.model

import com.andyl.iris.R

enum class AchievementId {
    FIRST_CHANGE,
    CHANGES_10,
    CHANGES_50,
    CHANGES_100,
    DAYS_7,
    DAYS_30,
    WEATHER_3,
    WEATHER_ALL,
    PACKS_5,
    RULES_10
}

data class AchievementDefinition(
    val id: AchievementId,
    val emoji: String,
    val titleRes: Int,
    val descRes: Int,
    val target: Int
)

val AchievementDefinitions = listOf(
    AchievementDefinition(AchievementId.FIRST_CHANGE, "🌱", R.string.ach_first_change_title, R.string.ach_first_change_desc, 1),
    AchievementDefinition(AchievementId.CHANGES_10, "⚙️", R.string.ach_changes_10_title, R.string.ach_changes_10_desc, 10),
    AchievementDefinition(AchievementId.CHANGES_50, "⚡", R.string.ach_changes_50_title, R.string.ach_changes_50_desc, 50),
    AchievementDefinition(AchievementId.CHANGES_100, "🏆", R.string.ach_changes_100_title, R.string.ach_changes_100_desc, 100),
    AchievementDefinition(AchievementId.DAYS_7, "📅", R.string.ach_days_7_title, R.string.ach_days_7_desc, 7),
    AchievementDefinition(AchievementId.DAYS_30, "🗓️", R.string.ach_days_30_title, R.string.ach_days_30_desc, 30),
    AchievementDefinition(AchievementId.WEATHER_3, "🌦️", R.string.ach_weather_3_title, R.string.ach_weather_3_desc, 3),
    AchievementDefinition(AchievementId.WEATHER_ALL, "🌈", R.string.ach_weather_all_title, R.string.ach_weather_all_desc, 6),
    AchievementDefinition(AchievementId.PACKS_5, "🗃️", R.string.ach_packs_5_title, R.string.ach_packs_5_desc, 5),
    AchievementDefinition(AchievementId.RULES_10, "🧠", R.string.ach_rules_10_title, R.string.ach_rules_10_desc, 10)
)

data class Achievement(
    val definition: AchievementDefinition,
    val value: Int
) {
    val unlocked: Boolean get() = value >= definition.target
    val progress: Float get() = (value.toFloat() / definition.target).coerceIn(0f, 1f)
}
