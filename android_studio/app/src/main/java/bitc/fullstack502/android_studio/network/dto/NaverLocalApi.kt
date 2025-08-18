package bitc.fullstack502.android_studio.network.dto

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NaverLocalApi {
    /** 서버 프록시: /api/naver/local/nearby?query=...&lat=...&lon=...&radius=...&size=... */
    @GET("/api/naver/local/nearby")
    fun nearby(
        @Query("query") query: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radius") radius: Int = 1000,
        @Query("size") size: Int = 30
    ): Call<NaverLocalResp>
}
