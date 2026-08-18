package com.andyl.iris

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.ui.components.OnboardingScreen
import com.andyl.iris.ui.components.rememberBackgroundLocationConsent
import com.andyl.iris.ui.navigation.AppNav
import com.andyl.iris.ui.theme.IrisWallpaperTheme
import com.andyl.iris.ui.theme.ThemeManager
import com.andyl.iris.worker.IrisWallpaperScheduler
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userPreferences: UserPreferencesRepository = koinInject()
            val themeManager: ThemeManager = koinInject()
            val scope = rememberCoroutineScope()
            var onboardingCompleted by remember { mutableStateOf<Boolean?>(null) }
            val systemDark = isSystemInDarkTheme()

            LaunchedEffect(Unit) {
                onboardingCompleted = userPreferences.isOnboardingCompleted()
                if (onboardingCompleted == true) {
                    // Only run background wallpaper updates once the user has set up the app.
                    IrisWallpaperScheduler.schedule(applicationContext, userPreferences.getUpdateIntervalMinutes())
                }
                val saved = userPreferences.getDarkModePreference()
                themeManager.setDarkModePref(saved)
                themeManager.setAccentColor(userPreferences.getAccentColor())
                themeManager.setAmoledMode(userPreferences.getAmoledMode())
                themeManager.setReduceAnimations(userPreferences.getReduceAnimations())
                themeManager.setHapticsEnabled(userPreferences.getHapticsEnabled())
            }

            val darkModePref by themeManager.darkModePref.collectAsState()
            val accentColor by themeManager.accentColor.collectAsState()
            val amoledMode by themeManager.amoledMode.collectAsState()
            val reduceAnimations by themeManager.reduceAnimations.collectAsState()
            val isDarkMode = darkModePref ?: systemDark

            val view = LocalView.current
            SideEffect {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDarkMode
                controller.isAppearanceLightNavigationBars = !isDarkMode
            }

            IrisWallpaperTheme(
                darkTheme = isDarkMode,
                accentColor = accentColor,
                amoled = amoledMode,
                reduceAnimations = reduceAnimations
            ) {
                if (onboardingCompleted == null) {
                    // Loading state - show nothing while checking
                    return@IrisWallpaperTheme
                }

                if (onboardingCompleted == false) {
                    OnboardingScreen(
                        onFinish = {
                            scope.launch {
                                userPreferences.setOnboardingCompleted()
                                onboardingCompleted = true
                                IrisWallpaperScheduler.schedule(applicationContext, userPreferences.getUpdateIntervalMinutes())
                            }
                        }
                    )
                } else {
                    val requestBackgroundLocation = rememberBackgroundLocationConsent(userPreferences) { isGranted ->
                        Log.d("IrisPermissions", "Background location granted: $isGranted")
                    }

                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions()
                    ) { results ->
                        Log.d("IrisPermissions", "Permissions result: $results")
                        // Only request background location after a prominent disclosure
                        // (handled inside rememberBackgroundLocationConsent).
                        requestBackgroundLocation(false)
                    }

                    LaunchedEffect(Unit) {
                        val permissions = mutableListOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                        }

                        launcher.launch(permissions.toTypedArray())
                    }

                    AppNav()
                }
            }
        }
    }
}
