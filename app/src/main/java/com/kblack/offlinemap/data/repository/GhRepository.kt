package com.kblack.offlinemap.data.repository

import android.content.Context
import com.kblack.offlinemap.data.mapper.toDomain
import com.kblack.offlinemap.data.models.Config
import com.kblack.offlinemap.data.models.MapAllowlist
import com.kblack.offlinemap.data.remote.api.GhRemoteDataSource
import com.kblack.offlinemap.models.MapModel
import com.squareup.moshi.Moshi
import java.io.File

interface GhRepository {
    suspend fun loadMapAllowlist(): List<MapModel>?
    suspend fun getConfig(): Config?
    fun getMapUrlResponse(url: String): Int
}

class GhRepositoryImpl(
    private val remoteDataSource: GhRemoteDataSource,
    private val context: Context,
    private val moshi: Moshi,
) : GhRepository {

    private val allowlistFileName = "map_allowlist.json"
    private val externalFilesDir = context.getExternalFilesDir(null)
    private val mapAllowlistAdapter = moshi.adapter(MapAllowlist::class.java)

    override suspend fun loadMapAllowlist(): List<MapModel>? {
        var mapAllowlist = remoteDataSource.getMapAllowlist()

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

    override suspend fun getConfig(): Config? = remoteDataSource.getConfig()

    override fun getMapUrlResponse(url: String): Int {
        return remoteDataSource.getUrlResponseCode(url)
    }

    private fun saveMapAllowlistToDisk(allowlist: MapAllowlist) {
        try {
            val file = File(externalFilesDir, allowlistFileName)
            file.writeText(mapAllowlistAdapter.toJson(allowlist))
        } catch (_: Exception) {}
    }

    private fun readMapAllowlistFromDisk(): MapAllowlist? {
        return try {
            val file = File(externalFilesDir, allowlistFileName)
            if (file.exists()) mapAllowlistAdapter.fromJson(file.readText()) else null
        } catch (_: Exception) {
            null
        }
    }

    private fun readMapAllowlistFromAssets(): MapAllowlist? {
        return try {
            val content = context.assets.open(allowlistFileName).bufferedReader().readText()
            mapAllowlistAdapter.fromJson(content)
        } catch (_: Exception) {
            null
        }
    }
}

