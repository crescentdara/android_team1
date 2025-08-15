package bitc.fullstack502.android_studio.network

import bitc.fullstack502.android_studio.model.LodgingItem
import retrofit2.http.GET
import retrofit2.http.Query

interface LodgingApi {
    @GET("/api/lodgings")
    suspend fun getLodgings(
        @Query("city") city: String?,
        @Query("town") town: String?,
        @Query("vill") vill: String?,
        @Query("checkIn") checkIn: String?,
        @Query("checkOut") checkOut: String?,
        @Query("adults") adults: Int?,
        @Query("children") children: Int?
    ): List<LodgingItem>
}