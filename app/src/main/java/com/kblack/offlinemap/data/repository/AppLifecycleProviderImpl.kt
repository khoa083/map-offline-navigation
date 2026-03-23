package com.kblack.offlinemap.data.repository

import com.kblack.offlinemap.domain.repository.AppLifecycleProvider

class AppLifecycleProviderImpl : AppLifecycleProvider {
    private var _isAppInForeground = false

    override var isAppInForeground: Boolean
        get() = _isAppInForeground
        set(value) {
            _isAppInForeground = value
        }
}