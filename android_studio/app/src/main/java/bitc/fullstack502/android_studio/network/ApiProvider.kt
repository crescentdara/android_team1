package bitc.fullstack502.android_studio.network

import bitc.fullstack502.android_studio.BuildConfig


import bitc.fullstack502.android_studio.util.AuthManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit/OkHttp 전역 제공자
 */
object ApiProvider {

    // ✅ BuildConfig에서 주입한 서버 주소 사용 (예: http://10.100.202.31:8080)
    private fun baseUrl(): String {
        val b = BuildConfig.API_BASE
        return if (b.endsWith("/")) b else "$b/"
    }

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = AuthManager.accessToken()
        val req = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else original
        chain.proceed(req)
    }

    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl())
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 앱 통합 API
    val api: AppApi by lazy { retrofit.create(AppApi::class.java) }
}
