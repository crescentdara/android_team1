pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://repository.map.naver.com/archive/maven")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
<<<<<<< HEAD
<<<<<<< HEAD
        maven(url = "https://jitpack.io")  // 라이브러리 저장소(JitPack) 등록
=======
        maven(url = "https://jitpack.io") // ✅ 이 줄 추가!
>>>>>>> origin/hsm/UserLogin
    }
}

rootProject.name = "Chat"
=======
        maven("https://jitpack.io")
        maven("https://repository.map.naver.com/archive/maven")
    }
}


rootProject.name = "android_studio"
>>>>>>> testmerge/LodgingMerge
include(":app")
 