package bitc.fullstack502.android_studio.model

import com.google.gson.annotations.SerializedName

data class ReadReceiptDTO(
    val roomId: String,
    val userId: String,
    val lastReadId: Long,
    @SerializedName("at") val lastReadAt: String
)

