package com.kblack.offlinemap.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.kblack.offlinemap.BuildConfig
import com.kblack.offlinemap.data.remote.api.interceptor.PhotonInterceptor
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
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    @Provides
    @Singleton
    fun providePhotonInterceptor(): PhotonInterceptor = PhotonInterceptor()

    @Provides
    @Singleton
    fun provideChuckerInterceptor(@ApplicationContext context: Context): ChuckerInterceptor {
        return ChuckerInterceptor.Builder(context)
            .build()
    }

    @Provides
    @Singleton
    @PhotonHttpClient
    fun providePhotonOkHttpClient(
        photonInterceptor: PhotonInterceptor,
        httpLoggingInterceptor: HttpLoggingInterceptor,
        chuckerInterceptor: ChuckerInterceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .readTimeout(20.toLong(), TimeUnit.SECONDS)
            .connectTimeout(20.toLong(), TimeUnit.SECONDS)
            .addInterceptor(photonInterceptor)
            .addInterceptor(chuckerInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .build()

    @Provides
    @Singleton
    @GhHttpClient
    fun provideGithubOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        chuckerInterceptor: ChuckerInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .readTimeout(20.toLong(), TimeUnit.SECONDS)
            .connectTimeout(20.toLong(), TimeUnit.SECONDS)
            .addInterceptor(chuckerInterceptor)
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
        @GhHttpClient okHttpClient: OkHttpClient,
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
        @PhotonHttpClient okHttpClient: OkHttpClient,
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