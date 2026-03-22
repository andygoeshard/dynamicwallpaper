package com.andyl.iris.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.andyl.iris.ui.screen.DynamicWallpaperScreen
import com.andyl.iris.ui.screen.WallpaperConfigScreen
import com.andyl.iris.ui.viewmodel.DynamicWallpaperViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNav() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {backStackEntry ->
            val viewModel: DynamicWallpaperViewModel = koinViewModel(viewModelStoreOwner = backStackEntry)
            DynamicWallpaperScreen(
                viewModel, onNavigateToSettings = { navController.navigate("config") }
            )
        }
        composable("config") {
            val parentEntry = remember(it) { navController.getBackStackEntry("main") }
            val viewModel: DynamicWallpaperViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
            WallpaperConfigScreen(
                viewModel,onBack = { navController.popBackStack() }
            )
        }
    }
}