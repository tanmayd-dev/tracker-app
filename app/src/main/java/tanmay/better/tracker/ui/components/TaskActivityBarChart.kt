package tanmay.better.tracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tanmay.better.tracker.Sentiment
import tanmay.better.tracker.TaskDayStatus
import tanmay.better.tracker.TaskType
import tanmay.better.tracker.ui.theme.HabitColors
import tanmay.better.tracker.ui.theme.TrackerTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max

@Composable
fun TaskActivityBarChart(
    dailyHistory: List<TaskDayStatus>,
    taskType: TaskType,
    sentiment: Sentiment,
    target: Int,
    modifier: Modifier = Modifier
) {
    if (dailyHistory.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val emerald = HabitColors.emeraldPrimary()
    val crimson = HabitColors.crimsonPrimary()
    val amber = HabitColors.amberPrimary()
    val mutedColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = labelColor)
    val countStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold)

    val maxCount = remember(dailyHistory, target) {
        val maxData = dailyHistory.maxOfOrNull { it.count } ?: 0
        max(max(maxData, target), 1)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Bar Chart Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
        ) {
            val isSevenDays = dailyHistory.size <= 7
            val leftPad = if (taskType == TaskType.COUNTER && target > 0) 24.dp.toPx() else 4.dp.toPx()
            val rightPad = 4.dp.toPx()
            val topPad = 14.dp.toPx() // Room for count label on top of bar
            val bottomPad = 4.dp.toPx()

            val chartW = size.width - leftPad - rightPad
            val chartH = size.height - topPad - bottomPad

            if (chartW <= 0 || chartH <= 0) return@Canvas

            // Target reference line for Counter tasks
            if (taskType == TaskType.COUNTER && target > 0 && target <= maxCount) {
                val targetY = topPad + chartH * (1f - (target.toFloat() / maxCount.toFloat()))
                val dash = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

                // Label on left
                val targetText = textMeasurer.measure("$target", labelStyle)
                drawText(targetText, topLeft = Offset(0f, targetY - targetText.size.height / 2f))

                // Line
                drawLine(
                    color = mutedColor,
                    start = Offset(leftPad, targetY),
                    end = Offset(size.width - rightPad, targetY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dash
                )
            }

            // Bars
            val count = dailyHistory.size
            val slotWidth = chartW / count
            val barWidth = if (isSevenDays) {
                (slotWidth * 0.55f).coerceIn(8.dp.toPx(), 24.dp.toPx())
            } else {
                (slotWidth * 0.75f).coerceIn(2.dp.toPx(), 12.dp.toPx())
            }

            dailyHistory.forEachIndexed { i, day ->
                val barCenterX = leftPad + i * slotWidth + slotWidth / 2f
                val barLeft = barCenterX - barWidth / 2f

                val barColor = when {
                    day.passed -> emerald
                    sentiment == Sentiment.BAD -> crimson
                    day.count > 0 -> amber
                    else -> mutedColor
                }

                if (day.count > 0) {
                    val barHeight = (day.count.toFloat() / maxCount.toFloat()) * chartH
                    val barTop = topPad + chartH - barHeight

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(barLeft, barTop),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )

                    // Draw count number on top of bar for 7-day view
                    if (isSevenDays && taskType == TaskType.COUNTER) {
                        val text = textMeasurer.measure("${day.count}", countStyle.copy(color = barColor))
                        val textX = barCenterX - text.size.width / 2f
                        val textY = (barTop - text.size.height - 1.dp.toPx()).coerceAtLeast(0f)
                        drawText(text, topLeft = Offset(textX, textY))
                    }
                } else {
                    // Zero count: minimal baseline indicator
                    val baseHeight = 2.dp.toPx()
                    val barTop = topPad + chartH - baseHeight
                    drawRoundRect(
                        color = if (day.passed && sentiment == Sentiment.BAD) emerald else mutedColor,
                        topLeft = Offset(barLeft, barTop),
                        size = Size(barWidth, baseHeight),
                        cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                    )
                }
            }

            // Base ground line
            drawLine(
                color = mutedColor.copy(alpha = 0.35f),
                start = Offset(leftPad, topPad + chartH),
                end = Offset(size.width - rightPad, topPad + chartH),
                strokeWidth = 1.dp.toPx()
            )
        }

        // X-Axis Date Labels Row
        if (dailyHistory.size <= 7) {
            val dayFormatter = DateTimeFormatter.ofPattern("EEE")
            val leftPadDp = if (taskType == TaskType.COUNTER && target > 0) 24.dp else 4.dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = leftPadDp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dailyHistory.forEach { day ->
                    Text(
                        text = day.date.format(dayFormatter).take(2),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val monthDayFormatter = DateTimeFormatter.ofPattern("MMM d")
            val leftPadDp = if (taskType == TaskType.COUNTER && target > 0) 24.dp else 4.dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = leftPadDp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dailyHistory.first().date.format(monthDayFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dailyHistory.last().date.format(monthDayFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(name = "Bar Chart - Counter Good 7 Days", showBackground = true)
@Composable
private fun TaskActivityBarChartCounterGoodPreview() {
    val today = LocalDate.now()
    val sampleHistory = listOf(
        TaskDayStatus(today.minusDays(6), 2, 3, false),
        TaskDayStatus(today.minusDays(5), 3, 3, true),
        TaskDayStatus(today.minusDays(4), 4, 3, true),
        TaskDayStatus(today.minusDays(3), 0, 3, false),
        TaskDayStatus(today.minusDays(2), 3, 3, true),
        TaskDayStatus(today.minusDays(1), 5, 3, true),
        TaskDayStatus(today, 3, 3, true)
    )

    TrackerTheme {
        TaskActivityBarChart(
            dailyHistory = sampleHistory,
            taskType = TaskType.COUNTER,
            sentiment = Sentiment.GOOD,
            target = 3,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Bar Chart - Counter Bad 7 Days", showBackground = true)
@Composable
private fun TaskActivityBarChartCounterBadPreview() {
    val today = LocalDate.now()
    val sampleHistory = listOf(
        TaskDayStatus(today.minusDays(6), 0, 1, true),
        TaskDayStatus(today.minusDays(5), 1, 1, true),
        TaskDayStatus(today.minusDays(4), 3, 1, false),
        TaskDayStatus(today.minusDays(3), 0, 1, true),
        TaskDayStatus(today.minusDays(2), 2, 1, false),
        TaskDayStatus(today.minusDays(1), 1, 1, true),
        TaskDayStatus(today, 0, 1, true)
    )

    TrackerTheme {
        TaskActivityBarChart(
            dailyHistory = sampleHistory,
            taskType = TaskType.COUNTER,
            sentiment = Sentiment.BAD,
            target = 1,
            modifier = Modifier.padding(16.dp)
        )
    }
}
