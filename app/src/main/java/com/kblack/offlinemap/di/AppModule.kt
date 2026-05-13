package com.kblack.offlinemap.di

import android.content.Context
import androidx.work.WorkManager
import com.kblack.offlinemap.data.remote.api.GhRemoteDataSource
import com.kblack.offlinemap.data.remote.api.PlaceRemoteDataSource
import com.kblack.offlinemap.data.remote.api.services.GhApiServices
import com.kblack.offlinemap.data.remote.api.services.PlaceApiServices
import com.kblack.offlinemap.data.repository.AppLifecycleProviderImpl
import com.kblack.offlinemap.data.repository.LocationRepositoryImpl
import com.kblack.offlinemap.data.repository.MapDownloadRepositoryImpl
import com.kblack.offlinemap.data.repository.RoutingRepositoryImpl
import com.kblack.offlinemap.data.repository.AppLifecycleProvider
import com.kblack.offlinemap.data.repository.GhRepository
import com.kblack.offlinemap.data.repository.GhRepositoryImpl
import com.kblack.offlinemap.data.repository.IOFileRepository
import com.kblack.offlinemap.data.repository.IOFileRepositoryImpl
import com.kblack.offlinemap.data.repository.LocationRepository
import com.kblack.offlinemap.data.repository.MapDownloadRepository
import com.kblack.offlinemap.data.repository.PlaceSearchRepository
import com.kblack.offlinemap.data.repository.PlaceSearchRepositoryImpl
import com.kblack.offlinemap.data.repository.RoutingRepository
import com.kblack.offlinemap.usecase.routing.BuildNavigationUseCase
import com.kblack.offlinemap.usecase.routing.InitializeRouterUseCase
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppLifecycleProvider(): AppLifecycleProvider {
        return AppLifecycleProviderImpl()
    }

    @Provides
    @Singleton
    fun provideMapListRemoteDataSource(
        ghApiServices: GhApiServices
    ): GhRemoteDataSource {
        return GhRemoteDataSource(ghApiServices)
    }

    @Provides
    @Singleton
    fun providePlaceRemoteDataSource(
        placeApiServices: PlaceApiServices
    ): PlaceRemoteDataSource {
        return PlaceRemoteDataSource(placeApiServices)
    }

    @Provides
    @Singleton
    fun provideGhRepository(
        remoteDataSource: GhRemoteDataSource,
        @ApplicationContext context: Context,
    ): GhRepository {
        return GhRepositoryImpl(remoteDataSource, context)
    }

    @Provides
    @Singleton
    fun providePlaceSearchRepository(
        placeRemoteDataSource: PlaceRemoteDataSource
    ): PlaceSearchRepository {
        return PlaceSearchRepositoryImpl(placeRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideMapDownloadRepository(
        @ApplicationContext context: Context,
        lifecycleProvider: AppLifecycleProvider,
        workManager : WorkManager
    ): MapDownloadRepository {
        return MapDownloadRepositoryImpl(context, lifecycleProvider, workManager)
    }

    @Provides
    @Singleton
    fun provideLocationRepository(
        @ApplicationContext context: Context
    ): LocationRepository {
        return LocationRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideRoutingRepository(): RoutingRepository {
        return RoutingRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideIOFileRepository(
        @ApplicationContext context: Context,
        moshi: Moshi
    ): IOFileRepository {
        return IOFileRepositoryImpl(context, moshi)
    }

    @Provides
    @Singleton
    fun provideInitializeRouterUseCase(
        routingRepository: RoutingRepository
    ): InitializeRouterUseCase {
        return InitializeRouterUseCase(routingRepository)
    }

    @Provides
    @Singleton
    fun provideBuildNavigationUseCase(): BuildNavigationUseCase {
        return BuildNavigationUseCase()
    }

}
