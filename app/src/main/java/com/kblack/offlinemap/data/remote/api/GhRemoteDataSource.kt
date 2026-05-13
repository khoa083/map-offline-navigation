package com.kblack.offlinemap.data.remote.api

import com.kblack.offlinemap.data.models.Config
import com.kblack.offlinemap.data.models.MapAllowlist
import com.kblack.offlinemap.data.remote.api.services.GhApiServices
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class GhRemoteDataSource @Inject constructor(
    private val ghApiServices: GhApiServices
) {
    suspend fun getMapAllowlist(): MapAllowlist? =
        try {
            ghApiServices.getAllowlist().body()
        } catch (_: Exception) {
            null
        }

    suspend fun getConfig(): Config? =
        try {
            ghApiServices.getConfig().body()
        } catch (_: Exception) {
            null
        }

    fun getUrlResponseCode(url: String): Int =
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()
            connection.responseCode
        } catch (_: Exception) {
            -1
        }
}