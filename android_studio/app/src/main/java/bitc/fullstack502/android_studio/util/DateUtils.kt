package bitc.fullstack502.android_studio.util

/**
 * "08.26(화)" 또는 "8.26(화)" → "2025-08-26"
 */
fun displayToYmd(display: String, year: Int): String {
    val m = Regex("""^\s*(\d{1,2})\.(\d{1,2})""").find(display)
        ?: throw IllegalArgumentException("날짜 형식 오류: $display")
    val mm = m.groupValues[1].padStart(2, '0')
    val dd = m.groupValues[2].padStart(2, '0')
    return "$year-$mm-$dd"
}

/**
 * "08.26(화) ~ 08.28(목)" → Pair("2025-08-26","2025-08-28")
 */
fun displayRangeToYmdPair(range: String, year: Int): Pair<String, String> {
    val parts = range.split("~")
    require(parts.size == 2) { "날짜 범위 형식 오류: $range" }
    val start = displayToYmd(parts[0].trim(), year)
    val end   = displayToYmd(parts[1].trim(), year)
    return start to end
}
