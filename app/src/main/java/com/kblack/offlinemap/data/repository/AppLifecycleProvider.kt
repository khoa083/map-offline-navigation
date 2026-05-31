package com.kblack.offlinemap.data.repository

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AppLifecycleProvider {
    val isAppInForeground: StateFlow<Boolean>
}

class AppLifecycleProviderImpl : AppLifecycleProvider, DefaultLifecycleObserver {
    private val _isAppInForeground = MutableStateFlow(false)
    override val isAppInForeground: StateFlow<Boolean> = _isAppInForeground.asStateFlow()

    fun startTracking() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        _isAppInForeground.value = true
    }

    override fun onStop(owner: LifecycleOwner) {
        _isAppInForeground.value = false
    }

}