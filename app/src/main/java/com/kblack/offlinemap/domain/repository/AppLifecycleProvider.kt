package com.kblack.offlinemap.domain.repository


// để các clas khác biết app đang foreground hay background
interface AppLifecycleProvider {
    var isAppInForeground: Boolean
}