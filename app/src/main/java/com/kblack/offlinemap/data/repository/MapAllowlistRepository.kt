package com.kblack.offlinemap.data.repository

import android.content.Context
import com.google.gson.Gson
import com.kblack.offlinemap.data.mapper.toDomain
import com.kblack.offlinemap.data.models.MapAllowlist
import com.kblack.offlinemap.data.remote.api.MapListRemoteDataSource
import com.kblack.offlinemap.data.utils.Constant.ALLOWLIST_URL
import com.kblack.offlinemap.models.MapModel

interface MapAllowlistRepository {
    suspend fun loadMapAllowlist(url: String = ALLOWLIST_URL): List<MapModel>?
    fun getMapUrlResponse(url: String): Int
}

class MapAllowlistRepositoryImpl(
    private val remoteDataSource: MapListRemoteDataSource,
    private val context: Context,
) : MapAllowlistRepository {

    private val allowlistFileName = "map_allowlist.json"
    private val externalFilesDir = context.getExternalFilesDir(null)

    override suspend fun loadMapAllowlist(url: String): List<MapModel>? {
        var mapAllowlist = remoteDataSource.fetchMapAllowlist(url)

        if (mapAllowlist == null) {
            mapAllowlist = readMapAllowlistFromDisk()
        } else {
            saveMapAllowlistToDisk(mapAllowlist)
        }

        if (mapAllowlist == null) {
            mapAllowlist = readMapAllowlistFromAssets()
        }

        return mapAllowlist?.maps?.map { it.toDomain() }
    }

    override fun getMapUrlResponse(url: String): Int {
        return remoteDataSource.getUrlResponseCode(url)
    }

    private fun saveMapAllowlistToDisk(allowlist: MapAllowlist) {
        try {
            val file = java.io.File(externalFilesDir, allowlistFileName)
            file.writeText(Gson().toJson(allowlist))
        } catch (_: Exception) {
        }
    }

    private fun readMapAllowlistFromDisk(): MapAllowlist? {
        return try {
            val file = java.io.File(externalFilesDir, allowlistFileName)
            if (file.exists()) Gson().fromJson(file.readText(), MapAllowlist::class.java) else null
        } catch (_: Exception) {
            null
        }
    }

    private fun readMapAllowlistFromAssets(): MapAllowlist? {
        return try {
            val content = context.assets.open(allowlistFileName).bufferedReader().readText()
            Gson().fromJson(content, MapAllowlist::class.java)
        } catch (_: Exception) {
            null
        }
    }
}

