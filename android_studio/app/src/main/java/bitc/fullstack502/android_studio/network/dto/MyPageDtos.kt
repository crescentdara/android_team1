package bitc.fullstack502.android_studio.network.dto

data class CommentDto(
    val id: Long,
    val postId: Long,
    val parentId: Long?,
    val author: String,
    val content: String,
    val createdAt: String,

    // 👇 신규(옵션)
    val postTitle: String? = null,
    val postImgUrl: String? = null
)

// 항공 즐겨찾기/예약
data class FlightWishDto(
    val id: Long,
    val airline: String,
    val flightNo: String,
    val depart: String,   // ICN 등
    val arrive: String,   // NRT 등
    val thumb: String?    // 항공사 로고 등
)

data class FlightBookingDto(
    val id: Long,
    val airline: String,
    val flightNo: String,
    val depart: String,
    val arrive: String,
    val departTime: String,  // "2025-08-21 09:00"
    val arriveTime: String,
    val status: String
)

// 숙소 즐겨찾기
data class LodgingWishDto(
    val id: Long,
    val lodId: Long,
    val name: String,
    val city: String?,
    val town: String?,
    val img: String?
)


