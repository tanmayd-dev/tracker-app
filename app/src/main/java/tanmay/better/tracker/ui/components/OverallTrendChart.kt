package tanmay.better.tracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tanmay.better.tracker.DayAdherence
import tanmay.better.tracker.ReportPeriod
import tanmay.better.tracker.ui.theme.HabitColors
import tanmay.better.tracker.ui.theme.TrackerTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun OverallTrendChart(
    dailyTrend: List<DayAdherence>,
    period: ReportPeriod,
    modifier: Modifier = Modifier
) {
    if (dailyTrend.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = labelColor)

    val averageAdherence = remember(dailyTrend) {
        if (dailyTrend.isNotEmpty()) dailyTrend.map { it.adherenceRate }.average().toFloat() else 0f
    }
    val avgPct = (averageAdherence * 100).toInt()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Adherence Trend",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (period) {
                            ReportPeriod.LAST_7_DAYS -> "Daily consistency (Last 7 Days)"
                            ReportPeriod.LAST_30_DAYS -> "Daily consistency (Last 30 Days)"
                            ReportPeriod.TODAY -> "Today's consistency"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Average badge
                Text(
                    text = "Avg: $avgPct%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = HabitColors.adherenceColor(averageAdherence)
                )
            }

            // Canvas Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val leftPad = 32.dp.toPx()
                val rightPad = 12.dp.toPx()
                val topPad = 12.dp.toPx()
                val bottomPad = 16.dp.toPx()

                val chartW = size.width - leftPad - rightPad
                val chartH = size.height - topPad - bottomPad

                if (chartW <= 0 || chartH <= 0) return@Canvas

                val y100 = topPad
                val y50 = topPad + chartH * 0.5f
                val y0 = topPad + chartH

                // Draw Y-axis labels & dashed guidelines
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                // 100%
                val text100 = textMeasurer.measure("100%", labelStyle)
                drawText(text100, topLeft = Offset(0f, y100 - text100.size.height / 2f))
                drawLine(
                    color = gridColor,
                    start = Offset(leftPad, y100),
                    end = Offset(size.width - rightPad, y100),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect
                )

                // 50%
                val text50 = textMeasurer.measure("50%", labelStyle)
                drawText(text50, topLeft = Offset(0f, y50 - text50.size.height / 2f))
                drawLine(
                    color = gridColor,
                    start = Offset(leftPad, y50),
                    end = Offset(size.width - rightPad, y50),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect
                )

                // 0%
                val text0 = textMeasurer.measure("0%", labelStyle)
                drawText(text0, topLeft = Offset(0f, y0 - text0.size.height / 2f))
                drawLine(
                    color = gridColor,
                    start = Offset(leftPad, y0),
                    end = Offset(size.width - rightPad, y0),
                    strokeWidth = 1.dp.toPx()
                )

                val points = dailyTrend.mapIndexed { i, day ->
                    val x = if (dailyTrend.size > 1) {
                        leftPad + i * (chartW / (dailyTrend.size - 1))
                    } else {
                        leftPad + chartW / 2f
                    }
                    val y = topPad + (1f - day.adherenceRate.coerceIn(0f, 1f)) * chartH
                    Offset(x, y)
                }

                if (points.isNotEmpty()) {
                    // Fill path under the line
                    val fillPath = Path().apply {
                        moveTo(points.first().x, y0)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, y0)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = 0.28f),
                                lineColor.copy(alpha = 0.02f)
                            ),
                            startY = topPad,
                            endY = y0
                        )
                    )

                    // Line path
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }

                    drawPath(
                        path = linePath,
                        color = lineColor,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Points on nodes (draw dots for up to 14 days, or key markers for 30 days)
                    val drawAllDots = dailyTrend.size <= 14
                    points.forEachIndexed { idx, point ->
                        if (drawAllDots || idx == 0 || idx == points.lastIndex || idx % 5 == 0) {
                            drawCircle(
                                color = lineColor,
                                radius = 4.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }
            }

            // X-Axis Date Labels Row
            if (dailyTrend.size == 7) {
                val dayFormatter = DateTimeFormatter.ofPattern("EEE")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dailyTrend.forEach { day ->
                        Text(
                            text = day.date.format(dayFormatter).take(2),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (dailyTrend.size > 7) {
                val monthDayFormatter = DateTimeFormatter.ofPattern("MMM d")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = dailyTrend.first().date.format(monthDayFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dailyTrend[dailyTrend.size / 2].date.format(monthDayFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dailyTrend.last().date.format(monthDayFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(name = "Overall Trend - 7 Days", showBackground = true)
@Composable
private fun OverallTrendChart7DaysPreview() {
    val today = LocalDate.now()
    val sampleTrend = listOf(
        DayAdherence(today.minusDays(6), 0.6f, 3, 5),
        DayAdherence(today.minusDays(5), 0.8f, 4, 5),
        DayAdherence(today.minusDays(4), 0.4f, 2, 5),
        DayAdherence(today.minusDays(3), 1.0f, 5, 5),
        DayAdherence(today.minusDays(2), 0.8f, 4, 5),
        DayAdherence(today.minusDays(1), 0.6f, 3, 5),
        DayAdherence(today, 1.0f, 5, 5)
    )

    TrackerTheme {
        OverallTrendChart(
            dailyTrend = sampleTrend,
            period = ReportPeriod.LAST_7_DAYS,
            modifier = Modifier.padding(16.dp)
        )
    }
}
