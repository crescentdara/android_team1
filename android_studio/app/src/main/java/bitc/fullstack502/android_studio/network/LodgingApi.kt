package bitc.fullstack502.android_studio.network

import bitc.fullstack502.android_studio.model.LodgingItem
import bitc.fullstack502.android_studio.network.dto.LodgingBookingDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LodgingApi {

    /** 숙소 목록 조회 */
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

    /** 숙소 예약 생성 */
    @POST("/api/lodging/book")
    fun createBooking(
        @Body booking: LodgingBookingDto
    ): Call<Void>
}
