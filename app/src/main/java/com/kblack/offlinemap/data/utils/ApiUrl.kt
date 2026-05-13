package com.kblack.offlinemap.data.utils

import com.kblack.offlinemap.BuildConfig

object ApiUrl {
    //TODO: Since this is an open-source project, the URL is placed here.
    const val BASE_GITHUB_URL = "https://raw.githubusercontent.com/khoa083/mapdata/refs/heads/main/"
    const val BASE_NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?"
    const val BASE_PHOTON_URL = "https://photon.komoot.io/"

    //todo: FIXME: It shouldn't be like this; this is a temporary solution, I'll revise it later.
//    val ALLOWLIST_ENDPOINT = when (BuildConfig.BUILD_TYPE) {
//        "debug" -> "map_lists_dev.json"
//        else -> "map_lists.json"
//    }
    const val ALLOWLIST_ENDPOINT = "map_lists.json"
    const val CONFIG_ENDPOINT = "config.json"
    const val PHOTON_SEARCH_ENDPOINT = "api/"

}