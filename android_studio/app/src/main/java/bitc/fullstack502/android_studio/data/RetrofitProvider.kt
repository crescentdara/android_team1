package bitc.fullstack502.android_studio.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitProvider {
    // 에뮬레이터에서 PC 서버 접속
    private const val BASE_URL = "http://10.0.2.2:8080"

    val postApi: PostApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PostApi::class.java)
    }
}
