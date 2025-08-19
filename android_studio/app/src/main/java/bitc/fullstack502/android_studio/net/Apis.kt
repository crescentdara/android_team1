package bitc.fullstack502.android_studio.net

import kotlin.jvm.java

object Apis {
    private val retrofit by lazy { RetrofitProvider.retrofit }

    val flightSearch: FlightSearchAPI by lazy {
        retrofit.create(FlightSearchAPI::class.java)
    }

    val reservation: FlightReservationAPI by lazy {
        retrofit.create(FlightReservationAPI::class.java)
    }
}
