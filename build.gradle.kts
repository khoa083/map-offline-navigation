import org.jetbrains.kotlin.util.removeSuffixIfPresent

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.hilt) apply false
    alias(libs.plugins.android.ksp) apply false
}

val configSDK = mapOf(
    "min_sdk" to 26,
    "target_sdk" to 36,

    )

val versionInfo = mapOf(
    "major" to 1,
    "minor" to 0,
    "patch" to 0,
    "build" to 0
)

val myVersionName = "." + "git rev-parse --short=7 HEAD".runCommand(workingDir = rootDir)
val commitMessage = "git log -1 --pretty=%B".runCommand(workingDir = rootDir).replace("\n", " ")

val versionCode =
    versionInfo["major"]!! * 1000000 + versionInfo["minor"]!! * 10000 + versionInfo["patch"]!! * 100 + versionInfo["build"]!!

val versionName =
    "${versionInfo["major"]}.${versionInfo["minor"]}.${versionInfo["patch"]}"

fun String.runCommand(
    workingDir: File = File(".")
): String = providers.exec {
    setWorkingDir(workingDir)
    commandLine(split(' '))
}.standardOutput.asText.get().removeSuffixIfPresent("\n")

//fun readProperties(propertiesFile: File) = Properties().apply {
//    propertiesFile.inputStream().use { fis ->
//        load(fis)
//    }
//}

extra["configSDK"] = configSDK
extra["versionCode"] = versionCode
extra["versionName"] = versionName
extra["myVersionName"] = myVersionName
extra["commitMessage"] = commitMessage