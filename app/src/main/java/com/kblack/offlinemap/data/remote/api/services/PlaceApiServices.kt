package com.kblack.offlinemap.data.remote.api.services

import com.kblack.offlinemap.data.models.GeoLocation
import com.kblack.offlinemap.data.utils.ApiUrl
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceApiServices {

    @GET(ApiUrl.PHOTON_SEARCH_ENDPOINT)
    suspend fun searchPlace(
        @Query("q") query: String,
        @Query("limit") limit: Int? = 1,
    ): Response<GeoLocation>

}