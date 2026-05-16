package tanmay.better.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tanmay.better.tracker.ui.theme.TrackerTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

private fun todayStartEndMillis(): Pair<Long, Long> {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val startOfDay = today.atStartOfDay(zone).toInstant().toEpochMilli()
    val startOfNext = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    return startOfDay to startOfNext
}

private fun currentWeekStartEndMillis(): Pair<Long, Long> {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .atStartOfDay(zone)
        .toInstant().toEpochMilli()
    val startOfNext = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
        .atStartOfDay(zone).toInstant()
        .toEpochMilli()
    return startOfWeek to startOfNext
}

class MainActivity : ComponentActivity() {

    private lateinit var trackerDatabase: TrackerDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        trackerDatabase = Room.databaseBuilder(
            applicationContext,
            TrackerDatabase::class.java,
            "tracker_database"
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = trackerDatabase.trackerDao()
                    dao.insertActivity(
                        TrackerActivity(1, "Read a book", 1, GoalType.AT_LEAST, Frequency.DAILY)
                    )
                    dao.insertActivity(
                        TrackerActivity(2, "Go to the gym", 3, GoalType.AT_LEAST, Frequency.WEEKLY)
                    )
                    dao.insertActivity(
                        TrackerActivity(3, "Eat junk food", 2, GoalType.AT_MOST, Frequency.WEEKLY)
                    )
                }
            }
        }).build()


        val dao = trackerDatabase.trackerDao()

        val trackerViewModel: TrackerViewModel by viewModels {
            TrackerViewModelFactory(dao)
        }

        enableEdgeToEdge()
        setContent {
            TrackerTheme {
                val activities by trackerViewModel.activities.collectAsState()
                val activityCheckins by trackerViewModel.activityCheckIns.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    ActivityList(
                        activities = activities,
                        activityCheckins = activityCheckins,
                        modifier = Modifier.padding(paddingValues = innerPadding),
                        onCheckIn = { activity -> trackerViewModel.onCheckIn(activity) })
                }
            }
        }
    }
}

@Composable
fun ActivityList(
    activities: List<TrackerActivity>,
    activityCheckins: List<ActivityCheckIn>,
    modifier: Modifier,
    onCheckIn: (TrackerActivity) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(items = activities, key = { it.id }) { activity ->
            ActivityRow(
                activity = activity,
                activityCheckins = activityCheckins.filter { (activityId, timestamp) ->
                    val idMatches = activityId == activity.id
                    when (activity.frequency) {
                        Frequency.DAILY -> {
                            val (start, end) = todayStartEndMillis()
                            idMatches && timestamp in start until end
                        }

                        Frequency.WEEKLY -> {
                            val (start, end) = currentWeekStartEndMillis()
                            idMatches && timestamp in start until end
                        }
                    }
                },
                onCheckIn = { onCheckIn(activity) })
        }
    }
}

@Composable
fun ActivityRow(
    activity: TrackerActivity,
    activityCheckins: List<ActivityCheckIn>,
    onCheckIn: (TrackerActivity) -> Unit
) {
    val progress = activityCheckins.size
    val limit = activity.target
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
                .combinedClickable(
                    onClick = {},
                    onLongClick = {  }
                )
        ) {
            Text(
                activity.name,
                fontSize = 10.em,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${activity.frequency}",
                fontSize = 6.em,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        ProgressIndicator(
            progress = progress,
            limit = limit,
            goalType = activity.goalType,
            onCheckIn = onCheckIn,
            activity = activity
        )
    }
}

@Composable
fun ProgressIndicator(
    progress: Int,
    limit: Int,
    goalType: GoalType,
    onCheckIn: (TrackerActivity) -> Unit,
    activity: TrackerActivity
) {
    val progressRatio = (progress.toFloat() / limit.toFloat())
        .coerceIn(0f, 1f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(128.dp)
            .clickable(onClick = { onCheckIn(activity) })
    ) {
        CircularProgressIndicator(
            progress = { progressRatio },
            modifier = Modifier.fillMaxSize(),
            color = when (goalType) {
                GoalType.AT_LEAST -> MaterialTheme.colorScheme.primary
                GoalType.AT_MOST -> MaterialTheme.colorScheme.error
            },
            strokeWidth = 6.dp
        )
        Text(
            "$progress/$limit",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 32.sp,
        )
    }
}
