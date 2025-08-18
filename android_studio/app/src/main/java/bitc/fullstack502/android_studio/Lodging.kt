package bitc.fullstack502.android_studio.model

data class Lodging(
    val id: Long?,
    val city: String?,
    val town: String?,
    val vill: String?,
    val name: String,
    val phone: String?,
    val addrRd: String?,
    val addrJb: String?,
    val lat: Double?,
    val lon: Double?,
    val totalRoom: Int? = 3,
    val img: String?
)
