package bitc.fullstack502.android_studio.model

data class CommonItem(
    val id: Long,
    val title: String,
    val subtitle: String = "",
    val imageUrl: String? = null
)
