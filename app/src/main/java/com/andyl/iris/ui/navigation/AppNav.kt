package com.andyl.iris.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.andyl.iris.domain.mapper.*
import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.ui.screen.DynamicWallpaperScreen
import com.andyl.iris.ui.screen.WallpaperConfigScreen
import com.andyl.iris.ui.searchscreen.SearchScreen
import com.andyl.iris.ui.searchscreen.SearchViewModel
import com.andyl.iris.ui.searchscreen.WallpaperSlot
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
                onNavigateToSearch = { weather, time, day, fixed, label ->
                    val route = buildString {
                        append("search")
                        val params = mutableListOf<String>()
                        weather?.let { params.add("weather=${it.toKey()}") }
                        time?.let { params.add("time=${it.name}") }
                        day?.let { params.add("day=$it") }
                        fixed?.let { params.add("fixed=$it") }
                        label?.let { params.add("label=$it") }
                        if (params.isNotEmpty()) {
                            append("?")
                            append(params.joinToString("&"))
                        }
                    }
                    navController.navigate(route)
                }
            )
        }

        composable(
            route = "search?weather={weather}&time={time}&day={day}&fixed={fixed}&label={label}",
            arguments = listOf(
                navArgument("weather") { nullable = true; defaultValue = null },
                navArgument("time") { nullable = true; defaultValue = null },
                navArgument("day") { nullable = true; defaultValue = null },
                navArgument("fixed") { nullable = true; defaultValue = null },
                navArgument("label") { nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val mainEntry = remember(backStackEntry) { navController.getBackStackEntry("main") }
            val sharedViewModel: DynamicWallpaperViewModel = koinViewModel(viewModelStoreOwner = mainEntry)
            
            val weatherArg = backStackEntry.arguments?.getString("weather")
            val timeArg = backStackEntry.arguments?.getString("time")
            val dayArg = backStackEntry.arguments?.getString("day")
            val fixedArg = backStackEntry.arguments?.getString("fixed")
            val labelArg = backStackEntry.arguments?.getString("label")

            val initialSlot = if (labelArg != null) {
                WallpaperSlot(
                    weather = weatherArg?.let { weatherFromKey(it.uppercase()) },
                    time = timeArg?.let { TimeOfDay.valueOf(it) },
                    dayName = dayArg,
                    fixedTime = fixedArg,
                    label = labelArg
                )
            } else null

            val searchViewModel: SearchViewModel = koinViewModel(parameters = { parametersOf(sharedViewModel) })
            
            LaunchedEffect(initialSlot) {
                if (initialSlot != null) {
                    searchViewModel.selectSlot(initialSlot)
                }
            }

            SearchScreen(
                viewModel = searchViewModel,
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
