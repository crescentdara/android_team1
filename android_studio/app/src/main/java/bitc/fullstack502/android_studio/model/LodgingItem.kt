package bitc.fullstack502.android_studio.model

data class LodgingItem(
    val id: Long,
    val name: String,
    val price: Int,
    val rating: Double?,
    val thumbnailUrl: String?
)