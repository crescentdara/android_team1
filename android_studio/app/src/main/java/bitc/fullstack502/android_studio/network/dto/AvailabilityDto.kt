package bitc.fullstack502.android_studio.network.dto

data class AvailabilityDto(
    val available: Boolean,
    val totalRoom: Int,
    val reservedRooms: Long,
    val availableRooms: Int,
    val reason: String?,
    val checkIn: String?,
    val checkOut: String?,
    val guests: Int?
)
