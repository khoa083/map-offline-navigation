package com.kblack.offlinemap.data.repository

import com.kblack.offlinemap.data.mapper.toDomain
import com.kblack.offlinemap.data.remote.api.PlaceRemoteDataSource
import com.kblack.offlinemap.models.PlaceSearch

interface PlaceSearchRepository {
    suspend fun searchPlaces(query: String, limit: Int): List<PlaceSearch>?
}

class PlaceSearchRepositoryImpl(
    private val placeRemoteDataSource: PlaceRemoteDataSource
): PlaceSearchRepository {
    override suspend fun searchPlaces(
        query: String,
        limit: Int
    ): List<PlaceSearch>? {
        val place = placeRemoteDataSource.searchPlaces(query, limit)
        return place?.features?.map { it.toDomain() }

    }
}