package bitc.fullstack502.android_studio.network

import bitc.fullstack502.android_studio.network.dto.LodgingDetailDto
import bitc.fullstack502.android_studio.network.dto.AvailabilityDto
import retrofit2.Call
import retrofit2.http.*

interface LodgingDetailApi {
    @GET("/api/lodging/{id}/detail")
    fun getDetail(@Path("id") id: Long): Call<LodgingDetailDto>

    @GET("/api/lodging/{id}/availability")
    fun getAvailability(
        @Path("id") id: Long,
        @Query("checkIn") checkIn: String,
        @Query("checkOut") checkOut: String,
        @Query("guests") guests: Int? = null
    ): Call<AvailabilityDto>

    @POST("/api/lodging/{id}/prepay")
    fun prepay(@Path("id") id: Long, @Body body: Map<String, Any>): Call<Map<String, Any>>



}
