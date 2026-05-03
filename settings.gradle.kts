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
        mavenLocal()
        google()
        mavenCentral()
    }
}

rootProject.name = "VBHelper"
include(":app")

includeBuild("../VB-DIM-Reader-2.0.0") {
    dependencySubstitution {
        substitute(module("com.github.cfogrady:vb-dim-reader")).using(project(":"))
    }
}

includeBuild("../vb-nfc-reader") {
    dependencySubstitution {
        substitute(module("com.github.cfogrady:vb-nfc-reader")).using(project(":"))
    }
}
