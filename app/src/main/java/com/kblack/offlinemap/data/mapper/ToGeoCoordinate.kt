package com.kblack.offlinemap.data.mapper

import android.location.Location
import com.kblack.offlinemap.domain.models.GeoCoordinate

fun Location.toDomain(): GeoCoordinate = GeoCoordinate(
    latitude = latitude,
    longitude = longitude,
    accuracy = if (hasAccuracy()) accuracy else 0f,
    bearing = if (hasBearing()) bearing else null,
    bearingAccuracyDegrees = if (hasBearingAccuracy()) bearingAccuracyDegrees else null,
    altitude = if (hasAltitude()) altitude else null,
    speed = if (hasSpeed()) speed else null,
    time = time
)