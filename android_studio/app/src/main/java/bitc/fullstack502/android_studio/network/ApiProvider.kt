package bitc.fullstack502.android_studio.network

import bitc.fullstack502.android_studio.util.AuthManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiProvider {

    // ✅ BASE_URL은 단 한 곳에서만 관리 (반드시 슬래시로 끝나야 함)
    private const val BASE_URL = "http://10.0.2.2:8080/"


    private val client: OkHttpClient by lazy {
        val log = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            // 🔐 토큰 자동 첨부 인터셉터
            .addInterceptor { chain ->
                val req = chain.request()
                val token = AuthManager.accessToken()
                val newReq = if (!token.isNullOrBlank()) {
                    req.newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else req
                chain.proceed(newReq)
            }
            .addInterceptor(log)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 하나의 통합 서비스만 노출
    val api: AppApi by lazy { retrofit.create(AppApi::class.java) }

    /*
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

// 여기서 부터는 Apis.kt
    private val retrofit by lazy { RetrofitProvider.retrofit }

    val flightSearch: FlightSearchAPI by lazy {
        retrofit.create(FlightSearchAPI::class.java)
    }

    val reservation: FlightReservationAPI by lazy {
        retrofit.create(FlightReservationAPI::class.java)
    }
    */
}
