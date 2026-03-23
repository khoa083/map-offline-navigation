package com.kblack.offlinemap.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kblack.offlinemap.presentation.screen.home.HomeScreen
import com.kblack.offlinemap.presentation.screen.home.HomeViewModel
import com.kblack.offlinemap.presentation.screen.overview.MapViewScreen

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
            MapViewScreen(map,homeViewModel)
        }
    }

}