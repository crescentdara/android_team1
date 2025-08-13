package bitc.fullstack502.android_studio.network.dto

data class CommDto(
    val id: Long,
    val postId: Long,
    val parentId: Long?,
    val author: String,
    val content: String,
    val createdAt: String
)
