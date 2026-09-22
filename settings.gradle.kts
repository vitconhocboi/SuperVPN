include(":core:baseui")


include(":core:billing")


include(":core:analytics")


include(":core:config")

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
        maven(url = uri("https://jitpack.io"))
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = uri("https://jitpack.io"))
        flatDir {
            dirs("app/libs")
        }
    }
}

rootProject.name = "SuperVPN"
include(":app")
include(":BaseUI")
include(":core")
include(":core:preference")
include(":core:dimens")
include(":core:utilities")
include(":core:rate")

 