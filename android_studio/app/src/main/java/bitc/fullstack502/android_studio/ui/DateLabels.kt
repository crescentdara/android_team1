package bitc.fullstack502.android_studio.ui

import java.time.*
import java.time.format.DateTimeFormatter

object DateLabels {
    private val ymd = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun labelOf(sentAtIso: String, zone: ZoneId = ZoneId.systemDefault()): String {
        val d = toLocalDate(sentAtIso, zone)
        val today = LocalDate.now(zone)
        return when (d) {
            today -> "오늘"
            today.minusDays(1) -> "어제"
            else -> d.format(ymd)
        }
    }

    private fun toLocalDate(iso: String, zone: ZoneId): LocalDate =
        try { Instant.parse(iso).atZone(zone).toLocalDate() }
        catch (_: Exception) { LocalDate.parse(iso.substring(0,10)) }
}
