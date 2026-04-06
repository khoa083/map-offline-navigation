package com.kblack.offlinemap.presentation.di

import com.kblack.offlinemap.domain.repository.LocationRepository
import com.kblack.offlinemap.domain.repository.MapAllowlistRepository
import com.kblack.offlinemap.domain.repository.MapDownloadRepository
import com.kblack.offlinemap.domain.repository.RoutingRepository
import com.kblack.offlinemap.domain.usecase.location.GetCurrentLocationUseCase
import com.kblack.offlinemap.domain.usecase.location.ObserveCurrentLocationUseCase
import com.kblack.offlinemap.domain.usecase.mapallowlist.LoadMapAllowlistUseCase
import com.kblack.offlinemap.domain.usecase.routing.CalculateRouteUseCase
import com.kblack.offlinemap.domain.usecase.routing.InitializeRouterUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.CancelAllUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.CancelDownloadMapUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.DeleteMapUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.DownloadMapUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.GetGraphPathUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.GetLocalMapStatusUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.GetStyleJsonPathUseCase
import com.kblack.offlinemap.domain.usecase.routing.BuildNavigationUseCase
import com.kblack.offlinemap.domain.usecase.routing.CloseRouterUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    @ViewModelScoped
    fun provideCalculateRouteUseCase(
        routingRepository: RoutingRepository
    ): CalculateRouteUseCase {
        return CalculateRouteUseCase(routingRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideInitializeRouterUseCase(
        routingRepository: RoutingRepository
    ): InitializeRouterUseCase {
        return InitializeRouterUseCase(routingRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideCloseRouterUseCase(
        routingRepository: RoutingRepository
    ): CloseRouterUseCase {
        return CloseRouterUseCase(routingRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideCancelAllUseCase(
        downloadMapRepository: MapDownloadRepository
    ): CancelAllUseCase {
        return CancelAllUseCase(downloadMapRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideCancelDownloadMapUseCase(
        downloadMapRepository: MapDownloadRepository
    ): CancelDownloadMapUseCase {
        return CancelDownloadMapUseCase(downloadMapRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideDownloadMapUseCase(
        downloadMapRepository: MapDownloadRepository
    ): DownloadMapUseCase {
        return DownloadMapUseCase(downloadMapRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetStyleJsonPath(
        downloadMapRepository: MapDownloadRepository
    ): GetStyleJsonPathUseCase {
        return GetStyleJsonPathUseCase(downloadMapRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetGraphPath(
        downloadMapRepository: MapDownloadRepository
    ): GetGraphPathUseCase {
        return GetGraphPathUseCase(downloadMapRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetCurrentLocation(
        locationRepository: LocationRepository
    ): GetCurrentLocationUseCase {
        return GetCurrentLocationUseCase(locationRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideObserveCurrentLocation(
        locationRepository: LocationRepository
    ): ObserveCurrentLocationUseCase {
        return ObserveCurrentLocationUseCase(locationRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideBuildNavigationUseCase(): BuildNavigationUseCase {
        return BuildNavigationUseCase()
    }

    @Provides
    @ViewModelScoped
    fun provideGetLocalMapStatusUseCase(
        downloadMapRepository: MapDownloadRepository,
    ): GetLocalMapStatusUseCase {
        return GetLocalMapStatusUseCase(downloadMapRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideDeleteLocalMapFilesUseCase(
        downloadMapRepository: MapDownloadRepository,
    ): DeleteMapUseCase {
        return DeleteMapUseCase(downloadMapRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideLoadMapAllowlistUseCase(
        mapAllowlistRepository: MapAllowlistRepository,
    ): LoadMapAllowlistUseCase {
        return LoadMapAllowlistUseCase(mapAllowlistRepository)
    }

}