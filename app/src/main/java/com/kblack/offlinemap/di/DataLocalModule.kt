package com.kblack.offlinemap.di

import android.content.Context
import androidx.room.Room
import com.kblack.offlinemap.data.local.AppDatabase
import com.kblack.offlinemap.data.local.Migrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataLocalModule {

    @Provides
    @Singleton
    fun providesAppDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .addMigrations(Migrations.MIGRATION_1_2)
            .build()

    @Provides
    @Singleton
    fun providesPlaceSearchDao(appDatabase: AppDatabase) = appDatabase.placeSearchDao()

}