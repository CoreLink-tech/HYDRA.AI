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

// Modules with meaningful JVM unit tests — coverage gate applies only here.
val koverVerifiedModules = setOf(
    "model",
    "billing",
    "sharing",
    "download",
    "monitoring",
    "device",
)

subprojects {
    plugins.withId("com.android.library") {
        apply(plugin = "org.jetbrains.kotlinx.kover")
        if (name in koverVerifiedModules) {
            extensions.configure<org.jetbrains.kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension>("kover") {
                reports {
                    total {
                        verify {
                            rule("alpha-line-floor") {
                                minBound {
                                    line.set(30)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}
