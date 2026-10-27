package tanmay.better.tracker

import java.time.LocalDate
import java.time.ZoneId

object DateUtils {

    fun getDayStartEnd(
        date: LocalDate = LocalDate.now(ZoneId.systemDefault()),
        zone: ZoneId = ZoneId.systemDefault()
    ): Pair<Long, Long> {
        val startOfDay = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val startOfNext = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return Pair(startOfDay, startOfNext)
    }

    fun getPastDaysRange(
        days: Int,
        referenceDate: LocalDate = LocalDate.now(ZoneId.systemDefault()),
        zone: ZoneId = ZoneId.systemDefault()
    ): Pair<Long, Long> {
        val startDate = if (days > 0) referenceDate.minusDays((days - 1).toLong()) else referenceDate
        val startMillis = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMillis = referenceDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return Pair(startMillis, endMillis)
    }
}
