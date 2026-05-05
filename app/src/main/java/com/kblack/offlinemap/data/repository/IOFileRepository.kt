package com.kblack.offlinemap.data.repository

import android.content.Context
import com.kblack.offlinemap.data.models.DefaultLocation
import com.kblack.offlinemap.models.MapModel
import kotlinx.serialization.json.Json
import java.io.File

interface IOFileRepository {
    fun getStyleJsonPath(map: MapModel): String?
    fun getGraphPath(map: MapModel): String?
    fun loadDefaultLocations(): Map<String, DefaultLocation>
}


class IOFileRepositoryImpl(
    private val context: Context
) : IOFileRepository {

    private val externalFilesDir = context.getExternalFilesDir(null)


    override fun getStyleJsonPath(map: MapModel): String? {
        val file = File(externalFilesDir, "${map.normalizedName}/style_runtime.json")
        return if (file.exists()) file.absolutePath else null
    }

    override fun getGraphPath(map: MapModel): String? {
        val file = File(externalFilesDir, "${map.normalizedName}/graph-cache")
        return if (file.exists()) file.absolutePath else null
    }

    override fun loadDefaultLocations(): Map<String, DefaultLocation> {
        val json = context.assets.open("default_location.json").bufferedReader().readText()
        return Json.decodeFromString(json)
    }
}