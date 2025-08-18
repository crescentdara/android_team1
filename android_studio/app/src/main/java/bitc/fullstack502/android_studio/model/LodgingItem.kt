package bitc.fullstack502.android_studio.model

import com.google.gson.annotations.SerializedName

data class LodgingItem(
    val id: Long,
    val name: String,
    val city: String?,
    val town: String?,
    val addrRd: String?,   // 서버 DTO와 이름 일치
    // ✅ 서버에서 price, basePrice, lowestPrice 어떤 키를 써도 price 필드로 들어오도록 매핑
    @SerializedName(value = "price", alternate = ["basePrice", "lowestPrice"])
    val price: Long,
    val img: String?
)