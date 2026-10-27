package tanmay.better.tracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tanmay.better.tracker.HabitCompliance
import tanmay.better.tracker.Sentiment
import tanmay.better.tracker.TaskType
import tanmay.better.tracker.TrackerActivity
import tanmay.better.tracker.ui.theme.HabitColors

@Composable
fun TaskRow(
    activity: TrackerActivity,
    countToday: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onToggleBinary: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBreached = activity.sentiment == Sentiment.BAD &&
        !HabitCompliance.isDayPassed(activity.sentiment, countToday, activity.target)

    val isAchieved = activity.sentiment == Sentiment.GOOD &&
        HabitCompliance.isDayPassed(activity.sentiment, countToday, activity.target) &&
        (activity.target > 0 || countToday > 0)

    val cardBorder = when {
        isBreached -> BorderStroke(1.dp, HabitColors.crimsonPrimary().copy(alpha = 0.5f))
        isAchieved -> BorderStroke(1.dp, HabitColors.emeraldPrimary().copy(alpha = 0.4f))
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }

    val cardContainerColor = when {
        isBreached -> HabitColors.crimsonContainer().copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        border = cardBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activity.name,
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
                    // Task type badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = when (activity.taskType) {
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
                    val (targetText, targetBg, targetFg) = when (activity.sentiment) {
                        Sentiment.GOOD -> {
                            val label = if (activity.taskType == TaskType.COUNTER) {
                                "Target: ${activity.target}"
                            } else {
                                "Daily Goal"
                            }
                            Triple(
                                label,
                                if (isAchieved) HabitColors.emeraldContainer() else MaterialTheme.colorScheme.surfaceVariant,
                                if (isAchieved) HabitColors.onEmeraldContainer() else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Sentiment.BAD -> {
                            val label = if (activity.taskType == TaskType.COUNTER) {
                                "Limit: ${activity.target}"
                            } else {
                                "Avoidance"
                            }
                            if (isBreached) {
                                Triple(
                                    label,
                                    HabitColors.crimsonContainer(),
                                    HabitColors.onCrimsonContainer()
                                )
                            } else {
                                Triple(
                                    label,
                                    HabitColors.calmContainer(),
                                    HabitColors.onCalmContainer()
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = targetBg
                    ) {
                        Text(
                            text = targetText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = targetFg,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right side: Counter or Binary toggle control
            when (activity.taskType) {
                TaskType.COUNTER -> {
                    CounterControls(
                        count = countToday,
                        target = activity.target,
                        sentiment = activity.sentiment,
                        onIncrement = onIncrement,
                        onDecrement = onDecrement
                    )
                }
                TaskType.BINARY -> {
                    BinaryToggleControl(
                        isDone = countToday > 0,
                        sentiment = activity.sentiment,
                        onToggle = onToggleBinary
                    )
                }
            }

            // Overflow menu (3-dots)
            Box {
                var menuExpanded by remember { mutableStateOf(false) }

                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options for ${activity.name}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Delete",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun TaskRowPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TaskRow(
                activity = TrackerActivity(1L, "Morning Run", TaskType.COUNTER, Sentiment.GOOD, 5),
                countToday = 2,
                onIncrement = {},
                onDecrement = {},
                onToggleBinary = {},
                onEdit = {},
                onDelete = {}
            )
            TaskRow(
                activity = TrackerActivity(2L, "Morning Run (Done)", TaskType.COUNTER, Sentiment.GOOD, 5),
                countToday = 5,
                onIncrement = {},
                onDecrement = {},
                onToggleBinary = {},
                onEdit = {},
                onDelete = {}
            )
            TaskRow(
                activity = TrackerActivity(3L, "Read 30 mins", TaskType.BINARY, Sentiment.GOOD, 1),
                countToday = 0,
                onIncrement = {},
                onDecrement = {},
                onToggleBinary = {},
                onEdit = {},
                onDelete = {}
            )
            TaskRow(
                activity = TrackerActivity(4L, "Read 30 mins (Done)", TaskType.BINARY, Sentiment.GOOD, 1),
                countToday = 1,
                onIncrement = {},
                onDecrement = {},
                onToggleBinary = {},
                onEdit = {},
                onDelete = {}
            )
            TaskRow(
                activity = TrackerActivity(5L, "Late Night Snacking", TaskType.BINARY, Sentiment.BAD, 0),
                countToday = 0,
                onIncrement = {},
                onDecrement = {},
                onToggleBinary = {},
                onEdit = {},
                onDelete = {}
            )
            TaskRow(
                activity = TrackerActivity(6L, "Late Night Snacking (Breached)", TaskType.BINARY, Sentiment.BAD, 0),
                countToday = 1,
                onIncrement = {},
                onDecrement = {},
                onToggleBinary = {},
                onEdit = {},
                onDelete = {}
            )
            TaskRow(
                activity = TrackerActivity(7L, "Coffee Intake", TaskType.COUNTER, Sentiment.BAD, 2),
                countToday = 1,
                onIncrement = {},
                onDecrement = {},
                onToggleBinary = {},
                onEdit = {},
                onDelete = {}
            )
            TaskRow(
                activity = TrackerActivity(8L, "Coffee Intake (Breached)", TaskType.COUNTER, Sentiment.BAD, 2),
                countToday = 3,
                onIncrement = {},
                onDecrement = {},
                onToggleBinary = {},
                onEdit = {},
                onDelete = {}
            )
        }
    }
}
