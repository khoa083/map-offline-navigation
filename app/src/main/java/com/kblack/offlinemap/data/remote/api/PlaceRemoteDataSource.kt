package com.kblack.offlinemap.data.remote.api

import com.kblack.offlinemap.data.models.GeoLocation
import com.kblack.offlinemap.data.remote.api.services.PlaceApiServices
import javax.inject.Inject

class PlaceRemoteDataSource @Inject constructor(
    private val placeApiServices: PlaceApiServices
) {
    suspend fun searchPlaces(query: String, limit: Int): GeoLocation? =
        try {
            placeApiServices.searchPlace(query, limit).body()
        } catch (_: Exception) {
            null
        }
}