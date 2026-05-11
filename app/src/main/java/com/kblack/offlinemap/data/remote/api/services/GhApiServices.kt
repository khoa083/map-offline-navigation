package com.kblack.offlinemap.data.remote.api.services

import com.kblack.offlinemap.data.utils.ApiUrl
import retrofit2.http.GET

interface GhApiServices {

    @GET(ApiUrl.CONFIG_ENDPOINT)
    suspend fun getConfig(): String

}