package com.kblack.offlinemap.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kblack.offlinemap.ui.screen.home.HomeScreen
import com.kblack.offlinemap.ui.viewmodel.HomeViewModel
import com.kblack.offlinemap.ui.screen.overview.MapViewScreen

@Composable
fun MapNavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Home.route
    ) {
        composable(route = Home.route) {
            HomeScreen(
                onClickMapView = { mapModel ->
                    navController.navigate(MapView.createRoute(mapModel.mapId))
                },
                homeViewModel
            )
        }
        composable(
            route = MapView.route,
            arguments = listOf(navArgument("mapId") { type = NavType.StringType }))
        { backStackEntry ->
            val mapId = backStackEntry.arguments?.getString("mapId") ?: return@composable
            val uiState by homeViewModel.uiState.collectAsState()
            val map = uiState.maps.find { it.mapId == mapId } ?: return@composable
            MapViewScreen(map)
        }
    }

}