package bitc.fullstack502.android_studio.net

import bitc.fullstack502.android_studio.model.BookingRequest
import bitc.fullstack502.android_studio.model.BookingResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FlightReservationAPI {
    @POST("/api/booking/flight")
    fun createBooking(@Body request: BookingRequest) : BookingResponse

    @GET("/api/booking/flight/{id}")
    fun getBooking()
}