package tanmay.better.tracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class DateUtilsTest {

    @Test
    fun getDayStartEnd_fixedDateUtc_producesCorrectRange() {
        val date = LocalDate.of(2026, 10, 27)
        val zone = ZoneId.of("UTC")

        val (start, end) = DateUtils.getDayStartEnd(date, zone)

        val expectedStart = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val expectedEnd = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        assertEquals(expectedStart, start)
        assertEquals(expectedEnd, end)
        assertEquals(24 * 60 * 60 * 1000L, end - start)
    }

    @Test
    fun getDayStartEnd_fixedDateNonUtc_producesCorrectRange() {
        val date = LocalDate.of(2026, 5, 16)
        val zone = ZoneId.of("Asia/Kolkata")

        val (start, end) = DateUtils.getDayStartEnd(date, zone)

        val expectedStart = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val expectedEnd = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        assertEquals(expectedStart, start)
        assertEquals(expectedEnd, end)
        assertEquals(24 * 60 * 60 * 1000L, end - start)
    }

    @Test
    fun getDayStartEnd_defaultParameters_producesValidRange() {
        val (start, end) = DateUtils.getDayStartEnd()
        assertTrue(start < end)
        assertTrue(end - start in (23 * 60 * 60 * 1000L)..(25 * 60 * 60 * 1000L))
    }

    @Test
    fun getPastDaysRange_sevenDays_startsSixDaysPriorEndsNextDay() {
        val referenceDate = LocalDate.of(2026, 10, 27)
        val zone = ZoneId.of("UTC")

        val (start, end) = DateUtils.getPastDaysRange(days = 7, referenceDate = referenceDate, zone = zone)

        val expectedStart = referenceDate.minusDays(6).atStartOfDay(zone).toInstant().toEpochMilli()
        val expectedEnd = referenceDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        assertEquals(expectedStart, start)
        assertEquals(expectedEnd, end)
        assertEquals(7 * 24 * 60 * 60 * 1000L, end - start)
    }

    @Test
    fun getPastDaysRange_oneDay_matchesSingleDayRange() {
        val referenceDate = LocalDate.of(2026, 10, 27)
        val zone = ZoneId.of("UTC")

        val pastRange = DateUtils.getPastDaysRange(days = 1, referenceDate = referenceDate, zone = zone)
        val dayRange = DateUtils.getDayStartEnd(referenceDate, zone)

        assertEquals(dayRange.first, pastRange.first)
        assertEquals(dayRange.second, pastRange.second)
    }

    @Test
    fun getPastDaysRange_thirtyDays_producesCorrectRange() {
        val referenceDate = LocalDate.of(2026, 10, 27)
        val zone = ZoneId.of("UTC")

        val (start, end) = DateUtils.getPastDaysRange(days = 30, referenceDate = referenceDate, zone = zone)

        val expectedStart = referenceDate.minusDays(29).atStartOfDay(zone).toInstant().toEpochMilli()
        val expectedEnd = referenceDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        assertEquals(expectedStart, start)
        assertEquals(expectedEnd, end)
        assertEquals(30 * 24 * 60 * 60 * 1000L, end - start)
    }

    @Test
    fun getPastDaysRange_defaultParameters_producesValidRange() {
        val (start, end) = DateUtils.getPastDaysRange(7)
        assertTrue(start < end)
    }
}
