package bitc.fullstack502.android_studio.net

import bitc.fullstack502.android_studio.model.BookingRequest
import bitc.fullstack502.android_studio.model.BookingResponse
import bitc.fullstack502.android_studio.model.Flight
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface FlightSearchAPI {
    @GET("api/flights/search")
    fun searchFlights(
        @Query("dep") dep: String,
        @Query("arr") arr: String,
        @Query("date") date: String,              // yyyy-MM-dd
        @Query("depTime") depTime: String? = null // null이면 쿼리에서 제외
    ): Call<List<Flight>>


}
