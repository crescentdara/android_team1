package bitc.fullstack502.android_studio.network.dto

data class PostDto(
    val id: Long,
    val title: String,
    val content: String?,
    val imgUrl: String?,
    val lookCount: Long,
    val likeCount: Long,
    val author: String,
    val createdAt: String?,
    val updatedAt: String?
)

data class PagePostDto(
    val content: List<PostDto>,
    val number: Int,
    val totalPages: Int,
    val totalElements: Long
)
