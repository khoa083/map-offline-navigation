package com.kblack.offlinemap

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        // Compared against BuildConfig.APPLICATION_ID (not a hardcoded string) because the
        // debug build type applies applicationIdSuffix = ".dev" (see app/build.gradle.kts),
        // so the real package name at runtime is "com.kblack.offlinemap.dev", not
        // "com.kblack.offlinemap". A hardcoded expected value here silently drifts from the
        // build config the moment a suffix/flavor is introduced -- as it did previously.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals(BuildConfig.APPLICATION_ID, appContext.packageName)
    }
}
