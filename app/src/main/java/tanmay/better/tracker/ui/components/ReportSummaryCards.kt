package tanmay.better.tracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import tanmay.better.tracker.ConsolidatedReport
import tanmay.better.tracker.ReportPeriod
import tanmay.better.tracker.ui.theme.HabitColors
import tanmay.better.tracker.ui.theme.TrackerTheme

@Composable
fun ReportSummaryCards(
    report: ConsolidatedReport,
    modifier: Modifier = Modifier
) {
    val overallPercent = (report.overallAdherenceRate * 100).roundToInt()
    val adherenceColor = HabitColors.adherenceColor(report.overallAdherenceRate)
    val adherenceContainer = HabitColors.adherenceContainer(report.overallAdherenceRate)

    val totalPassed = report.goodHabitsPassedDays + report.badHabitsCleanDays
    val totalDays = report.goodHabitsTotalDays + report.badHabitsTotalDays

    val goodHabitsPct = if (report.goodHabitsTotalDays > 0) {
        (report.goodHabitsPassedDays.toFloat() / report.goodHabitsTotalDays * 100).roundToInt()
    } else {
        0
    }
    val goodHabitsRate = if (report.goodHabitsTotalDays > 0) {
        report.goodHabitsPassedDays.toFloat() / report.goodHabitsTotalDays
    } else {
        0f
    }

    val badHabitsPct = if (report.badHabitsTotalDays > 0) {
        (report.badHabitsCleanDays.toFloat() / report.badHabitsTotalDays * 100).roundToInt()
    } else {
        0
    }
    val badHabitsRate = if (report.badHabitsTotalDays > 0) {
        report.badHabitsCleanDays.toFloat() / report.badHabitsTotalDays
    } else {
        0f
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Card: Overall Adherence Percentage
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = adherenceContainer.copy(alpha = 0.18f)
            ),
            border = BorderStroke(1.dp, adherenceColor.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Overall Adherence",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = report.period.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "$overallPercent% Adherence",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = adherenceColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$totalPassed of $totalDays task-days completed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { report.overallAdherenceRate },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = adherenceColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // Secondary Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Good Habits Met Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(HabitColors.emeraldPrimary())
                        )
                        Text(
                            text = "Good Habits Met",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${report.goodHabitsPassedDays}/${report.goodHabitsTotalDays} days • $goodHabitsPct%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { goodHabitsRate },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = HabitColors.emeraldPrimary(),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // Bad Habits Avoided Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(HabitColors.calmGreenBorder())
                        )
                        Text(
                            text = "Bad Habits Avoided",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${report.badHabitsCleanDays}/${report.badHabitsTotalDays} clean • $badHabitsPct%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { badHabitsRate },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = HabitColors.calmGreenBorder(),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(name = "Summary Cards - High Adherence", showBackground = true)
@Composable
private fun ReportSummaryCardsHighPreview() {
    TrackerTheme {
        ReportSummaryCards(
            report = ConsolidatedReport(
                period = ReportPeriod.LAST_7_DAYS,
                overallAdherenceRate = 0.86f,
                goodHabitsPassedDays = 18,
                goodHabitsTotalDays = 20,
                badHabitsCleanDays = 10,
                badHabitsTotalDays = 12,
                taskReports = emptyList()
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Summary Cards - Amber Adherence", showBackground = true)
@Composable
private fun ReportSummaryCardsAmberPreview() {
    TrackerTheme {
        ReportSummaryCards(
            report = ConsolidatedReport(
                period = ReportPeriod.LAST_7_DAYS,
                overallAdherenceRate = 0.65f,
                goodHabitsPassedDays = 13,
                goodHabitsTotalDays = 20,
                badHabitsCleanDays = 6,
                badHabitsTotalDays = 10,
                taskReports = emptyList()
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Summary Cards - Low Adherence", showBackground = true)
@Composable
private fun ReportSummaryCardsLowPreview() {
    TrackerTheme {
        ReportSummaryCards(
            report = ConsolidatedReport(
                period = ReportPeriod.LAST_7_DAYS,
                overallAdherenceRate = 0.35f,
                goodHabitsPassedDays = 5,
                goodHabitsTotalDays = 20,
                badHabitsCleanDays = 3,
                badHabitsTotalDays = 10,
                taskReports = emptyList()
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
