package com.kblack.offlinemap.data.remote.api.services

import retrofit2.http.GET

interface GhApiServices {
    @GET("v1/route")
    suspend fun getRoute(): String
}