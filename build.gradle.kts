// Top-level build file. Plugin versions live in gradle/libs.versions.toml.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt)
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    parallel = true
    // Engineering alpha: analysis always runs; does not fail the build on residual style debt.
    // After `./gradlew detektBaseline`, set ignoreFailures=false and point baseline at
    // config/detekt/baseline.xml so only new issues fail CI.
    ignoreFailures = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    baseline.setFrom(files("$rootDir/config/detekt/baseline.xml"))
    source.setFrom(
        files(
            "app/src/main/kotlin",
            "core",
            "feature",
        ),
    )
}

// One JVM target for Kotlin across every module (matches Java 17 compileOptions).
subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}
