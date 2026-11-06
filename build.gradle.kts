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
    // Keep ignoreFailures until config/detekt/baseline.xml is generated on an Android SDK agent:
    //   ./gradlew detektBaseline && git add config/detekt/baseline.xml
    // then set ignoreFailures = false.
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

val koverVerifiedModules = setOf(
    "model",
    "billing",
    "sharing",
    "download",
    "monitoring",
    "device",
)

subprojects {
    // Reproducible dependency resolution when lockfiles are present (generate with --write-locks).
    configurations.configureEach {
        resolutionStrategy.activateDependencyLocking()
    }

    plugins.withId("com.android.library") {
        apply(plugin = "org.jetbrains.kotlinx.kover")
        if (name in koverVerifiedModules) {
            extensions.configure<org.jetbrains.kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension>("kover") {
                reports {
                    total {
                        verify {
                            rule("line-floor") {
                                minBound {
                                    // Target 60% as more ViewModel pure tests land; floor for CI stability.
                                    line.set(40)
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

// Convenience alias documented in README / CI.
tasks.register("coverageCheck") {
    dependsOn(
        ":core:model:koverVerify",
        ":core:billing:koverVerify",
        ":core:sharing:koverVerify",
        ":core:download:koverVerify",
        ":core:monitoring:koverVerify",
    )
    group = "verification"
    description = "Run Kover coverage verification on core modules with unit tests."
}
