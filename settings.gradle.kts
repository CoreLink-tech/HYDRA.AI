pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HydraAI"

include(":app")

// Core layers: no Android UI, pure domain plus infrastructure
include(":core:model")
include(":core:designsystem")
include(":core:device")
include(":core:database")
include(":core:llama")
include(":core:download")
include(":core:billing")
include(":core:sharing")
include(":core:monitoring")

// Feature screens
include(":feature:onboarding")
include(":feature:chat")
include(":feature:models")
include(":feature:settings")
