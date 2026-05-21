package com.kblack.offlinemap.data.remote.api.interceptor

import android.os.Build
import com.kblack.offlinemap.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class PhotonInterceptor(): Interceptor {

    val userAgent = "Kblack: Offline Map Navigation/${BuildConfig.VERSION_CODE} (https://github.com/khoa083/map-offline-navigation) " +
            "Android/${Build.VERSION.RELEASE}; ${Build.MODEL} Build/${Build.ID}"

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithUserAgent = originalRequest.newBuilder()
            .header("User-Agent", userAgent)
            .build()
        return chain.proceed(requestWithUserAgent)
    }

}