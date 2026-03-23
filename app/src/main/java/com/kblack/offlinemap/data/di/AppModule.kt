package com.kblack.offlinemap.data.di

import android.content.Context
import com.kblack.offlinemap.data.remote.api.MapListRemoteDataSource
import com.kblack.offlinemap.data.repository.AppLifecycleProviderImpl
import com.kblack.offlinemap.data.repository.MapDownloadRepositoryImpl
import com.kblack.offlinemap.domain.repository.AppLifecycleProvider
import com.kblack.offlinemap.domain.repository.MapDownloadRepository
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
    fun provideMapDownloadRepository(
        @ApplicationContext context: Context,
        lifecycleProvider: AppLifecycleProvider,
    ): MapDownloadRepository {
        return MapDownloadRepositoryImpl(context, lifecycleProvider)
    }

}