package com.kblack.offlinemap.data.repository

interface AppLifecycleProvider {
    var isAppInForeground: Boolean
}

class AppLifecycleProviderImpl : AppLifecycleProvider {
    private var _isAppInForeground = false

    override var isAppInForeground: Boolean
        get() = _isAppInForeground
        set(value) {
            _isAppInForeground = value
        }
}