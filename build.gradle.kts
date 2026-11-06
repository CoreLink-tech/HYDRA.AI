// Top-level build file. Plugin versions live in gradle/libs.versions.toml.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover) apply false
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    parallel = true
    // Keep ignoreFailures until config/detekt/baseline.xml is generated on an Android SDK agent.
    ignoreFailures = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    source.setFrom(
        files(
            "app/src/main/kotlin",
            "core",
            "feature",
        ),
    )
}

// Core modules where we care about unit-test coverage reporting.
val koverModules = setOf(
    "model",
    "billing",
    "sharing",
    "download",
    "monitoring",
    "device",
)

subprojects {
    // Reproducible dependency resolution when lockfiles are present (./gradlew dependencies --write-locks).
    configurations.configureEach {
        resolutionStrategy.activateDependencyLocking()
    }

    // Apply Kover without referencing its DSL types in the root script (avoids classpath compile errors).
    plugins.withId("com.android.library") {
        if (name in koverModules) {
            apply(plugin = "org.jetbrains.kotlinx.kover")
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}
