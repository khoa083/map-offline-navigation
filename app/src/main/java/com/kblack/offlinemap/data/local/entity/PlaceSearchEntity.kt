package com.kblack.offlinemap.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "place_search_entity")
data class PlaceSearchEntity(
    @PrimaryKey
    val osmId: Long = 0L,

    val osmValue: String = "",
    val name: String = "",
    val street: String = "",
    val locality: String = "",
    val district: String = "",
    val city: String = "",
    val state: String = "",
    val country: String = "",
    @ColumnInfo(index = true)
    val searchVector: String = "",
    val postcode: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
)
