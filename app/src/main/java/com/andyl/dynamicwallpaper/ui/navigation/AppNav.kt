package com.andyl.dynamicwallpaper.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.andyl.dynamicwallpaper.ui.screen.DynamicWallpaperScreen
import com.andyl.dynamicwallpaper.ui.screen.WallpaperConfigScreen

@Composable
fun AppNav() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            DynamicWallpaperScreen(
                onNavigateToSettings = { navController.navigate("config") }
            )
        }
        composable("config") {
            WallpaperConfigScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}