package com.kblack.offlinemap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kblack.offlinemap.data.local.dao.PlaceSearchDao
import com.kblack.offlinemap.data.local.entity.PlaceSearchEntity

@Database(
    entities = [
        PlaceSearchEntity::class
    ],
    version = Migrations.DB_VERSION,
    exportSchema = false
)

abstract class AppDatabase: RoomDatabase()  {

    abstract fun placeSearchDao(): PlaceSearchDao

    companion object {
        const val DATABASE_NAME = "kblack_offline_map"
    }

}