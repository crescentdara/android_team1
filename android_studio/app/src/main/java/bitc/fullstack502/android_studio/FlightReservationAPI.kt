package bitc.fullstack502.android_studio

import bitc.fullstack502.android_studio.data.BookingRequest
import bitc.fullstack502.android_studio.data.BookingResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FlightReservationAPI {
    @POST("/api/booking/flight")
    fun createBooking(@Body request: BookingRequest) : BookingResponse

    @GET("/api/booking/flight/{id}")
    fun getBooking()
}