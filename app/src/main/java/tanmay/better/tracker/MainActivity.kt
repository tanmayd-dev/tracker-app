package tanmay.better.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tanmay.better.tracker.ui.components.TaskEditSheet
import tanmay.better.tracker.ui.screens.ReportsScreen
import tanmay.better.tracker.ui.screens.TrackerScreen
import tanmay.better.tracker.ui.theme.TrackerTheme

enum class NavTab {
    TRACKER,
    REPORTS
}

class MainActivity : ComponentActivity() {

    private lateinit var trackerDatabase: TrackerDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        trackerDatabase = Room.databaseBuilder(
            applicationContext,
            TrackerDatabase::class.java,
            "tracker_database"
        ).fallbackToDestructiveMigration()
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = trackerDatabase.trackerDao()
                    dao.insertActivity(
                        TrackerActivity(1L, "Daily Reading", TaskType.BINARY, Sentiment.GOOD, 1)
                    )
                    dao.insertActivity(
                        TrackerActivity(2L, "Water Intake (Glasses)", TaskType.COUNTER, Sentiment.GOOD, 8)
                    )
                    dao.insertActivity(
                        TrackerActivity(3L, "No Fast Food", TaskType.BINARY, Sentiment.BAD, 0)
                    )
                    dao.insertActivity(
                        TrackerActivity(4L, "Coffee Intake", TaskType.COUNTER, Sentiment.BAD, 2)
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
                MainApp(viewModel = trackerViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(NavTab.TRACKER) }
    var editingActivity by remember { mutableStateOf<TrackerActivity?>(null) }
    var isCreatingTask by remember { mutableStateOf(false) }

    val activities by viewModel.activities.collectAsState()
    val activityCheckIns by viewModel.activityCheckIns.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            NavTab.TRACKER -> "Habit Tracker"
                            NavTab.REPORTS -> "Reports & Analytics"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == NavTab.TRACKER,
                    onClick = { selectedTab = NavTab.TRACKER },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Tracker"
                        )
                    },
                    label = { Text("Tracker") }
                )
                NavigationBarItem(
                    selected = selectedTab == NavTab.REPORTS,
                    onClick = { selectedTab = NavTab.REPORTS },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Reports"
                        )
                    },
                    label = { Text("Reports") }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == NavTab.TRACKER) {
                FloatingActionButton(
                    onClick = {
                        editingActivity = null
                        isCreatingTask = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Task"
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            NavTab.TRACKER -> {
                TrackerScreen(
                    activities = activities,
                    activityCheckIns = activityCheckIns,
                    onIncrement = { activity -> viewModel.onIncrement(activity) },
                    onDecrement = { activity -> viewModel.onDecrement(activity) },
                    onToggleBinary = { activity -> viewModel.onToggleBinary(activity) },
                    onEditTask = { activity ->
                        isCreatingTask = false
                        editingActivity = activity
                    },
                    onDeleteTask = { activity -> viewModel.deleteActivity(activity) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            NavTab.REPORTS -> {
                ReportsScreen(
                    activities = activities,
                    checkIns = activityCheckIns,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    if (isCreatingTask || editingActivity != null) {
        TaskEditSheet(
            activityToEdit = editingActivity,
            onDismiss = {
                editingActivity = null
                isCreatingTask = false
            },
            onSave = { name, type, sentiment, target ->
                val current = editingActivity
                if (current != null) {
                    viewModel.updateActivity(
                        current.copy(
                            name = name,
                            taskType = type,
                            sentiment = sentiment,
                            target = target
                        )
                    )
                } else {
                    viewModel.addActivity(name, type, sentiment, target)
                }
                editingActivity = null
                isCreatingTask = false
            },
            onDelete = if (editingActivity != null) {
                { activity ->
                    viewModel.deleteActivity(activity)
                    editingActivity = null
                    isCreatingTask = false
                }
            } else null
        )
    }
}
