package bitc.fullstack502.android_studio.network.dto

data class NaverLocalItem(
    val title: String?,
    val link: String?,
    val category: String?,
    val description: String?,
    val telephone: String?,
    val address: String?,
    val roadAddress: String?,
    val mapx: String?, // TM128 x (문자열로 옴)
    val mapy: String?  // TM128 y
)
data class NaverLocalResp(val items: List<NaverLocalItem> = emptyList())


