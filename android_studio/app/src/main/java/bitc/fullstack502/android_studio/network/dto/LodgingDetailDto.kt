package bitc.fullstack502.android_studio.network.dto

data class LodgingDetailDto(
    val id: Long,
    val name: String?,
    val city: String?,
    val town: String?,
    val vill: String?,
    val phone: String?,
    val addrRd: String?,
    val addrJb: String?,
    val lat: Double?,
    val lon: Double?,
    val totalRoom: Int?,
    val img: String?,
    val views: Long?,
    val wishCount: Long?,
    val bookCount: Long?
)
