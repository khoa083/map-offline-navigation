package com.kblack.offlinemap.data.remote.api

import com.google.gson.Gson
import com.kblack.offlinemap.data.model.MapAllowlist
import java.net.HttpURLConnection
import java.net.URL

// todo: FIXME - Currently, this simple case only retrieves data from GitHub, configuring Retrofit is not necessary. I will modify it later.
class MapListRemoteDataSource {
    fun fetchMapAllowlist(url: String): MapAllowlist? {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Gson().fromJson(response, MapAllowlist::class.java)
            } else { null }
        } catch (e: Exception) {
            null
        }
    }

    fun getUrlResponseCode(url: String): Int {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()
            connection.responseCode
        } catch (_: Exception) {
            -1
        }
    }
}