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
//@Suppress("JcenterRepositoryObsolete")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven ("https://jitpack.io"){
//            name = "GitHubPackages"
//            url = uri("https://maven.pkg.github.com/termux/termux-app")

            credentials {
                username = "danieledellacioppa"

            }
        }
        //noinspection JcenterRepositoryObsolete
//        jcenter()
    }
}

rootProject.name = "AndroidRemoteController"
include(":app")
 