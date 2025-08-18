pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
        maven("https://repository.map.naver.com/archive/maven") // <-- 플러그인 쪽에서도 인식되도록
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")                           // STOMP, uCrop
        maven("https://repository.map.naver.com/archive/maven")// <-- 네이버 지도 SDK
        // 혹시 위가 막혀있으면 예비로 다음도 추가해보세요:
        // maven("https://navermaps.github.io/android-map-sdk/repo")
    }
}

rootProject.name = "android_studio"
include(":app")
