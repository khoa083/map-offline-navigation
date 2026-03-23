package com.kblack.offlinemap.presentation.navigation

sealed interface MapDestination {
    val route: String
}

data object Home : MapDestination {
    override val route: String = "home"
}

data object MapView : MapDestination {
    override val route: String = "map_view/{mapId}"
    fun createRoute(mapId: String) = "map_view/$mapId"
}
