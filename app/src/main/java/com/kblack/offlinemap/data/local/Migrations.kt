package com.kblack.offlinemap.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    const val DB_VERSION = 2

    val MIGRATION_1_2 = object : Migration(1,2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE place_search_entity ADD COLUMN searchVector TEXT NOT NULL DEFAULT ''"
            )
            db.execSQL(
                """
                UPDATE place_search_entity 
                SET searchVector = lower(
                    name || ' ' || 
                    coalesce(city, '') || ' ' || 
                    coalesce(state, '') || ' ' || 
                    coalesce(country, '')
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_place_search_entity_search_vector ON place_search_entity(searchVector)")
        }
    }
}