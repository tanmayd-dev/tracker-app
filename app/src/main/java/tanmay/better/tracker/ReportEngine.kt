package tanmay.better.tracker

import java.time.LocalDate
import java.time.ZoneId

enum class ReportPeriod(val days: Int, val title: String) {
    TODAY(1, "Today"),
    LAST_7_DAYS(7, "7 Days"),
    LAST_30_DAYS(30, "30 Days")
}

data class TaskDayStatus(
    val date: LocalDate,
    val count: Int,
    val target: Int,
    val passed: Boolean
)

data class DayAdherence(
    val date: LocalDate,
    val adherenceRate: Float,
    val passedCount: Int,
    val totalCount: Int
)

data class TaskReport(
    val activity: TrackerActivity,
    val passedDays: Int,
    val totalDays: Int,
    val adherenceRate: Float,
    val dailyHistory: List<TaskDayStatus>
)

data class ConsolidatedReport(
    val period: ReportPeriod,
    val overallAdherenceRate: Float,
    val goodHabitsPassedDays: Int,
    val goodHabitsTotalDays: Int,
    val badHabitsCleanDays: Int,
    val badHabitsTotalDays: Int,
    val taskReports: List<TaskReport>,
    val dailyTrend: List<DayAdherence> = emptyList()
)

object ReportEngine {
    fun generateReport(
        activities: List<TrackerActivity>,
        checkIns: List<ActivityCheckIn>,
        period: ReportPeriod,
        referenceDate: LocalDate = LocalDate.now(ZoneId.systemDefault()),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): ConsolidatedReport {
        val dates = (0 until period.days).map { offset ->
            referenceDate.minusDays((period.days - 1 - offset).toLong())
        }

        val checkInsByActivity = checkIns.groupBy { it.activityId }

        val taskReports = activities.map { activity ->
            val activityCheckIns = checkInsByActivity[activity.id].orEmpty()
            val dailyHistory = dates.map { date ->
                val (dayStart, dayEnd) = DateUtils.getDayStartEnd(date, zoneId)
                val count = activityCheckIns.count { checkIn ->
                    checkIn.timestamp in dayStart until dayEnd
                }
                val passed = HabitCompliance.isDayPassed(activity.sentiment, count, activity.target)
                TaskDayStatus(
                    date = date,
                    count = count,
                    target = activity.target,
                    passed = passed
                )
            }
            val passedDays = dailyHistory.count { it.passed }
            val totalDays = dailyHistory.size
            val adherenceRate = if (totalDays > 0) passedDays.toFloat() / totalDays.toFloat() else 0f

            TaskReport(
                activity = activity,
                passedDays = passedDays,
                totalDays = totalDays,
                adherenceRate = adherenceRate,
                dailyHistory = dailyHistory
            )
        }

        val goodHabitsReports = taskReports.filter { it.activity.sentiment == Sentiment.GOOD }
        val badHabitsReports = taskReports.filter { it.activity.sentiment == Sentiment.BAD }

        val goodHabitsPassedDays = goodHabitsReports.sumOf { it.passedDays }
        val goodHabitsTotalDays = goodHabitsReports.sumOf { it.totalDays }

        val badHabitsCleanDays = badHabitsReports.sumOf { it.passedDays }
        val badHabitsTotalDays = badHabitsReports.sumOf { it.totalDays }

        val totalPassed = goodHabitsPassedDays + badHabitsCleanDays
        val totalDays = goodHabitsTotalDays + badHabitsTotalDays
        val overallAdherenceRate = if (totalDays > 0) totalPassed.toFloat() / totalDays.toFloat() else 0f

        val dailyTrend = dates.mapIndexed { index, date ->
            val passedCount = taskReports.count { it.dailyHistory[index].passed }
            val totalCount = taskReports.size
            val adherenceRate = if (totalCount > 0) passedCount.toFloat() / totalCount.toFloat() else 0f
            DayAdherence(
                date = date,
                adherenceRate = adherenceRate,
                passedCount = passedCount,
                totalCount = totalCount
            )
        }

        return ConsolidatedReport(
            period = period,
            overallAdherenceRate = overallAdherenceRate,
            goodHabitsPassedDays = goodHabitsPassedDays,
            goodHabitsTotalDays = goodHabitsTotalDays,
            badHabitsCleanDays = badHabitsCleanDays,
            badHabitsTotalDays = badHabitsTotalDays,
            taskReports = taskReports,
            dailyTrend = dailyTrend
        )
    }
}
