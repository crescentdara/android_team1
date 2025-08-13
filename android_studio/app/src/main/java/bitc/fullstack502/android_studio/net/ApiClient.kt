package bitc.fullstack502.android_studio.net

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java

object ApiClient {
    // 에뮬레이터는 10.0.2.2 / 마지막에 슬래시 필수!
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val http by lazy {
        val log = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder().addInterceptor(log).build()
    }

    val chat: ChatApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatApi::class.java)
    }
}
