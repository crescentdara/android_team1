package bitc.fullstack502.android_studio.network

import bitc.fullstack502.android_studio.network.dto.LodgingWishStatusDto
import retrofit2.Call
import retrofit2.http.*

interface LodgingWishApi {
    @GET("/api/lodging/{id}/wish/status")
    fun status(
        @Path("id") lodgingId: Long,
        @Query("userId") userId: Long
    ): Call<LodgingWishStatusDto>

    @POST("/api/lodging/{id}/wish/toggle")
    fun toggle(
        @Path("id") lodgingId: Long,
        @Query("userId") userId: Long
    ): Call<LodgingWishStatusDto>
}
