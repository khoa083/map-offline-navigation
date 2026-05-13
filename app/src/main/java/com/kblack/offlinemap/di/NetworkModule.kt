package com.kblack.offlinemap.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.kblack.offlinemap.data.remote.api.services.GhApiServices
import com.kblack.offlinemap.data.remote.api.services.PlaceApiServices
import com.kblack.offlinemap.data.utils.ApiUrl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        @ApplicationContext context: Context
    ): OkHttpClient =
        OkHttpClient.Builder()
            .readTimeout(20.toLong(), TimeUnit.SECONDS)
            .connectTimeout(20.toLong(), TimeUnit.SECONDS)
            .addInterceptor(ChuckerInterceptor(context))
            .addInterceptor(httpLoggingInterceptor)
            .build()

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    @GhRetrofit
    fun provideGhRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(ApiUrl.BASE_GITHUB_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    @PhotonRetrofit
    fun providePhotonRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(ApiUrl.BASE_PHOTON_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideGhApiServices(@GhRetrofit retrofit: Retrofit): GhApiServices =
        retrofit.create(GhApiServices::class.java)

    @Provides
    @Singleton
    fun providePlaceApiServices(@PhotonRetrofit retrofit: Retrofit): PlaceApiServices =
        retrofit.create(PlaceApiServices::class.java)

}