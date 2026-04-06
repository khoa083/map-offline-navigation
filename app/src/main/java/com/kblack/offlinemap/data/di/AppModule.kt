package com.kblack.offlinemap.data.di

import android.content.Context
import androidx.work.WorkManager
import com.kblack.offlinemap.data.remote.api.MapListRemoteDataSource
import com.kblack.offlinemap.data.repository.AppLifecycleProviderImpl
import com.kblack.offlinemap.data.repository.LocationRepositoryImpl
import com.kblack.offlinemap.data.repository.MapDownloadRepositoryImpl
import com.kblack.offlinemap.data.repository.RoutingRepositoryImpl
import com.kblack.offlinemap.domain.repository.AppLifecycleProvider
import com.kblack.offlinemap.domain.repository.LocationRepository
import com.kblack.offlinemap.domain.repository.MapDownloadRepository
import com.kblack.offlinemap.domain.repository.RoutingRepository
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
    fun provideMapListRemoteDataSource(): MapListRemoteDataSource {
        return MapListRemoteDataSource()
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

}
