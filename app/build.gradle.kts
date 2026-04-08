plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.ksp)
    alias(libs.plugins.android.hilt)
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.kblack.offlinemap"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.kblack.offlinemap"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isPseudoLocalesEnabled = true
//            applicationIdSuffix = ".debug"
            // TODO myVersionName contains the hash of the commit
//            versionNameSuffix = rootProject.extra["myVersionName"] as String
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }

    androidResources {
        noCompress.add("pmtiles")
    }
    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE.md",
                "META-INF/NOTICE.md"
            )
        }
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.navigation.compose)
    implementation(libs.icons.extended)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.coil.compose)
    implementation(libs.bundles.dataStore)
    implementation(libs.work.runtime.ktx)
    implementation(libs.play.services.location)
    implementation(libs.bundles.maplibre)
    implementation(libs.konfetti.compose)
//    implementation(libs.maplibre.plugin.annotation)
    ksp(libs.hilt.android.compiler)
    implementation(libs.bundles.hilt)
    implementation(libs.bundles.roomDb)
    ksp(libs.room.compiler)
    implementation(libs.bundles.retrofit2)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.bundles.okhttp)
    implementation(libs.timber)
    implementation(libs.commonmark)
    implementation(libs.richtext)
    implementation(libs.tar)

    //noinspection UseTomlInstead
    implementation("com.github.luben:zstd-jni:1.5.7-7@aar")
    //todo: https://discuss.graphhopper.com/t/offlne-routing-on-android/9176/3
    implementation("com.graphhopper:graphhopper-core:1.0") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
        exclude(group = "org.openstreetmap.osmosis", module = "osmosis-osm-binary")
        exclude(group = "org.apache.xmlgraphics", module = "xmlgraphics-commons")
    }

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)
    debugImplementation(libs.chucker.debug)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.leak.canary)
    releaseImplementation(libs.chucker.release)
}