package bitc.fullstack502.android_studio.network

import retrofit2.http.GET
import retrofit2.http.Query

interface LocationApi {
    @GET("/api/locations/cities")
    suspend fun getCities(): List<String>

    @GET("/api/locations/towns")
    suspend fun getTowns(@Query("city") city: String): List<String>

    @GET("/api/locations/vills")
    suspend fun getVills(
        @Query("city") city: String,
        @Query("town") town: String
    ): List<String>
}