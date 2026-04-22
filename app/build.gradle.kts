import java.util.Properties
import kotlin.apply

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.ksp)
    alias(libs.plugins.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.kblack.offlinemap"
    compileSdk = ((rootProject.extra["configSDK"] as Map<*, *>)["target_sdk"] as Int?)!!

    defaultConfig {
        applicationId = "com.kblack.offlinemap"
        minSdk = ((rootProject.extra["configSDK"] as Map<*, *>)["min_sdk"] as Int?)!!
        targetSdk = ((rootProject.extra["configSDK"] as Map<*, *>)["target_sdk"] as Int?)!!
        versionCode = rootProject.extra["versionCode"] as Int
        versionName = rootProject.extra["versionName"] as String

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true

        buildConfigField(
            "String",
            "MY_VERSION_NAME",
            "\"$versionName${rootProject.extra["myVersionName"] as String}\""
        )
        buildConfigField(
            "String",
            "MY_COMMIT_NAME",
            "\"${rootProject.extra["commitMessage"] as String}\""
        )
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val propFile = rootProject.file("local.properties")
            val props = Properties()
            if (propFile.exists()) propFile.inputStream().use { props.load(it) }

            storeFile = file(System.getenv("RELEASE_STORE_FILE") ?: props.getProperty("RELEASE_STORE_FILE") ?: "debug.keystore")
            storePassword = System.getenv("RELEASE_STORE_PASSWORD") ?: props.getProperty("RELEASE_STORE_PASSWORD") ?: ""
            keyAlias = System.getenv("RELEASE_KEY_ALIAS") ?: props.getProperty("RELEASE_KEY_ALIAS") ?: ""
            keyPassword = System.getenv("RELEASE_KEY_PASSWORD") ?: props.getProperty("RELEASE_KEY_PASSWORD") ?: ""
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            all {
                it.failOnNoDiscoveredTests = false
                it.jvmArgs(
                    "-XX:+EnableDynamicAgentLoading",
                    "-XX:-PrintWarnings",
                    "-Xshare:off"
                )
            }
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    packaging {
        dex {
            useLegacyPackaging = true
        }
        jniLibs {
            useLegacyPackaging = true
        }
        resources {
            excludes += "META-INF/*.version"
            // https://youtrack.jetbrains.com/issue/KT-48019/Bundle-Kotlin-Tooling-Metadata-into-apk-artifacts
            excludes += "kotlin-tooling-metadata.json"
            // https://github.com/Kotlin/kotlinx.coroutines?tab=readme-ov-file#avoiding-including-the-debug-infrastructure-in-the-resulting-apk
            excludes += "DebugProbesKt.bin"
            excludes += setOf(
                "META-INF/LICENSE.md",
                "META-INF/NOTICE.md"
            )
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // TODO myVersionName contains the hash of the commit
//            versionNameSuffix = rootProject.extra["myVersionName"] as String
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isPseudoLocalesEnabled = true
//            applicationIdSuffix = ".debug"
            // TODO myVersionName contains the hash of the commit
//            versionNameSuffix = rootProject.extra["myVersionName"] as String
            enableUnitTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }


    androidResources {
        noCompress.add("pmtiles")
    }

    dependenciesInfo {
        // Disables dependency metadata when building APKs (for IzzyOnDroid/F-Droid)
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles (for Google Play)
        includeInBundle = false
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
    implementation(libs.slf4j.android)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine.test)

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

// todo: FIX https://issuetracker.google.com/issues/463283604
apply(from = file("jacoco.gradle.kts"))