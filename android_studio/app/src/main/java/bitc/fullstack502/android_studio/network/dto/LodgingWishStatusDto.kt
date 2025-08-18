package bitc.fullstack502.android_studio.network.dto

import com.google.gson.annotations.SerializedName

data class LodgingWishStatusDto(
    val wished: Boolean,
    @SerializedName(value = "wishCount", alternate = ["count"])
    val wishCount: Long
)
