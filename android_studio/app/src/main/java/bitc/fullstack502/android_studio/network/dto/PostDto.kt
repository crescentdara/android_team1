package bitc.fullstack502.android_studio.network.dto

data class PostDto(
    val id: Long,
    val title: String,
    val content: String,
    val imgUrl: String?,
    val author: String,
    val likeCount: Int,
    val lookCount: Int,
    val liked: Boolean? = false // 서버에서 안 내려주면 기본 false
)


data class PagePostDto(
    val content: List<PostDto>,
    val number: Int,
    val totalPages: Int,
    val totalElements: Long
)
