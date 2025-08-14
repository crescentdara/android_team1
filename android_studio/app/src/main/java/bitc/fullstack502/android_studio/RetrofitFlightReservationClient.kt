package bitc.fullstack502.android_studio

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitFlightReservationClient {

    private val BASE_URL = "http://10.100.202.2:8080"

    val instance: FlightReservationAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FlightReservationAPI::class.java)
    }
}