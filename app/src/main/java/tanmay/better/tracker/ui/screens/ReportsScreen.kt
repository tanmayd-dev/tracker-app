package tanmay.better.tracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.ZoneId
import tanmay.better.tracker.ActivityCheckIn
import tanmay.better.tracker.ReportEngine
import tanmay.better.tracker.ReportPeriod
import tanmay.better.tracker.Sentiment
import tanmay.better.tracker.TaskType
import tanmay.better.tracker.TrackerActivity
import tanmay.better.tracker.ui.components.ReportSummaryCards
import tanmay.better.tracker.ui.components.TaskReportItem
import tanmay.better.tracker.ui.theme.TrackerTheme

@Composable
fun ReportsScreen(
    activities: List<TrackerActivity>,
    checkIns: List<ActivityCheckIn>,
    modifier: Modifier = Modifier
) {
    if (activities.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                Text(
                    text = "No Habits Tracked Yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "No habits tracked yet. Create a task in the Tracker to start building reports.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        var selectedPeriod by remember { mutableStateOf(ReportPeriod.LAST_7_DAYS) }

        val report = remember(activities, checkIns, selectedPeriod) {
            ReportEngine.generateReport(activities, checkIns, selectedPeriod)
        }

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period Selector Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReportPeriod.entries.forEach { period ->
                        val chipLabel = when (period) {
                            ReportPeriod.TODAY -> "Today"
                            ReportPeriod.LAST_7_DAYS -> "Last 7 Days"
                            ReportPeriod.LAST_30_DAYS -> "Last 30 Days"
                        }
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period },
                            label = {
                                Text(
                                    text = chipLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedPeriod == period) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            // Summary Cards (Hero Card + Secondary Cards)
            item {
                ReportSummaryCards(report = report)
            }

            // Section Header: Task Breakdown
            item {
                Column {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Task Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Task Report Items
            items(
                items = report.taskReports,
                key = { it.activity.id }
            ) { taskReport ->
                TaskReportItem(report = taskReport)
            }
        }
    }
}

@Preview(name = "Reports Screen - Content", showBackground = true)
@Composable
private fun ReportsScreenContentPreview() {
    val activities = listOf(
        TrackerActivity(1L, "Morning Meditation", TaskType.BINARY, Sentiment.GOOD, 1),
        TrackerActivity(2L, "Gym Workout", TaskType.COUNTER, Sentiment.GOOD, 3),
        TrackerActivity(3L, "Late Night Snacking", TaskType.COUNTER, Sentiment.BAD, 1)
    )

    val today = LocalDate.now(ZoneId.systemDefault())
    val zoneId = ZoneId.systemDefault()
    val checkIns = buildList {
        // Activity 1: completed 6 of 7 days
        for (i in 0 until 6) {
            val date = today.minusDays(i.toLong())
            val epoch = date.atStartOfDay(zoneId).toEpochSecond() * 1000 + 3600_000
            add(ActivityCheckIn(1L, epoch))
        }
        // Activity 2: 3 checks today, 3 checks yesterday, 2 checks day before
        val todayEpoch = today.atStartOfDay(zoneId).toEpochSecond() * 1000 + 3600_000
        val yesterdayEpoch = today.minusDays(1).atStartOfDay(zoneId).toEpochSecond() * 1000 + 3600_000
        repeat(3) { add(ActivityCheckIn(2L, todayEpoch + it * 1000)) }
        repeat(3) { add(ActivityCheckIn(2L, yesterdayEpoch + it * 1000)) }
        // Activity 3: breached 2 days ago
        val twoDaysAgo = today.minusDays(2).atStartOfDay(zoneId).toEpochSecond() * 1000 + 3600_000
        repeat(2) { add(ActivityCheckIn(3L, twoDaysAgo + it * 1000)) }
    }

    TrackerTheme {
        ReportsScreen(
            activities = activities,
            checkIns = checkIns
        )
    }
}

@Preview(name = "Reports Screen - Empty", showBackground = true)
@Composable
private fun ReportsScreenEmptyPreview() {
    TrackerTheme {
        ReportsScreen(
            activities = emptyList(),
            checkIns = emptyList()
        )
    }
}
