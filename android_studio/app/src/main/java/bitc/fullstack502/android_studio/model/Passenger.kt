package bitc.fullstack502.android_studio.model

import java.io.Serializable

data class Passenger(
    val index: Int,
    val type: PassengerType,
    var lastNameEn: String = "",
    var firstNameEn: String = "",
    var gender: String = "",
    var birth: String = "",
    var passportNo: String = "",
    var passportExpiry: String = "",
    var nationality: String = "",
    var phone: String = "",
    var email: String = "",
    var emergencyName: String = "",
    var emergencyPhone: String = "",
    var edited: Boolean = false          // ★ 추가
) : Serializable {

    fun isRequiredFilled(): Boolean =
        lastNameEn.isNotBlank() &&
                firstNameEn.isNotBlank() &&
                gender.isNotBlank() &&
                birth.isNotBlank() &&
                passportNo.isNotBlank()

    fun displayName(): String =
        "$lastNameEn $firstNameEn".trim()

    fun displayTitle(): String {
        val hasName = lastNameEn.isNotBlank() || firstNameEn.isNotBlank()
        if (hasName) return displayName()
        val typeKo = if (type == PassengerType.ADULT) "성인" else "소아"
        return "$typeKo ${index + 1}"
    }
}