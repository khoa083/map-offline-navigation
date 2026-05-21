package com.kblack.offlinemap.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertPlaceSearch(placeSearchEntity: PlaceSearchEntity)

    @Delete
    suspend fun deletePlaceSearch(placeSearchEntity: PlaceSearchEntity)

    @Query("DELETE FROM place_search_entity")
    suspend fun deleteAllPlaceSearch()
}