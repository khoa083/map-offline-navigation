package com.kblack.offlinemap.presentation.screen.overview.component

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberUserLocationState
import timber.log.Timber

@Immutable
data class MapLocationAccessState(
    val hasPermission: Boolean,
    val isLocationServiceOn: Boolean,
    val onLocationClick: () -> Unit,
)

private fun hasLocationPermission(ctx: Context): Boolean {
    val fine = ActivityCompat.checkSelfPermission(
        ctx, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ActivityCompat.checkSelfPermission(
        ctx, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

private fun isLocationServiceEnabled(ctx: Context): Boolean {
    val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        locationManager.isLocationEnabled
    } else {
        @Suppress("DEPRECATION")
        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        @Suppress("DEPRECATION")
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        gpsEnabled || networkEnabled
    }
}

private fun shouldOpenAppSettings(hostActivity: Activity): Boolean {
    val fineDenied = ActivityCompat.checkSelfPermission(
        hostActivity,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED
    val coarseDenied = ActivityCompat.checkSelfPermission(
        hostActivity,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED

    val finePermanentlyDenied = fineDenied && !ActivityCompat.shouldShowRequestPermissionRationale(
        hostActivity,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val coarsePermanentlyDenied = coarseDenied && !ActivityCompat.shouldShowRequestPermissionRationale(
        hostActivity,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    return finePermanentlyDenied || coarsePermanentlyDenied
}

private fun openAppSettings(context: Context) {
    context.startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )
}

private fun refreshLocationAccessState(
    context: Context,
    setHasPermission: (Boolean) -> Unit,
    setIsLocationServiceOn: (Boolean) -> Unit,
) {
    setHasPermission(hasLocationPermission(context))
    setIsLocationServiceOn(isLocationServiceEnabled(context))
}

private fun locationButtonClickAction(
    context: Context,
    permissionLauncher: ActivityResultLauncher<Array<String>>,
    locationSettingsLauncher: ActivityResultLauncher<Intent>,
    onLocationReady: () -> Unit,
): () -> Unit = {
    val liveHasPermission = hasLocationPermission(context)
    val liveIsLocationServiceOn = isLocationServiceEnabled(context)
    when {
        !liveHasPermission -> {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        !liveIsLocationServiceOn -> {
            locationSettingsLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }

        else -> onLocationReady()
    }
}

@Suppress("unused")
@Composable
fun rememberMapLocationAccessState(
    context: Context,
    onLocationReady: () -> Unit,
): MapLocationAccessState {
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember { mutableStateOf(hasLocationPermission(context)) }
    var isLocationServiceOn by remember { mutableStateOf(isLocationServiceEnabled(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        Timber.d("[CAPTURE] permission result = $result")
        refreshLocationAccessState(context, { hasPermission = it }, { isLocationServiceOn = it })
        if (!hasPermission && activity != null && shouldOpenAppSettings(activity)) {
            openAppSettings(context)
        }
    }

    val locationSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshLocationAccessState(context, { hasPermission = it }, { isLocationServiceOn = it })
        if (hasPermission && isLocationServiceOn) {
            onLocationReady()
        }
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshLocationAccessState(context, { hasPermission = it }, { isLocationServiceOn = it })
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(hasPermission, isLocationServiceOn) {
        if (hasPermission && isLocationServiceOn) {
            onLocationReady()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    return remember(hasPermission, isLocationServiceOn) {
        MapLocationAccessState(
            hasPermission = hasPermission,
            isLocationServiceOn = isLocationServiceOn,
            onLocationClick = locationButtonClickAction(
                context = context,
                permissionLauncher = permissionLauncher,
                locationSettingsLauncher = locationSettingsLauncher,
                onLocationReady = onLocationReady,
            )
        )
    }
}

@Suppress("unused")
@Composable
@SuppressLint("MissingPermission")
fun rememberMapLocationState(hasPermission: Boolean) =
    if (hasPermission) {
        val locationProvider = rememberDefaultLocationProvider()
        rememberUserLocationState(locationProvider)
    } else {
        null
    }
