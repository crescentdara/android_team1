package bitc.fullstack502.android_studio.ui

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DateLabels {
    private val iso = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private val ymd = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun labelOf(sentAtIso: String, zone: ZoneId = ZoneId.systemDefault()): String {
        // 서버가 "2025-08-13T07:12:34Z" 같은 ISO라 가정
        val zdt: ZonedDateTime = runCatching { ZonedDateTime.parse(sentAtIso, iso) }.getOrNull()
            ?: return sentAtIso.replace('T',' ').take(10)
        val d: LocalDate = zdt.withZoneSameInstant(zone).toLocalDate()
        val today = LocalDate.now(zone)
        return when (d) {
            today -> "오늘"
            today.minusDays(1) -> "어제"
            else -> d.format(ymd)
        }
    }
}
