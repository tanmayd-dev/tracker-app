package tanmay.better.tracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import kotlin.math.roundToInt
import tanmay.better.tracker.Sentiment
import tanmay.better.tracker.TaskDayStatus
import tanmay.better.tracker.TaskReport
import tanmay.better.tracker.TaskType
import tanmay.better.tracker.TrackerActivity
import tanmay.better.tracker.ui.theme.HabitColors
import tanmay.better.tracker.ui.theme.TrackerTheme

@Composable
fun TaskReportItem(
    report: TaskReport,
    modifier: Modifier = Modifier
) {
    val adherencePct = (report.adherenceRate * 100).roundToInt()
    val adherenceColor = HabitColors.adherenceColor(report.adherenceRate)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Task Name & Badges on Left, Consistency Score on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = report.activity.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Task Type badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = when (report.activity.taskType) {
                                    TaskType.COUNTER -> "Counter"
                                    TaskType.BINARY -> "Daily Check"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Target / Sentiment badge
                        val (targetLabel, targetBg, targetFg) = when (report.activity.sentiment) {
                            Sentiment.GOOD -> {
                                val label = if (report.activity.taskType == TaskType.COUNTER) {
                                    "Target: ${report.activity.target}"
                                } else {
                                    "Daily Goal"
                                }
                                Triple(
                                    label,
                                    HabitColors.emeraldContainer().copy(alpha = 0.5f),
                                    HabitColors.onEmeraldContainer()
                                )
                            }
                            Sentiment.BAD -> {
                                val label = if (report.activity.taskType == TaskType.COUNTER) {
                                    "Limit: ${report.activity.target}"
                                } else {
                                    "Avoidance"
                                }
                                Triple(
                                    label,
                                    HabitColors.calmContainer(),
                                    HabitColors.onCalmContainer()
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = targetBg
                        ) {
                            Text(
                                text = targetLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = targetFg,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Consistency Score Badge e.g. "6/7 days (86%)"
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "${report.passedDays}/${report.totalDays} days ($adherencePct%)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = adherenceColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Mini Day Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (report.dailyHistory.size == 1) "Today" else "${report.dailyHistory.size}-Day History",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(if (report.dailyHistory.size > 14) 4.dp else 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    report.dailyHistory.forEach { dayStatus ->
                        val dotColor = when {
                            dayStatus.passed -> HabitColors.emeraldPrimary()
                            report.activity.sentiment == Sentiment.BAD -> HabitColors.crimsonPrimary().copy(alpha = 0.85f)
                            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                        }
                        val dotSize = if (report.dailyHistory.size > 14) 7.dp else 9.dp
                        Box(
                            modifier = Modifier
                                .size(dotSize)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
            }

            // Daily Activity Bar Chart
            if (report.dailyHistory.size > 1) {
                TaskActivityBarChart(
                    dailyHistory = report.dailyHistory,
                    taskType = report.activity.taskType,
                    sentiment = report.activity.sentiment,
                    target = report.activity.target
                )
            }

            // Progress Bar showing adherence rate
            LinearProgressIndicator(
                progress = { report.adherenceRate },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = adherenceColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Preview(name = "Task Report Item - Good Habit", showBackground = true)
@Composable
private fun TaskReportItemGoodPreview() {
    val today = LocalDate.now()
    TrackerTheme {
        TaskReportItem(
            report = TaskReport(
                activity = TrackerActivity(1L, "Morning Run", TaskType.COUNTER, Sentiment.GOOD, 3),
                passedDays = 6,
                totalDays = 7,
                adherenceRate = 6f / 7f,
                dailyHistory = (0 until 7).map {
                    TaskDayStatus(today.minusDays((6 - it).toLong()), if (it == 2) 1 else 3, 3, it != 2)
                }
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Task Report Item - Bad Habit", showBackground = true)
@Composable
private fun TaskReportItemBadPreview() {
    val today = LocalDate.now()
    TrackerTheme {
        TaskReportItem(
            report = TaskReport(
                activity = TrackerActivity(2L, "Sweets & Pastries", TaskType.COUNTER, Sentiment.BAD, 1),
                passedDays = 4,
                totalDays = 7,
                adherenceRate = 4f / 7f,
                dailyHistory = (0 until 7).map {
                    val passed = it in listOf(0, 1, 3, 6)
                    TaskDayStatus(today.minusDays((6 - it).toLong()), if (passed) 0 else 3, 1, passed)
                }
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
