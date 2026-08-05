package com.andyl.iris.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ThemeManager {
    // null = follow system
    private val _darkModePref = MutableStateFlow<Boolean?>(null)
    val darkModePref: StateFlow<Boolean?> = _darkModePref

    private val _accentColor = MutableStateFlow<String?>(null)
    val accentColor: StateFlow<String?> = _accentColor

    private val _amoledMode = MutableStateFlow(false)
    val amoledMode: StateFlow<Boolean> = _amoledMode

    private val _reduceAnimations = MutableStateFlow(false)
    val reduceAnimations: StateFlow<Boolean> = _reduceAnimations

    private val _hapticsEnabled = MutableStateFlow(true)
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled

    fun setDarkModePref(pref: Boolean?) {
        _darkModePref.value = pref
    }

    fun setAccentColor(color: String?) {
        _accentColor.value = color
    }

    fun setAmoledMode(enabled: Boolean) {
        _amoledMode.value = enabled
    }

    fun setReduceAnimations(enabled: Boolean) {
        _reduceAnimations.value = enabled
    }

    fun setHapticsEnabled(enabled: Boolean) {
        _hapticsEnabled.value = enabled
    }
}
