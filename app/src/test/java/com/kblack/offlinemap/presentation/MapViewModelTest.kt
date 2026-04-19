package com.kblack.offlinemap.presentation

import app.cash.turbine.test
import com.kblack.offlinemap.domain.models.GeoCoordinate
import com.kblack.offlinemap.domain.models.Route
import com.kblack.offlinemap.domain.usecase.location.GetCurrentLocationUseCase
import com.kblack.offlinemap.domain.usecase.location.ObserveCurrentLocationUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.GetGraphPathUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.GetStyleJsonPathUseCase
import com.kblack.offlinemap.domain.usecase.routing.BuildNavigationUseCase
import com.kblack.offlinemap.domain.usecase.routing.CalculateRouteUseCase
import com.kblack.offlinemap.domain.usecase.routing.CloseRouterUseCase
import com.kblack.offlinemap.domain.usecase.routing.InitializeRouterUseCase
import com.kblack.offlinemap.presentation.viewmodel.MapViewModel
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    private val getStyleJsonPathUseCase: GetStyleJsonPathUseCase = mockk()
    private val getGraphPathUseCase: GetGraphPathUseCase = mockk()
    private val calculateRouteUseCase: CalculateRouteUseCase = mockk()
    private val initializeRouterUseCase: InitializeRouterUseCase = mockk()
    private val closeRouterUseCase: CloseRouterUseCase = mockk()
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase = mockk()
    private val observeCurrentLocationUseCase: ObserveCurrentLocationUseCase = mockk()
    private val buildNavigationUseCase: BuildNavigationUseCase = mockk()

    private lateinit var viewModel: MapViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val from = GeoCoordinate(21.067069, 105.804058)
    private val to   = GeoCoordinate(21.070380, 105.806746)

    private fun fakeRoute() = Route(
        distanceMeters = 500.0,
        durationMillis = 60_000L,
        points = listOf(from, to),
        instructions = emptyList(),
        speedDetails = emptyMap(),
        isDirectFallback = false,
        debugInfo = ""
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { closeRouterUseCase() } just runs
        viewModel = MapViewModel(
            getStyleJsonPathUseCase,
            getGraphPathUseCase,
            calculateRouteUseCase,
            initializeRouterUseCase,
            closeRouterUseCase,
            getCurrentLocationUseCase,
            observeCurrentLocationUseCase,
            buildNavigationUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `recalculateRoute should return an error message when routingReady = false`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.recalculateRoute()

            val errorMess = awaitItem()
            assertNotNull(errorMess.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }


}