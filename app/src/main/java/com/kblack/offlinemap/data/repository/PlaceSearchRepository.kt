package com.kblack.offlinemap.data.repository

import com.kblack.offlinemap.data.local.dao.PlaceSearchDao
import com.kblack.offlinemap.data.mapper.toPlaceSearch
import com.kblack.offlinemap.data.mapper.toPlaceSearchEntity
import com.kblack.offlinemap.data.remote.api.PlaceRemoteDataSource
import com.kblack.offlinemap.models.PlaceSearch
import com.kblack.offlinemap.utils.containsNonLatin
import com.kblack.offlinemap.utils.toGlobalSearchVector
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface PlaceSearchRepository {
    fun getPlaceFromRoom(query: String, limit: Int): Flow<List<PlaceSearch>>
    suspend fun searchPlaces(query: String, limit: Int): List<PlaceSearch>?
}

class PlaceSearchRepositoryImpl(
    private val placeRemoteDataSource: PlaceRemoteDataSource,
    private val placeSearchDao: PlaceSearchDao
): PlaceSearchRepository {
    override fun getPlaceFromRoom(
        query: String,
        limit: Int
    ) : Flow<List<PlaceSearch>> {
        return placeSearchDao.queryPlaceDatabase(pattern(query), limit)
            .map { entity ->
                entity.map { entity -> entity.toPlaceSearch() }
            }
    }

    //Offline-First/Online-first Architecture
    //Single Source of Truth (SSOT)
    override suspend fun searchPlaces(
        query: String,
        limit: Int
    ): List<PlaceSearch>? = withContext(IO) {
        val placeRemoteData = placeRemoteDataSource.searchPlaces(query, limit)
        val places = placeRemoteData?.features?.map { it.toPlaceSearch() }

        if (query.containsNonLatin()) {
            return@withContext places
        }

        if (places != null) {
            val entities = places.map { it.toPlaceSearchEntity() }
            placeSearchDao.upsertAllPlaceSearch(entities)
        }
        places
    }

    private fun pattern(query: String): String {
        val cleanQuery = query.toGlobalSearchVector()

        val escaped = cleanQuery
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
        return "%${escaped}%"
    }

}