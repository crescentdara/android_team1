package bitc.fullstack502.android_studio.util

object ChatIds {
    /** 두 userId를 정렬해서 항상 같은 roomId 생성 */
    fun roomIdFor(a: String, b: String): String =
        if (a <= b) "$a|$b" else "$b|$a"
}
