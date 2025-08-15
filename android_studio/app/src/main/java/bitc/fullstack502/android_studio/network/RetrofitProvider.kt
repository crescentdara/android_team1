package bitc.fullstack502.android_studio.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitProvider {

    // ① 끝에 슬래시 추가 (Retrofit 규칙)
    private const val BASE_URL = "http://10.0.2.2:8080"

    private val client by lazy {
        val log = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        OkHttpClient.Builder()
            .addInterceptor(log)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    // ② private 제거 → LodgingDetailActivity/NearbyFoodBottomSheet에서 접근 가능
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val locationApi: LocationApi by lazy { retrofit.create(LocationApi::class.java) }
    val lodgingApi: LodgingApi by lazy { retrofit.create(LodgingApi::class.java) }

    val lodgingDetailApi: LodgingDetailApi by lazy { retrofit.create(LodgingDetailApi::class.java) }

}
