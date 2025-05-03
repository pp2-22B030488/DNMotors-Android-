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
        maven { url = uri("https://maven.pkg.github.com/pp2-22B030488/ChangePasswordLib")
            credentials {
                username = "pp2-22B030488"
                password = "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"
            }
        }

    }
}

rootProject.name = "DNMotors"
include(":app")
include(":data")
include(":domain")
