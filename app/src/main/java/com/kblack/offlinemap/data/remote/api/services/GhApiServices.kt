package com.kblack.offlinemap.data.remote.api.services

import com.kblack.offlinemap.data.models.Config
import com.kblack.offlinemap.data.models.MapAllowlist
import com.kblack.offlinemap.data.utils.ApiUrl
import retrofit2.Response
import retrofit2.http.GET

interface GhApiServices {

    @GET(ApiUrl.ALLOWLIST_ENDPOINT)
    suspend fun getAllowlist(): Response<MapAllowlist>

    @GET(ApiUrl.CONFIG_ENDPOINT)
    suspend fun getConfig(): Response<Config>

}