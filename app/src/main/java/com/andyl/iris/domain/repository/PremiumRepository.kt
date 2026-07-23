package com.andyl.iris.domain.repository

import com.andyl.iris.domain.model.ScaleMode
import kotlinx.coroutines.flow.Flow

interface PremiumRepository {
    fun isPremium(): Boolean
    fun setPremium(value: Boolean)
    fun getMaxCustomPacks(): Int
    fun isPackUnlocked(packId: String): Boolean
    fun isScaleModeUnlocked(mode: ScaleMode): Boolean
    fun isTargetUnlocked(target: Int): Boolean
    fun isSourceUnlocked(source: String): Boolean
    fun isTemperatureRulesEnabled(): Boolean
    fun observePremiumStatus(): Flow<Boolean>
}
