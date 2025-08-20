package bitc.fullstack502.android_studio.network.dto

data class LodgingListDto(
    val id: Long,
    val name: String?,
    val city: String?,
    val town: String?,
    val addrRd: String?,
    val basePrice: Long?,
    val img: String?
)
