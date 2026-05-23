package com.kblack.offlinemap.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.kblack.offlinemap.data.local.entity.PlaceSearchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceSearchDao {

    @Query("SELECT * FROM place_search_entity")
    fun getAllPlaceSearch(): Flow<List<PlaceSearchEntity>>

    @Upsert
    suspend fun upsertPlaceSearch(placeSearchEntity: PlaceSearchEntity)

    @Query("SELECT * FROM place_search_entity WHERE osmId = :id LIMIT 1")
    suspend fun getOsmId(id: String): PlaceSearchEntity?

    @Query("""
    SELECT * FROM place_search_entity
    WHERE name LIKE :pattern OR city LIKE :pattern OR district LIKE :pattern OR country LIKE :pattern
    ORDER BY name ASC
    LIMIT :limit
    """)
    fun searchLikeFlow(pattern: String, limit: Int): Flow<List<PlaceSearchEntity>>

    @Delete
    suspend fun deletePlaceSearch(placeSearchEntity: PlaceSearchEntity)

    @Query("DELETE FROM place_search_entity")
    suspend fun deleteAllPlaceSearch()
}