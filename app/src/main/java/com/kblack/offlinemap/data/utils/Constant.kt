package com.kblack.offlinemap.data.utils

import com.kblack.offlinemap.BuildConfig

object Constant {
    const val KEY_MAP_URL = "KEY_MAP_URL"
    const val KEY_MAP_NAME = "KEY_MAP_NAME"
    const val KEY_MAP_DOWNLOAD_MAP_DIR = "KEY_MAP_DOWNLOAD_MAP_DIR"
    const val KEY_MAP_DOWNLOAD_FILE_NAME = "KEY_MAP_DOWNLOAD_FILE_NAME"
    const val KEY_MAP_TOTAL_BYTES = "KEY_MAP_TOTAL_BYTES"
    const val KEY_MAP_DOWNLOAD_RECEIVED_BYTES = "KEY_MAP_DOWNLOAD_RECEIVED_BYTES"
    const val KEY_MAP_DOWNLOAD_RATE = "KEY_MAP_DOWNLOAD_RATE"
    const val KEY_MAP_DOWNLOAD_REMAINING_MS = "KEY_MAP_DOWNLOAD_REMAINING_SECONDS"
    const val KEY_MAP_DOWNLOAD_ERROR_MESSAGE = "KEY_MAP_DOWNLOAD_ERROR_MESSAGE"
    const val KEY_MAP_START_UNZIPPING = "KEY_MAP_START_UNZIPPING"

    // The extension of the tmp download files.
    const val TMP_FILE_EXT = "tmp"

    const val MAP_NAME_TAG = "mapName"

    //TODO: Since this is an open-source project, the URL is placed here.
    const val BASE_HUGGINGFACE_URL = "https://huggingface.co/datasets/kblack083/mapdata/resolve/main/"

    val ALLOWLIST_URL = "https://raw.githubusercontent.com/khoa083/mapdata/refs/heads/main/${
        when(BuildConfig.BUILD_TYPE) {
            "debug" -> "map_lists_dev.json"
            else -> "map_lists.json"
        }
    }"
}