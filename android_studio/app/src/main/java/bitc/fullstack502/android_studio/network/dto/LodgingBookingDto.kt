package bitc.fullstack502.android_studio.network.dto

data class LodgingBookingDto(
    val id: Long? = null,       // 예약 PK
    val userId: Long,
    val lodId: Long,
    val ckIn: String,
    val ckOut: String,
    val totalPrice: Long,
    val roomType: String,
    val adult: Int,
    val child: Int,
    val status: String,

    // 서버에서 내려주는 숙소 정보
    val lodName: String?,       // 숙소 이름
    val lodImg: String?,        // 숙소 이미지
    val addrRd: String?,        // 도로명 주소
    val addrJb: String?         // 지번 주소
)
