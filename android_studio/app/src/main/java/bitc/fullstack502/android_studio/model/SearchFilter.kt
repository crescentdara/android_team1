package bitc.fullstack502.android_studio.model

data class SearchFilter(
    var city: String? = null,
    var town: String? = null,
    var vill: String? = null,
    var checkIn: String? = null,
    var checkOut: String? = null,
    var adults: Int = 1,
    var children: Int = 0
)