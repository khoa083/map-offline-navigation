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
    suspend fun upsertAllPlaceSearch(placeSearchEntity: List<PlaceSearchEntity>)

    @Query("SELECT * FROM place_search_entity WHERE osmId = :id LIMIT 1")
    suspend fun getOsmId(id: String): PlaceSearchEntity?

    //todo: Consider using FTS instead of LIKE.
    @Query(
        """
    SELECT * FROM place_search_entity
        WHERE searchVector LIKE :pattern ESCAPE '\'
        ORDER BY name ASC
        LIMIT :limit
    """
    )
    fun queryPlaceDatabase(pattern: String, limit: Int): Flow<List<PlaceSearchEntity>>

    @Delete
    suspend fun deletePlaceSearch(placeSearchEntity: PlaceSearchEntity)

    @Query("DELETE FROM place_search_entity")
    suspend fun deleteAllPlaceSearch()
}