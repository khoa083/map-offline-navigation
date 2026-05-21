package com.kblack.offlinemap.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    const val DB_VERSION = 1

    // todo: Example
    val MIGRATION_1_2 = object : Migration(1,2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                ""
            )
        }
    }
}