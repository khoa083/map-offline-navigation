package com.kblack.offlinemap.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.kblack.offlinemap.BuildConfig
import com.kblack.offlinemap.data.models.Config
import androidx.core.net.toUri

class UpdateChecker {
    private val currentVer = BuildConfig.VERSION_CODE
    private val BASE_REPO_GITHUB_URL = "https://github.com/khoa083/map-offline-navigation/"

    //todo: FIXME Currently, there is only one API configuration.
//    fun isUpdateApp(config: Config): Boolean {
//        return when (BuildConfig.BUILD_TYPE){
//            "debug"-> {
//                currentVer < config.version ?: return false
//            }
//            else ->{
//                currentVer < config.version ?: return false
//            }
//        }
//    }
    fun isUpdateApp(config: Config?): Boolean = currentVer < (config?.version ?: return false)

    fun openGitHubRelease(context: Context, versionTag: String?) {
        val url = "${BASE_REPO_GITHUB_URL}releases/tag/$versionTag"
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {null}
    }
}

fun Int.toVersionName(): String {
    val major = this / 1000000
    val minor = (this % 1000000) / 10000
    val patch = (this % 10000) / 100
    return "$major.$minor.$patch"
}