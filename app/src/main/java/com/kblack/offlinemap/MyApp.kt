package com.kblack.offlinemap

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Stable
import com.kblack.offlinemap.presentation.BugHandlerActivity
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import kotlin.system.exitProcess

@Stable
@HiltAndroidApp
class MyApp: Application(), Thread.UncaughtExceptionHandler  {

    init {
        if (BuildConfig.DEBUG) Thread.setDefaultUncaughtExceptionHandler(this)
    }

    companion object {
        private const val TAG = "KblackApplication"
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        val exceptionMessage = Log.getStackTraceString(e)
        val threadName = Thread.currentThread().name
        Timber.tag(TAG).e("Error on thread $threadName:\n $exceptionMessage")
        if(BuildConfig.DEBUG) {
            val intent = Intent(this, BugHandlerActivity::class.java)
            intent.putExtra("exception_message", exceptionMessage)
            intent.putExtra("thread", threadName)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        exitProcess(10)
    }

    override fun onCreate() {
        super.onCreate()

        if (Timber.treeCount == 0 && BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

}