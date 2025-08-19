package bitc.fullstack502.android_studio.net

import bitc.fullstack502.android_studio.model.BookingRequest
import bitc.fullstack502.android_studio.model.BookingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FlightReservationAPI {
    // POST /api/booking/flight  ->  Response<BookingResponse> (suspend)
    @POST("api/booking/flight")
    suspend fun createBooking(
        @Body req: BookingRequest
    ): Response<BookingResponse>
}
