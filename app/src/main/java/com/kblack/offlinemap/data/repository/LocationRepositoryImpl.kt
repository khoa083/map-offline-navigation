package com.kblack.offlinemap.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.kblack.offlinemap.data.mapper.toDomain
import com.kblack.offlinemap.domain.models.GeoCoordinate
import com.kblack.offlinemap.domain.repository.LocationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


class LocationRepositoryImpl(
    appContext: Context
) : LocationRepository {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(appContext)

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): GeoCoordinate? {
        return try {
            val currentLocation = readLastKnownLocation() ?: readFreshLocation()
            currentLocation?.toDomain()
        } catch (_: SecurityException) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    override fun observeCurrentLocation(intervalMs: Long): Flow<GeoCoordinate> = callbackFlow {
        val minTimeMs = if (intervalMs < 0L) 0L else intervalMs
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, minTimeMs)
            .setMinUpdateIntervalMillis(minTimeMs)
            .setWaitForAccurateLocation(false)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    trySend(location.toDomain())
                }
            }
        }

        try {
            fusedLocationClient
                .requestLocationUpdates(request, callback, Looper.getMainLooper())
                .addOnFailureListener { throwable -> close(throwable) }

            readLastKnownLocation()?.let {
                trySend(it.toDomain())
            }
        } catch (_: SecurityException) {
            close()
        } catch (_: IllegalStateException) {
            close()
        }

        awaitClose {
            try {
                fusedLocationClient.removeLocationUpdates(callback)
            } catch (_: SecurityException) {
                null
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun readFreshLocation(): Location? {
        val tokenSource = CancellationTokenSource()
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            .setDurationMillis(10_000L)
            .setMaxUpdateAgeMillis(5_000L)
            .build()

        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { tokenSource.cancel() }

            fusedLocationClient
                .getCurrentLocation(request, tokenSource.token)
                .addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
                .addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
                .addOnCanceledListener { if (continuation.isActive) continuation.resume(null) }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun readLastKnownLocation(): Location? {
        return try {
            fusedLocationClient.lastLocation.awaitOrNull()
        } catch (_: SecurityException) {
            null
        }
    }

    private suspend fun <T> Task<T>.awaitOrNull(): T? {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
            addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
            addOnCanceledListener { if (continuation.isActive) continuation.resume(null) }
        }
    }
}