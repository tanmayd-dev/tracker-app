package tanmay.better.tracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ReportEngineTest {

    private val testZone: ZoneId = ZoneId.of("UTC")
    private val referenceDate: LocalDate = LocalDate.of(2026, 10, 27)

    private fun checkInAt(activityId: Long, date: LocalDate, hour: Int = 12): ActivityCheckIn {
        val timestamp = date.atTime(hour, 0).atZone(testZone).toInstant().toEpochMilli()
        return ActivityCheckIn(activityId = activityId, timestamp = timestamp)
    }

    @Test
    fun generateReport_emptyActivities_returnsZeroAdherenceAndEmptyReports() {
        val periods = listOf(ReportPeriod.TODAY, ReportPeriod.LAST_7_DAYS, ReportPeriod.LAST_30_DAYS)

        for (period in periods) {
            val report = ReportEngine.generateReport(
                activities = emptyList(),
                checkIns = emptyList(),
                period = period,
                referenceDate = referenceDate,
                zoneId = testZone
            )

            assertEquals(period, report.period)
            assertEquals(0f, report.overallAdherenceRate, 0.0001f)
            assertEquals(0, report.goodHabitsPassedDays)
            assertEquals(0, report.goodHabitsTotalDays)
            assertEquals(0, report.badHabitsCleanDays)
            assertEquals(0, report.badHabitsTotalDays)
            assertTrue(report.taskReports.isEmpty())
        }
    }

    @Test
    fun generateReport_singleGoodHabit_testedOverSevenDays_fiveOfSevenDaysCheckedIn() {
        val activity = TrackerActivity(
            id = 1L,
            name = "Morning Workout",
            taskType = TaskType.BINARY,
            sentiment = Sentiment.GOOD,
            target = 1
        )

        // Checked in on 5 days: Day 0 (today), Day -1, Day -2, Day -4, Day -6
        // Days -3 and -5 have no check-in
        val checkIns = listOf(
            checkInAt(1L, referenceDate.minusDays(0)),
            checkInAt(1L, referenceDate.minusDays(1)),
            checkInAt(1L, referenceDate.minusDays(2)),
            checkInAt(1L, referenceDate.minusDays(4)),
            checkInAt(1L, referenceDate.minusDays(6))
        )

        val report = ReportEngine.generateReport(
            activities = listOf(activity),
            checkIns = checkIns,
            period = ReportPeriod.LAST_7_DAYS,
            referenceDate = referenceDate,
            zoneId = testZone
        )

        assertEquals(ReportPeriod.LAST_7_DAYS, report.period)
        assertEquals(1, report.taskReports.size)

        val taskReport = report.taskReports[0]
        assertEquals(activity, taskReport.activity)
        assertEquals(5, taskReport.passedDays)
        assertEquals(7, taskReport.totalDays)
        assertEquals(5f / 7f, taskReport.adherenceRate, 0.0001f)

        assertEquals(5, report.goodHabitsPassedDays)
        assertEquals(7, report.goodHabitsTotalDays)
        assertEquals(0, report.badHabitsCleanDays)
        assertEquals(0, report.badHabitsTotalDays)
        assertEquals(5f / 7f, report.overallAdherenceRate, 0.0001f)
    }

    @Test
    fun generateReport_singleGoodHabit_testedOverToday_checkedInAndNotCheckedIn() {
        val activity = TrackerActivity(
            id = 1L,
            name = "Read 20 pages",
            taskType = TaskType.BINARY,
            sentiment = Sentiment.GOOD,
            target = 1
        )

        // Case 1: Checked in today
        val reportDoneToday = ReportEngine.generateReport(
            activities = listOf(activity),
            checkIns = listOf(checkInAt(1L, referenceDate)),
            period = ReportPeriod.TODAY,
            referenceDate = referenceDate,
            zoneId = testZone
        )
        assertEquals(1, reportDoneToday.taskReports[0].passedDays)
        assertEquals(1, reportDoneToday.taskReports[0].totalDays)
        assertEquals(1.0f, reportDoneToday.taskReports[0].adherenceRate, 0.0001f)
        assertEquals(1.0f, reportDoneToday.overallAdherenceRate, 0.0001f)
        assertEquals(1, reportDoneToday.goodHabitsPassedDays)
        assertEquals(1, reportDoneToday.goodHabitsTotalDays)

        // Case 2: Not checked in today (check-in was yesterday)
        val reportNotDoneToday = ReportEngine.generateReport(
            activities = listOf(activity),
            checkIns = listOf(checkInAt(1L, referenceDate.minusDays(1))),
            period = ReportPeriod.TODAY,
            referenceDate = referenceDate,
            zoneId = testZone
        )
        assertEquals(0, reportNotDoneToday.taskReports[0].passedDays)
        assertEquals(1, reportNotDoneToday.taskReports[0].totalDays)
        assertEquals(0.0f, reportNotDoneToday.taskReports[0].adherenceRate, 0.0001f)
        assertEquals(0.0f, reportNotDoneToday.overallAdherenceRate, 0.0001f)
        assertEquals(0, reportNotDoneToday.goodHabitsPassedDays)
        assertEquals(1, reportNotDoneToday.goodHabitsTotalDays)
    }

    @Test
    fun generateReport_singleBadHabit_targetZero_sevenDays_sixCleanDays() {
        val activity = TrackerActivity(
            id = 2L,
            name = "Smoking",
            taskType = TaskType.COUNTER,
            sentiment = Sentiment.BAD,
            target = 0
        )

        // 0 check-ins on 6 days; 2 check-ins on 1 day (minusDays(2))
        val checkIns = listOf(
            checkInAt(2L, referenceDate.minusDays(2), hour = 10),
            checkInAt(2L, referenceDate.minusDays(2), hour = 18)
        )

        val report = ReportEngine.generateReport(
            activities = listOf(activity),
            checkIns = checkIns,
            period = ReportPeriod.LAST_7_DAYS,
            referenceDate = referenceDate,
            zoneId = testZone
        )

        assertEquals(ReportPeriod.LAST_7_DAYS, report.period)
        assertEquals(1, report.taskReports.size)

        val taskReport = report.taskReports[0]
        assertEquals(activity, taskReport.activity)
        assertEquals(6, taskReport.passedDays)
        assertEquals(7, taskReport.totalDays)
        assertEquals(6f / 7f, taskReport.adherenceRate, 0.0001f)

        assertEquals(0, report.goodHabitsPassedDays)
        assertEquals(0, report.goodHabitsTotalDays)
        assertEquals(6, report.badHabitsCleanDays)
        assertEquals(7, report.badHabitsTotalDays)
        assertEquals(6f / 7f, report.overallAdherenceRate, 0.0001f)
    }

    @Test
    fun generateReport_mixedGoodAndBadHabits_combinesPassedAndCleanDays() {
        val goodActivity = TrackerActivity(
            id = 1L,
            name = "Exercise",
            taskType = TaskType.BINARY,
            sentiment = Sentiment.GOOD,
            target = 1
        )
        val badActivity = TrackerActivity(
            id = 2L,
            name = "Smoking",
            taskType = TaskType.COUNTER,
            sentiment = Sentiment.BAD,
            target = 0
        )

        // Good habit: 5 check-ins over 7 days (5 passed, 2 failed)
        // Bad habit: 2 check-ins on day -1 (6 clean, 1 breached)
        val checkIns = listOf(
            checkInAt(1L, referenceDate.minusDays(0)),
            checkInAt(1L, referenceDate.minusDays(1)),
            checkInAt(1L, referenceDate.minusDays(2)),
            checkInAt(1L, referenceDate.minusDays(4)),
            checkInAt(1L, referenceDate.minusDays(6)),
            checkInAt(2L, referenceDate.minusDays(1), hour = 9),
            checkInAt(2L, referenceDate.minusDays(1), hour = 21)
        )

        val report = ReportEngine.generateReport(
            activities = listOf(goodActivity, badActivity),
            checkIns = checkIns,
            period = ReportPeriod.LAST_7_DAYS,
            referenceDate = referenceDate,
            zoneId = testZone
        )

        assertEquals(2, report.taskReports.size)
        assertEquals(5, report.goodHabitsPassedDays)
        assertEquals(7, report.goodHabitsTotalDays)
        assertEquals(6, report.badHabitsCleanDays)
        assertEquals(7, report.badHabitsTotalDays)

        // Total passed days: 5 + 6 = 11, Total days: 7 + 7 = 14
        val expectedOverallAdherence = 11f / 14f
        assertEquals(expectedOverallAdherence, report.overallAdherenceRate, 0.0001f)

        val goodReport = report.taskReports.first { it.activity.id == 1L }
        val badReport = report.taskReports.first { it.activity.id == 2L }

        assertEquals(5, goodReport.passedDays)
        assertEquals(5f / 7f, goodReport.adherenceRate, 0.0001f)
        assertEquals(6, badReport.passedDays)
        assertEquals(6f / 7f, badReport.adherenceRate, 0.0001f)
    }

    @Test
    fun generateReport_badHabitWithHarmReductionLimit_evaluatesCountsCorrectly() {
        val limitActivity = TrackerActivity(
            id = 3L,
            name = "Coffee Cups",
            taskType = TaskType.COUNTER,
            sentiment = Sentiment.BAD,
            target = 2 // Allows up to 2 cups; 3 or more breaches limit
        )

        // Day 0: 0 check-ins (pass: 0 <= 2)
        // Day -1: 1 check-in (pass: 1 <= 2)
        // Day -2: 2 check-ins (pass: 2 <= 2)
        // Day -3: 3 check-ins (fail: 3 <= 2 is false)
        // Day -4: 4 check-ins (fail: 4 <= 2 is false)
        // Day -5: 0 check-ins (pass: 0 <= 2)
        // Day -6: 1 check-in (pass: 1 <= 2)
        val checkIns = listOf(
            checkInAt(3L, referenceDate.minusDays(1), hour = 9),
            checkInAt(3L, referenceDate.minusDays(2), hour = 9),
            checkInAt(3L, referenceDate.minusDays(2), hour = 14),
            checkInAt(3L, referenceDate.minusDays(3), hour = 8),
            checkInAt(3L, referenceDate.minusDays(3), hour = 12),
            checkInAt(3L, referenceDate.minusDays(3), hour = 17),
            checkInAt(3L, referenceDate.minusDays(4), hour = 8),
            checkInAt(3L, referenceDate.minusDays(4), hour = 11),
            checkInAt(3L, referenceDate.minusDays(4), hour = 14),
            checkInAt(3L, referenceDate.minusDays(4), hour = 18),
            checkInAt(3L, referenceDate.minusDays(6), hour = 10)
        )

        val report = ReportEngine.generateReport(
            activities = listOf(limitActivity),
            checkIns = checkIns,
            period = ReportPeriod.LAST_7_DAYS,
            referenceDate = referenceDate,
            zoneId = testZone
        )

        val taskReport = report.taskReports[0]
        assertEquals(5, taskReport.passedDays) // Days 0, -1, -2, -5, -6 passed
        assertEquals(7, taskReport.totalDays)
        assertEquals(5f / 7f, taskReport.adherenceRate, 0.0001f)

        // Verify individual day statuses from dailyHistory
        val historyByDate = taskReport.dailyHistory.associateBy { it.date }

        val dayMinus1 = historyByDate[referenceDate.minusDays(1)]!!
        assertEquals(1, dayMinus1.count)
        assertEquals(2, dayMinus1.target)
        assertTrue(dayMinus1.passed)

        val dayMinus2 = historyByDate[referenceDate.minusDays(2)]!!
        assertEquals(2, dayMinus2.count)
        assertEquals(2, dayMinus2.target)
        assertTrue(dayMinus2.passed)

        val dayMinus3 = historyByDate[referenceDate.minusDays(3)]!!
        assertEquals(3, dayMinus3.count)
        assertEquals(2, dayMinus3.target)
        assertFalse(dayMinus3.passed)

        val dayMinus4 = historyByDate[referenceDate.minusDays(4)]!!
        assertEquals(4, dayMinus4.count)
        assertEquals(2, dayMinus4.target)
        assertFalse(dayMinus4.passed)
    }

    @Test
    fun generateReport_dailyHistory_isInStrictChronologicalOrder() {
        val activity = TrackerActivity(
            id = 1L,
            name = "Meditate",
            taskType = TaskType.BINARY,
            sentiment = Sentiment.GOOD,
            target = 1
        )

        // Test LAST_7_DAYS
        val report7 = ReportEngine.generateReport(
            activities = listOf(activity),
            checkIns = emptyList(),
            period = ReportPeriod.LAST_7_DAYS,
            referenceDate = referenceDate,
            zoneId = testZone
        )

        val history7 = report7.taskReports[0].dailyHistory
        assertEquals(7, history7.size)
        assertEquals(referenceDate.minusDays(6), history7.first().date)
        assertEquals(referenceDate, history7.last().date)
        for (i in 0 until history7.size - 1) {
            assertEquals(history7[i].date.plusDays(1), history7[i + 1].date)
        }

        // Test LAST_30_DAYS
        val report30 = ReportEngine.generateReport(
            activities = listOf(activity),
            checkIns = emptyList(),
            period = ReportPeriod.LAST_30_DAYS,
            referenceDate = referenceDate,
            zoneId = testZone
        )

        val history30 = report30.taskReports[0].dailyHistory
        assertEquals(30, history30.size)
        assertEquals(referenceDate.minusDays(29), history30.first().date)
        assertEquals(referenceDate, history30.last().date)
        for (i in 0 until history30.size - 1) {
            assertEquals(history30[i].date.plusDays(1), history30[i + 1].date)
        }
    }

    @Test
    fun generateReport_ignoresCheckInsOutsideWindowOrForOtherActivities() {
        val activity = TrackerActivity(
            id = 1L,
            name = "Workout",
            taskType = TaskType.BINARY,
            sentiment = Sentiment.GOOD,
            target = 1
        )

        val checkIns = listOf(
            // Out of window (7 days prior to referenceDate is day -6; day -7 is out)
            checkInAt(1L, referenceDate.minusDays(7)),
            // In future (day +1)
            checkInAt(1L, referenceDate.plusDays(1)),
            // Other activity on an in-window day
            checkInAt(999L, referenceDate.minusDays(2)),
            // Valid in-window check-in
            checkInAt(1L, referenceDate.minusDays(2))
        )

        val report = ReportEngine.generateReport(
            activities = listOf(activity),
            checkIns = checkIns,
            period = ReportPeriod.LAST_7_DAYS,
            referenceDate = referenceDate,
            zoneId = testZone
        )

        val taskReport = report.taskReports[0]
        assertEquals(1, taskReport.passedDays)
        assertEquals(7, taskReport.totalDays)
        assertEquals(1f / 7f, taskReport.adherenceRate, 0.0001f)
    }

    @Test
    fun generateReport_goodCounterHabit_multipleCheckInsPerDay_evaluatedCorrectly() {
        val counterActivity = TrackerActivity(
            id = 4L,
            name = "Pushups",
            taskType = TaskType.COUNTER,
            sentiment = Sentiment.GOOD,
            target = 3
        )

        // Day 0: 3 check-ins (pass: 3 >= 3)
        // Day -1: 2 check-ins (fail: 2 >= 3 is false)
        // Day -2: 4 check-ins (pass: 4 >= 3)
        val checkIns = listOf(
            checkInAt(4L, referenceDate, hour = 8),
            checkInAt(4L, referenceDate, hour = 12),
            checkInAt(4L, referenceDate, hour = 16),
            checkInAt(4L, referenceDate.minusDays(1), hour = 9),
            checkInAt(4L, referenceDate.minusDays(1), hour = 15),
            checkInAt(4L, referenceDate.minusDays(2), hour = 7),
            checkInAt(4L, referenceDate.minusDays(2), hour = 11),
            checkInAt(4L, referenceDate.minusDays(2), hour = 15),
            checkInAt(4L, referenceDate.minusDays(2), hour = 19)
        )

        val report = ReportEngine.generateReport(
            activities = listOf(counterActivity),
            checkIns = checkIns,
            period = ReportPeriod.LAST_7_DAYS,
            referenceDate = referenceDate,
            zoneId = testZone
        )

        val taskReport = report.taskReports[0]
        assertEquals(2, taskReport.passedDays)
        assertEquals(7, taskReport.totalDays)
        assertEquals(2f / 7f, taskReport.adherenceRate, 0.0001f)
    }

    @Test
    fun generateReport_dailyTrend_calculatesPerDayAdherenceCorrectly() {
        val goodActivity = TrackerActivity(
            id = 1L,
            name = "Exercise",
            taskType = TaskType.BINARY,
            sentiment = Sentiment.GOOD,
            target = 1
        )
        val badActivity = TrackerActivity(
            id = 2L,
            name = "Smoking",
            taskType = TaskType.BINARY,
            sentiment = Sentiment.BAD,
            target = 0
        )

        // Today (Day 0): good passed (1 check), bad passed (0 checks) -> 2/2 = 1.0f
        // Day -1: good failed (0 checks), bad passed (0 checks) -> 1/2 = 0.5f
        // Day -2: good failed (0 checks), bad failed (1 check) -> 0/2 = 0.0f
        val checkIns = listOf(
            checkInAt(1L, referenceDate, hour = 10),
            checkInAt(2L, referenceDate.minusDays(2), hour = 14)
        )

        val report = ReportEngine.generateReport(
            activities = listOf(goodActivity, badActivity),
            checkIns = checkIns,
            period = ReportPeriod.LAST_7_DAYS,
            referenceDate = referenceDate,
            zoneId = testZone
        )

        assertEquals(7, report.dailyTrend.size)
        val trendByDate = report.dailyTrend.associateBy { it.date }

        val day0 = trendByDate[referenceDate]!!
        assertEquals(2, day0.passedCount)
        assertEquals(2, day0.totalCount)
        assertEquals(1.0f, day0.adherenceRate, 0.0001f)

        val dayMinus1 = trendByDate[referenceDate.minusDays(1)]!!
        assertEquals(1, dayMinus1.passedCount)
        assertEquals(2, dayMinus1.totalCount)
        assertEquals(0.5f, dayMinus1.adherenceRate, 0.0001f)

        val dayMinus2 = trendByDate[referenceDate.minusDays(2)]!!
        assertEquals(0, dayMinus2.passedCount)
        assertEquals(2, dayMinus2.totalCount)
        assertEquals(0.0f, dayMinus2.adherenceRate, 0.0001f)
    }
}
