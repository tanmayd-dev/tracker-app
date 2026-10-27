package tanmay.better.tracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tanmay.better.tracker.Sentiment
import tanmay.better.tracker.TaskType
import tanmay.better.tracker.TrackerActivity
import tanmay.better.tracker.ui.theme.HabitColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditSheet(
    activityToEdit: TrackerActivity? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, type: TaskType, sentiment: Sentiment, target: Int) -> Unit,
    onDelete: ((TrackerActivity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        TaskEditSheetContent(
            activityToEdit = activityToEdit,
            onDismiss = onDismiss,
            onSave = onSave,
            onDelete = onDelete
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditSheetContent(
    activityToEdit: TrackerActivity? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, type: TaskType, sentiment: Sentiment, target: Int) -> Unit,
    onDelete: ((TrackerActivity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var name by remember(activityToEdit) {
        mutableStateOf(activityToEdit?.name ?: "")
    }
    var taskType by remember(activityToEdit) {
        mutableStateOf(activityToEdit?.taskType ?: TaskType.BINARY)
    }
    var sentiment by remember(activityToEdit) {
        mutableStateOf(activityToEdit?.sentiment ?: Sentiment.GOOD)
    }

    val initialTarget = activityToEdit?.target ?: when (sentiment) {
        Sentiment.GOOD -> 1
        Sentiment.BAD -> 0
    }
    var target by remember(activityToEdit) {
        mutableIntStateOf(initialTarget)
    }
    var targetText by remember(activityToEdit) {
        mutableStateOf(initialTarget.toString())
    }

    var nameError by remember { mutableStateOf<String?>(null) }

    val handleSave = {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            nameError = "Task name cannot be empty"
        } else {
            nameError = null
            keyboardController?.hide()
            val finalTarget = when (taskType) {
                TaskType.BINARY -> if (sentiment == Sentiment.GOOD) 1 else 0
                TaskType.COUNTER -> {
                    val parsed = targetText.toIntOrNull() ?: target
                    val min = if (sentiment == Sentiment.GOOD) 1 else 0
                    maxOf(min, parsed)
                }
            }
            onSave(trimmed, taskType, sentiment, finalTarget)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .navigationBarsPadding()
    ) {
        // Header
        Text(
            text = if (activityToEdit != null) "Edit Task" else "New Habit / Task",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = if (activityToEdit != null)
                "Update properties and targets for this habit."
            else
                "Create a new habit to build or a vice to control.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Name input
        Text(
            text = "Task Name",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                if (nameError != null && it.isNotBlank()) {
                    nameError = null
                }
            },
            placeholder = {
                Text(
                    text = "e.g. Read 20 pages, Drink water, No junk food",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                )
            },
            isError = nameError != null,
            supportingText = {
                if (nameError != null) {
                    Text(
                        text = nameError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Task Type Selector
        Text(
            text = "Task Type",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SegmentedButton(
                selected = taskType == TaskType.BINARY,
                onClick = {
                    taskType = TaskType.BINARY
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                icon = {},
                label = {
                    Text(
                        text = "Binary (Single Check)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (taskType == TaskType.BINARY) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )

            SegmentedButton(
                selected = taskType == TaskType.COUNTER,
                onClick = {
                    taskType = TaskType.COUNTER
                    if (sentiment == Sentiment.GOOD && target < 1) {
                        target = 1
                        targetText = "1"
                    }
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                icon = {},
                label = {
                    Text(
                        text = "Counter (+/-)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (taskType == TaskType.COUNTER) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sentiment Selector
        Text(
            text = "Sentiment",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            val isGood = sentiment == Sentiment.GOOD
            SegmentedButton(
                selected = isGood,
                onClick = {
                    sentiment = Sentiment.GOOD
                    if (taskType == TaskType.COUNTER && target < 1) {
                        target = 1
                        targetText = "1"
                    }
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = HabitColors.emeraldContainer(),
                    activeContentColor = HabitColors.onEmeraldContainer()
                ),
                icon = {},
                label = {
                    Text(
                        text = "Do is Good (Habit)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isGood) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )

            val isBad = sentiment == Sentiment.BAD
            SegmentedButton(
                selected = isBad,
                onClick = {
                    sentiment = Sentiment.BAD
                    if (taskType == TaskType.COUNTER && activityToEdit == null && target == 1) {
                        target = 0
                        targetText = "0"
                    }
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = HabitColors.crimsonContainer(),
                    activeContentColor = HabitColors.onCrimsonContainer()
                ),
                icon = {},
                label = {
                    Text(
                        text = "Do is Bad (Vice/Limit)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isBad) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Target Number Field (or Stepper)
        Text(
            text = if (taskType == TaskType.COUNTER) {
                if (sentiment == Sentiment.GOOD) "Daily Target" else "Daily Limit"
            } else {
                "Daily Goal"
            },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (taskType == TaskType.BINARY) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (sentiment == Sentiment.GOOD)
                    HabitColors.emeraldContainer().copy(alpha = 0.35f)
                else
                    HabitColors.crimsonContainer().copy(alpha = 0.25f),
                border = BorderStroke(
                    1.dp,
                    if (sentiment == Sentiment.GOOD)
                        HabitColors.emeraldPrimary().copy(alpha = 0.45f)
                    else
                        HabitColors.crimsonPrimary().copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (sentiment == Sentiment.GOOD) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (sentiment == Sentiment.GOOD) HabitColors.emeraldPrimary() else HabitColors.crimsonPrimary(),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = if (sentiment == Sentiment.GOOD) "Target is 1 check" else "Avoidance (0)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (sentiment == Sentiment.GOOD)
                                "Complete once each day to reach your goal."
                            else
                                "Aim for 0 occurrences to keep this vice in check.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            val minTarget = if (sentiment == Sentiment.GOOD) 1 else 0
            val canDecrement = target > minTarget

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedIconButton(
                        onClick = {
                            if (canDecrement) {
                                val next = target - 1
                                target = next
                                targetText = next.toString()
                            }
                        },
                        enabled = canDecrement,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Text(
                            text = "−",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canDecrement)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        )
                    }

                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }
                            targetText = digits
                            digits.toIntOrNull()?.let { parsed ->
                                target = maxOf(minTarget, parsed)
                            }
                        },
                        label = {
                            Text(if (sentiment == Sentiment.GOOD) "Target Count" else "Limit Count")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (targetText.isEmpty()) {
                                    targetText = target.toString()
                                }
                                keyboardController?.hide()
                            }
                        ),
                        textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedIconButton(
                        onClick = {
                            val next = target + 1
                            target = next
                            targetText = next.toString()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase target count",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = if (sentiment == Sentiment.GOOD)
                        "Daily target count to achieve (e.g. 5 glasses, 20 pages)."
                    else if (target == 0)
                        "Limit: 0 per day (Zero tolerance avoidance)."
                    else
                        "Limit to <= $target per day (e.g. limit to <= $target coffees).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge)
            }

            Button(
                onClick = handleSave,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        if (activityToEdit != null) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    onDelete?.invoke(activityToEdit)
                },
                enabled = onDelete != null,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Delete Task",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun TaskEditSheetNewPreview() {
    MaterialTheme {
        Surface {
            TaskEditSheetContent(
                activityToEdit = null,
                onDismiss = {},
                onSave = { _, _, _, _ -> },
                onDelete = null
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskEditSheetEditCounterPreview() {
    MaterialTheme {
        Surface {
            TaskEditSheetContent(
                activityToEdit = TrackerActivity(
                    id = 1L,
                    name = "Drink 8 Glasses of Water",
                    taskType = TaskType.COUNTER,
                    sentiment = Sentiment.GOOD,
                    target = 8
                ),
                onDismiss = {},
                onSave = { _, _, _, _ -> },
                onDelete = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskEditSheetEditBadBinaryPreview() {
    MaterialTheme {
        Surface {
            TaskEditSheetContent(
                activityToEdit = TrackerActivity(
                    id = 2L,
                    name = "Late Night Snacking",
                    taskType = TaskType.BINARY,
                    sentiment = Sentiment.BAD,
                    target = 0
                ),
                onDismiss = {},
                onSave = { _, _, _, _ -> },
                onDelete = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskEditSheetEditBadCounterPreview() {
    MaterialTheme {
        Surface {
            TaskEditSheetContent(
                activityToEdit = TrackerActivity(
                    id = 3L,
                    name = "Coffee Consumption",
                    taskType = TaskType.COUNTER,
                    sentiment = Sentiment.BAD,
                    target = 2
                ),
                onDismiss = {},
                onSave = { _, _, _, _ -> },
                onDelete = {}
            )
        }
    }
}
