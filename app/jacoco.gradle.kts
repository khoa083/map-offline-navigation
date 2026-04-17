import org.gradle.testing.jacoco.tasks.JacocoReport

apply(plugin = "jacoco")

configure<JacocoPluginExtension> {
    toolVersion = "0.8.14"
}

val coverageModules = listOf(
    ":app"
)

// Mirrors JacocoConventionPlugin.JACOCO_EXCLUSIONS — keep in sync
val jacocoExclusions = listOf(
    "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
    "**/*BuildConfig*.*", "android/**/*.*",
    "**/*Test*.*",
    "**/di/**", "**/*Module*.*", "**/*_Factory*.*",
    "**/*\$ViewInjector*.*", "**/*\$ViewBinder*.*", "**/*_MembersInjector.class",
    "**/Dagger*Component*.*", "**/*Module_*Factory.class", "**/di/module/*",
    "**/*Dagger*.*", "**/*Hilt*.*", "**/hilt_aggregated_deps/*",
    "**/*_HiltModules*", "**/*_Impl*.*",
    "**/Lambda$*.class", "**/Lambda.class", "**/*Lambda.class", "**/*Lambda*.class",
    "**/*\$DefaultImpls*.*", "**/*\$Companion*.*", "**/*\$WhenMappings*.*",
    "**/*\$Creator*.*", "**/*\$inlined$*.*", "**/*\$sam$*.*",
    "**/*\$suspendImpl*.*", "**/*\$\$inlined*.*", "**/continuation/**",
    "**/ui/**", "**/*Screen*.*", "**/*Route*.*", "**/*Preview*.*",
    "**/component/**", "**/theme/**", "**/popup/**", "**/dialog/**", "**/screen/**",
    "**/*Activity*.*", "**/*Fragment*.*", "**/*App*.*",
    "**/*ComposableSingletons*.*", "**/ComposableLambda*.*",
    "**/*\$Composable*.*", "**/*Kt\$*.*", "**/*\$stableprop*.*", "**/*\$lambda*.*",
    //custom exclusions
    "**/model/**", "**/models/**", "**/navigation/**", "**/domain/repository/**",
)

tasks.register<JacocoReport>("jacocoTestReportAll") {
    group = "verification"
    description = "Generates aggregated JaCoCo coverage report for all modules"

    coverageModules.forEach { dependsOn("$it:testDebugUnitTest") }

    reports {
        xml.required.set(true)
        xml.outputLocation.set(file("${layout.buildDirectory.get()}/reports/jacoco/all/jacocoTestReportAll.xml"))
        html.required.set(true)
        html.outputLocation.set(file("${layout.buildDirectory.get()}/reports/jacoco/all/html"))
        csv.required.set(false)
    }

    val execFiles = coverageModules.map { project(it) }.map { p ->
        fileTree(p.layout.buildDirectory.get()) {
            include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        }
    }
    executionData.setFrom(files(execFiles))

    // AGP 9: collect classes from JARs (library and dynamic feature modules)
    val classFiles = coverageModules.map { project(it) }.flatMap { p ->
        val libraryJar = file("${p.layout.buildDirectory.get()}/intermediates/runtime_library_classes_jar/debug/bundleLibRuntimeToJarDebug/classes.jar")
        val featureJar = file("${p.layout.buildDirectory.get()}/intermediates/runtime_app_classes_jar/debug/bundleDebugClassesToRuntimeJar/classes.jar")
        val kotlinClasses = fileTree("${p.layout.buildDirectory.get()}/tmp/kotlin-classes/debug") { exclude(jacocoExclusions) }
        listOfNotNull(
            if (libraryJar.exists()) zipTree(libraryJar).matching { exclude(jacocoExclusions) } else null,
            if (featureJar.exists()) zipTree(featureJar).matching { exclude(jacocoExclusions) } else null,
            kotlinClasses
        )
    }
    classDirectories.setFrom(files(classFiles))

    val srcDirs = coverageModules.map { project(it) }.flatMap { p ->
        listOf("${p.projectDir}/src/main/java", "${p.projectDir}/src/main/kotlin")
    }
    sourceDirectories.setFrom(files(srcDirs))
}

tasks.register("jacocoReportAll") {
    group = "verification"
    description = "Runs jacocoTestReport for all modules and generates aggregated report"
    dependsOn("jacocoTestReportAll")
}