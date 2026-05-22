package com.andyl.iris.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.andyl.iris.ui.screen.DynamicWallpaperScreen
import com.andyl.iris.ui.screen.WallpaperConfigScreen
import com.andyl.iris.ui.searchscreen.SearchScreen
import com.andyl.iris.ui.viewmodel.DynamicWallpaperViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AppNav() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") { backStackEntry ->
            val viewModel: DynamicWallpaperViewModel = koinViewModel(viewModelStoreOwner = backStackEntry)
            DynamicWallpaperScreen(
                viewModel,
                onNavigateToSettings = { navController.navigate("config") },
                onNavigateToSearch = { navController.navigate("search") }
            )
        }

        composable("search") { backStackEntry ->
            val mainEntry = remember(backStackEntry) { navController.getBackStackEntry("main") }
            val sharedViewModel: DynamicWallpaperViewModel = koinViewModel(viewModelStoreOwner = mainEntry)
            
            SearchScreen(
                viewModel = koinViewModel(parameters = { parametersOf(sharedViewModel) }),
                onNavigateHome = { navController.popBackStack() }
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