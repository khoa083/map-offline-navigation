package com.kblack.offlinemap.presentation.model

enum class TopBarType {
    SETTING,
    DOWNLOAD_MAP,
    NAVIGATE_UP,
}

class TopBarAction(val type: TopBarType)