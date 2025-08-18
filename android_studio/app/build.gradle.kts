plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "bitc.fullstack502.android_studio"
    compileSdk = 36

    defaultConfig {
        applicationId = "bitc.fullstack502.android_studio"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    // <-- ViewBinding은 이 형태로 사용 (AGP 8.x)
    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
}

dependencies {
<<<<<<< HEAD
<<<<<<< HEAD
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("com.google.android.material:material:1.8.0")
// TabLayout
    implementation("androidx.viewpager2:viewpager2:1.0.0")
// ViewPager2


=======
    // ====== 네트워크 ======
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
>>>>>>> testmerge/LodgingMerge

    // ====== 코루틴 ======
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ====== UI ======
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // ====== AndroidX ======
=======
>>>>>>> testmerge/PostChatMerge
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
<<<<<<< HEAD

    // ====== 테스트 ======
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ====== STOMP & Rx ======
=======

    // ✅ SwipeRefreshLayout 추가 (레이아웃 클래스 못 찾는 오류 해결)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // 네트워킹
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // uCrop
    implementation("com.github.yalantis:ucrop:2.2.8")

    // Rx / STOMP (필요 시)
>>>>>>> testmerge/PostChatMerge
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

<<<<<<< HEAD

<<<<<<< HEAD
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // 홈 화면
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
}
=======
    // ====== Fragment 확장 ======
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    
    // 네이버 지도
    implementation("com.naver.maps:map-sdk:3.22.1")

    // 추가 ~
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    //
    implementation(libs.material)
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("io.coil-kt:coil:2.6.0")
}
>>>>>>> testmerge/LodgingMerge
=======
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    kapt("com.github.bumptech.glide:compiler:4.16.0") // Kotlin일 때
}
>>>>>>> testmerge/PostChatMerge
