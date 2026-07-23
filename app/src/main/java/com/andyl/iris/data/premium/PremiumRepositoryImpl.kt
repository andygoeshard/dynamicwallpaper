package com.andyl.iris.data.premium

import android.content.Context
import android.util.Log
import com.andyl.iris.billing.BillingManager
import com.andyl.iris.domain.model.ScaleMode
import com.andyl.iris.domain.repository.PremiumRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PremiumRepositoryImpl(
    context: Context,
    private val billingManager: BillingManager
) : PremiumRepository {

    private val prefs = context.getSharedPreferences("premium_prefs", Context.MODE_PRIVATE)
    private val _premiumFlow = MutableStateFlow(prefs.getBoolean(KEY_IS_PREMIUM, false))

    init {
        CoroutineScope(Dispatchers.IO).launch {
            billingManager.activePurchases.collectLatest { purchases ->
                val hasActive = purchases.any {
                    it.purchaseState == com.android.billingclient.api.Purchase.PurchaseState.PURCHASED &&
                    it.isAcknowledged
                }
                if (hasActive != _premiumFlow.value) {
                    setPremium(hasActive)
                    Log.d("PREMIUM_REPO", "Premium status synced: $hasActive")
                }
            }
        }
    }

    companion object {
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val MAX_CUSTOM_PACKS_FREE = 3
        private const val MAX_CUSTOM_PACKS_PREMIUM = 10

        private val FREE_PACK_IDS = setOf(
            "nature_weather",
            "argentina_weather",
            "cats_weather",
            "urban_weather",
            "anime_weather",
            "minimal_weather",
            "women_empowered",
            "cyber_samurai",
            "golden_hour",
            "ocean_waves",
            "cherry_blossom",
            "northern_lights",
            "coffee_vibes",
            "argentina_weekly",
            "kids_weekly",
            "sunsets_weekly",
            "puppies_weekly",
            "space_weekly",
            "food_weekly",
            "sports_weekly",
            "argentina_glory",
            "city_lights_time",
            "ocean_tides_time",
            "forest_moods_time",
            "desert_dreams_time",
            "mountain_dawn_time",
            "seleccion_argentina_random",
            "snow_city_temp",
            "rain_window_temp",
            "breeze_fields_temp"
        )
    }

    override fun isPremium(): Boolean {
        if (com.andyl.iris.BuildConfig.DEBUG) return true
        return prefs.getBoolean(KEY_IS_PREMIUM, false)
    }

    override fun setPremium(value: Boolean) {
        prefs.edit().putBoolean(KEY_IS_PREMIUM, value).apply()
        _premiumFlow.value = value
    }

    override fun getMaxCustomPacks(): Int {
        return if (isPremium()) MAX_CUSTOM_PACKS_PREMIUM else MAX_CUSTOM_PACKS_FREE
    }

    override fun isPackUnlocked(packId: String): Boolean {
        if (isPremium()) return true
        return packId in FREE_PACK_IDS
    }

    override fun isScaleModeUnlocked(mode: ScaleMode): Boolean {
        if (isPremium()) return true
        return mode == ScaleMode.CROP
    }

    override fun isTargetUnlocked(target: Int): Boolean {
        if (isPremium()) return true
        return target == 1
    }

    override fun isSourceUnlocked(source: String): Boolean {
        if (isPremium()) return true
        return source == "unsplash"
    }

    override fun isTemperatureRulesEnabled(): Boolean {
        return isPremium()
    }

    override fun observePremiumStatus(): Flow<Boolean> {
        return _premiumFlow.map { it }
    }
}
