package com.kblack.offlinemap.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.kblack.offlinemap.presentation.navigation.MapNavGraph
import com.kblack.offlinemap.presentation.viewmodel.HomeViewModel
import com.kblack.offlinemap.presentation.ui.theme.OfflinemapTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        homeViewModel.loadMapAllowlist()
        setContent {
            OfflinemapTheme {
                val navController = rememberNavController()
                MapNavGraph(
                    navController = navController,
                    homeViewModel = homeViewModel
                )
            }
        }
    }

}
