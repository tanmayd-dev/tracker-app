package tanmay.better.tracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tanmay.better.tracker.Sentiment
import tanmay.better.tracker.ui.theme.HabitColors

@Composable
fun BinaryToggleControl(
    isDone: Boolean,
    sentiment: Sentiment,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (sentiment) {
        Sentiment.GOOD -> {
            if (isDone) {
                Surface(
                    onClick = onToggle,
                    shape = CircleShape,
                    color = HabitColors.emeraldPrimary(),
                    modifier = modifier
                        .size(40.dp)
                        .semantics {
                            role = Role.Checkbox
                            contentDescription = "Completed today. Tap to undo."
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else {
                Surface(
                    onClick = onToggle,
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = modifier
                        .size(40.dp)
                        .semantics {
                            role = Role.Checkbox
                            contentDescription = "Not completed today. Tap to check in."
                        }
                ) {
                    // Empty outlined circle
                }
            }
        }
        Sentiment.BAD -> {
            if (!isDone) {
                // Clean state (count == 0)
                Surface(
                    onClick = onToggle,
                    shape = RoundedCornerShape(20.dp),
                    color = HabitColors.calmGreenContainer(),
                    border = BorderStroke(1.dp, HabitColors.calmGreenBorder()),
                    modifier = modifier
                        .defaultMinSize(minHeight = 36.dp)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Status: Clean. Tap to mark breached."
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = HabitColors.calmGreenText(),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Clean",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = HabitColors.calmGreenText()
                        )
                    }
                }
            } else {
                // Breached state (count > 0)
                Surface(
                    onClick = onToggle,
                    shape = RoundedCornerShape(20.dp),
                    color = HabitColors.crimsonContainer(),
                    border = BorderStroke(1.dp, HabitColors.crimsonPrimary()),
                    modifier = modifier
                        .defaultMinSize(minHeight = 36.dp)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Status: Breached. Tap to mark clean."
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = HabitColors.onCrimsonContainer(),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Breached",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = HabitColors.onCrimsonContainer()
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun BinaryToggleControlPreview() {
    MaterialTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BinaryToggleControl(isDone = false, sentiment = Sentiment.GOOD, onToggle = {})
            BinaryToggleControl(isDone = true, sentiment = Sentiment.GOOD, onToggle = {})
            BinaryToggleControl(isDone = false, sentiment = Sentiment.BAD, onToggle = {})
            BinaryToggleControl(isDone = true, sentiment = Sentiment.BAD, onToggle = {})
        }
    }
}
