package tanmay.better.tracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tanmay.better.tracker.HabitCompliance
import tanmay.better.tracker.Sentiment
import tanmay.better.tracker.ui.theme.HabitColors

@Composable
fun CounterControls(
    count: Int,
    target: Int,
    sentiment: Sentiment,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canDecrement = count > 0

    val isAchieved = sentiment == Sentiment.GOOD &&
        HabitCompliance.isDayPassed(sentiment, count, target) &&
        (target > 0 || count > 0)

    val isBreached = sentiment == Sentiment.BAD &&
        !HabitCompliance.isDayPassed(sentiment, count, target)

    val containerColor = when {
        isBreached -> HabitColors.crimsonContainer().copy(alpha = 0.55f)
        isAchieved -> HabitColors.emeraldContainer().copy(alpha = 0.55f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }

    val borderColor = when {
        isBreached -> HabitColors.crimsonPrimary().copy(alpha = 0.65f)
        isAchieved -> HabitColors.emeraldPrimary().copy(alpha = 0.55f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    val countTextColor = when {
        isBreached -> HabitColors.crimsonPrimary()
        isAchieved -> HabitColors.emeraldPrimary()
        else -> MaterialTheme.colorScheme.onSurface
    }

    val incrementTint = when {
        isBreached -> HabitColors.crimsonPrimary()
        isAchieved -> HabitColors.emeraldPrimary()
        else -> MaterialTheme.colorScheme.primary
    }

    val decrementTextColor = if (canDecrement) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            IconButton(
                onClick = onDecrement,
                enabled = canDecrement,
                modifier = Modifier
                    .size(36.dp)
                    .semantics { contentDescription = "Decrease count" }
            ) {
                Text(
                    text = "−",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = decrementTextColor
                )
            }

            Box(
                modifier = Modifier
                    .widthIn(min = 44.dp)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = countTextColor
                    )
                    if (target > 0) {
                        Text(
                            text = " / $target",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(
                onClick = onIncrement,
                modifier = Modifier
                    .size(36.dp)
                    .semantics { contentDescription = "Increase count" }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = incrementTint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CounterControlsPreview() {
    MaterialTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CounterControls(count = 1, target = 3, sentiment = Sentiment.GOOD, onIncrement = {}, onDecrement = {})
            CounterControls(count = 3, target = 3, sentiment = Sentiment.GOOD, onIncrement = {}, onDecrement = {})
            CounterControls(count = 0, target = 2, sentiment = Sentiment.BAD, onIncrement = {}, onDecrement = {})
            CounterControls(count = 3, target = 2, sentiment = Sentiment.BAD, onIncrement = {}, onDecrement = {})
        }
    }
}
