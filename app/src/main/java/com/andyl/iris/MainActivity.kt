package com.andyl.iris

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.ui.components.OnboardingScreen
import com.andyl.iris.ui.navigation.AppNav
import com.andyl.iris.ui.theme.IrisWallpaperTheme
import com.andyl.iris.ui.theme.ThemeManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val userPreferences: UserPreferencesRepository = koinInject()
            val themeManager: ThemeManager = koinInject()
            val scope = rememberCoroutineScope()
            var onboardingCompleted by remember { mutableStateOf<Boolean?>(null) }
            val systemDark = isSystemInDarkTheme()

            LaunchedEffect(Unit) {
                onboardingCompleted = userPreferences.isOnboardingCompleted()
                val saved = userPreferences.getDarkModePreference()
                themeManager.setDarkMode(saved ?: systemDark)
            }

            val isDarkMode by themeManager.isDarkMode.collectAsState()

            IrisWallpaperTheme(darkTheme = isDarkMode) {
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
                            }
                        }
                    )
                } else {
                
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { results ->
                    Log.d("IrisPermissions", "Permissions result: $results")
                }

                val backgroundLocationLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    Log.d("IrisPermissions", "Background location granted: $isGranted")
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
                    
                    // Request Background Location separately (Android requirement)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        val hasBackground = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
                        
                        if (hasFine && !hasBackground) {
                            Log.d("IrisPermissions", "Requesting background location...")
                            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        }
                    }
                    
                    checkAndRequestIgnoreBatteryOptimizations()
                }

                AppNav()
                }
            }
        }
    }

    private fun checkAndRequestIgnoreBatteryOptimizations() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        val packageName = packageName
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            Log.d("IrisPermissions", "Requesting battery optimization exemption...")
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = "package:$packageName".toUri()
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("IrisPermissions", "Could not start battery optimization intent", e)
            }
        }
    }
}
