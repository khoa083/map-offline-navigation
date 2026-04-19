package com.kblack.offlinemap.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import com.kblack.offlinemap.domain.models.GeoCoordinate
import com.kblack.offlinemap.domain.repository.LocationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

class LocationRepositoryImpl(
    appContext: Context
): LocationRepository {
    private val locationManager: LocationManager =
        appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): GeoCoordinate? {
        val last = readBestLastKnownLocation() ?: return null
        return GeoCoordinate(last.latitude, last.longitude)
    }

    @SuppressLint("MissingPermission")
    override fun observeCurrentLocation(intervalMs: Long): Flow<GeoCoordinate> = callbackFlow {
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trySend(GeoCoordinate(location.latitude, location.longitude))
            }

            override fun onProviderEnabled(provider: String) {
                Timber.d("onProviderEnabled")
            }

            override fun onProviderDisabled(provider: String) {
                Timber.d("onProviderDisabled")
            }
        }

        val minTimeMs = if (intervalMs < 0L) 0L else intervalMs

        try {
            val providers = arrayOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )

            for (provider in providers) {
                try {
                    if (locationManager.isProviderEnabled(provider)) {
                        locationManager.requestLocationUpdates(provider, minTimeMs, 0f, listener)
                    }
                } catch (_: IllegalArgumentException) {}
            }

            readBestLastKnownLocation()?.let {
                trySend(GeoCoordinate(it.latitude, it.longitude))
            }
        } catch (_: SecurityException) {
            close()
        } catch (_: IllegalArgumentException) {
            close()
        }

        awaitClose {
            try {
                locationManager.removeUpdates(listener)
            } catch (_: SecurityException) {null}
        }
    }

    @SuppressLint("MissingPermission")
    private fun readBestLastKnownLocation(): Location? {
        val candidates = arrayOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        var best: Location? = null
        for (provider in candidates) {
            val loc = try {
                locationManager.getLastKnownLocation(provider)
            } catch (_: SecurityException) {
                null
            } catch (_: IllegalArgumentException) {
                null
            }

            if (loc != null && (best == null || loc.time > best.time)) {
                best = loc
            }
        }
        return best
    }
}