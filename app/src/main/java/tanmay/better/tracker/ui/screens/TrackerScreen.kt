package tanmay.better.tracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tanmay.better.tracker.ActivityCheckIn
import tanmay.better.tracker.DateUtils
import tanmay.better.tracker.Sentiment
import tanmay.better.tracker.TaskType
import tanmay.better.tracker.TrackerActivity
import tanmay.better.tracker.ui.components.TaskRow
import tanmay.better.tracker.ui.theme.TrackerTheme

@Composable
fun TrackerScreen(
    activities: List<TrackerActivity>,
    activityCheckIns: List<ActivityCheckIn>,
    onIncrement: (TrackerActivity) -> Unit,
    onDecrement: (TrackerActivity) -> Unit,
    onToggleBinary: (TrackerActivity) -> Unit,
    onEditTask: (TrackerActivity) -> Unit,
    onDeleteTask: (TrackerActivity) -> Unit,
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
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = "No Habits or Tasks Yet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Tap the (+) button below to add your first habit or task and start building consistency.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        val (startOfDay, endOfDay) = DateUtils.getDayStartEnd()
        val todayCounts = remember(activityCheckIns, startOfDay, endOfDay) {
            activityCheckIns
                .filter { it.timestamp in startOfDay until endOfDay }
                .groupBy { it.activityId }
                .mapValues { it.value.size }
        }

        val sortedActivities = remember(activities) {
            activities.sortedBy { it.id }
        }

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = sortedActivities,
                key = { it.id }
            ) { activity ->
                val countToday = todayCounts[activity.id] ?: 0
                TaskRow(
                    activity = activity,
                    countToday = countToday,
                    onIncrement = { onIncrement(activity) },
                    onDecrement = { onDecrement(activity) },
                    onToggleBinary = { onToggleBinary(activity) },
                    onEdit = { onEditTask(activity) },
                    onDelete = { onDeleteTask(activity) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TrackerScreenPreview() {
    TrackerTheme {
        TrackerScreen(
            activities = listOf(
                TrackerActivity(1L, "Daily Reading", TaskType.BINARY, Sentiment.GOOD, 1),
                TrackerActivity(2L, "Water Intake (Glasses)", TaskType.COUNTER, Sentiment.GOOD, 8),
                TrackerActivity(3L, "No Fast Food", TaskType.BINARY, Sentiment.BAD, 0),
                TrackerActivity(4L, "Coffee Intake", TaskType.COUNTER, Sentiment.BAD, 2)
            ),
            activityCheckIns = listOf(
                ActivityCheckIn(1L, System.currentTimeMillis()),
                ActivityCheckIn(2L, System.currentTimeMillis()),
                ActivityCheckIn(2L, System.currentTimeMillis() - 1000),
                ActivityCheckIn(4L, System.currentTimeMillis())
            ),
            onIncrement = {},
            onDecrement = {},
            onToggleBinary = {},
            onEditTask = {},
            onDeleteTask = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrackerScreenEmptyPreview() {
    TrackerTheme {
        TrackerScreen(
            activities = emptyList(),
            activityCheckIns = emptyList(),
            onIncrement = {},
            onDecrement = {},
            onToggleBinary = {},
            onEditTask = {},
            onDeleteTask = {}
        )
    }
}
