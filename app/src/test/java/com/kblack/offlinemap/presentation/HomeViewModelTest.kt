package com.kblack.offlinemap.presentation

import app.cash.turbine.test
import com.kblack.offlinemap.domain.models.MapDownloadStatus
import com.kblack.offlinemap.domain.models.MapDownloadStatusType
import com.kblack.offlinemap.domain.models.MapModel
import com.kblack.offlinemap.domain.usecase.mapallowlist.GetMapUrlResponseUseCase
import com.kblack.offlinemap.domain.usecase.mapallowlist.LoadMapAllowlistUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.CancelAllUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.CancelDownloadMapUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.DeleteMapUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.DownloadMapUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.GetLocalMapStatusUseCase
import com.kblack.offlinemap.presentation.viewmodel.HomeViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val loadMapAllowlistUseCase: LoadMapAllowlistUseCase = mockk()
    private val getMapUrlResponseUseCase: GetMapUrlResponseUseCase = mockk()
    private val getLocalMapStatusUseCase: GetLocalMapStatusUseCase = mockk()
    private val deleteMapUseCase: DeleteMapUseCase = mockk()
    private val downloadMapUseCase: DownloadMapUseCase = mockk()
    private val cancelDownloadMapUseCase: CancelDownloadMapUseCase = mockk()
    private val cancelAllUseCase: CancelAllUseCase = mockk()

    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val mockMap = MapModel(mapId = "vn", name = "Vietnam", url = "https://test/vn.tar.zst")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { cancelAllUseCase(any()) } answers {
            firstArg<() -> Unit>().invoke()
        }

        viewModel = HomeViewModel(
            loadMapAllowlistUseCase,
            getMapUrlResponseUseCase,
            getLocalMapStatusUseCase,
            deleteMapUseCase,
            downloadMapUseCase,
            cancelDownloadMapUseCase,
            cancelAllUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadMapAllowlist should success updates state with maps`() = runTest {
        coEvery { loadMapAllowlistUseCase() } returns listOf(mockMap)
        every { getLocalMapStatusUseCase(any()) } returns MapDownloadStatus(MapDownloadStatusType.SUCCEEDED)

        viewModel.uiState.test {
            val init = awaitItem()
            assertEquals(true, init.loadingMapAllowlist)

            viewModel.loadMapAllowlist()

            val successState = awaitItem()
            assertEquals(false, successState.loadingMapAllowlist)
            assertEquals(listOf(mockMap), successState.maps)
            assertEquals(null, successState.loadingMapAllowlistError)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadMapAllowlist should show error when maps is null`() = runTest {
        coEvery { loadMapAllowlistUseCase() } returns null

        viewModel.uiState.test {
            val init = awaitItem()
            assertEquals(true, init.loadingMapAllowlist)

            viewModel.loadMapAllowlist()

            val errorState = awaitItem()
            assertEquals(false, errorState.loadingMapAllowlist)

            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }
    }

}