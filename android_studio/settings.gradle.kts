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
<<<<<<< HEAD
        maven(url = "https://jitpack.io")  // 라이브러리 저장소(JitPack) 등록
=======
        maven(url = "https://jitpack.io") // ✅ 이 줄 추가!
>>>>>>> origin/hsm/UserLogin
    }
}

rootProject.name = "Chat"
include(":app")
 