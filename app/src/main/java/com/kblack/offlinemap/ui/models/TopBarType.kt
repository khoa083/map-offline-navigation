package com.kblack.offlinemap.ui.models

enum class TopBarType {
    SETTING,
    DOWNLOAD_MAP,
    NAVIGATE_UP,
}

class TopBarAction(val type: TopBarType)