package com.kblack.offlinemap.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class GhRetrofit

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class GhHttpClient

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PhotonRetrofit

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PhotonHttpClient