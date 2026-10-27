package tanmay.better.tracker

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class FakeTrackerDAO : TrackerDAO {
    val activitiesList = mutableListOf<TrackerActivity>()
    private val activitiesFlow = MutableStateFlow<List<TrackerActivity>>(emptyList())
    private var nextActivityId = 1L

    val checkInsList = mutableListOf<ActivityCheckIn>()
    private val checkInsFlow = MutableStateFlow<List<ActivityCheckIn>>(emptyList())

    var lastDeletedCheckInParams: Triple<Long, Long, Long>? = null

    override suspend fun insertActivity(activity: TrackerActivity): Long {
        val assignedId = if (activity.id == 0L) nextActivityId++ else activity.id
        val newActivity = activity.copy(id = assignedId)
        activitiesList.removeAll { it.id == assignedId }
        activitiesList.add(newActivity)
        activitiesFlow.value = activitiesList.toList()
        return assignedId
    }

    override suspend fun updateActivity(activity: TrackerActivity) {
        val index = activitiesList.indexOfFirst { it.id == activity.id }
        if (index != -1) {
            activitiesList[index] = activity
        } else {
            activitiesList.add(activity)
        }
        activitiesFlow.value = activitiesList.toList()
    }

    override suspend fun deleteActivity(activity: TrackerActivity) {
        activitiesList.removeAll { it.id == activity.id }
        activitiesFlow.value = activitiesList.toList()
    }

    override fun getAllActivities(): Flow<List<TrackerActivity>> = activitiesFlow

    override suspend fun insertCheckIn(checkIn: ActivityCheckIn) {
        checkInsList.add(checkIn)
        checkInsFlow.value = checkInsList.toList()
    }

    override suspend fun deleteLatestCheckInForDay(activityId: Long, startOfDay: Long, endOfDay: Long) {
        lastDeletedCheckInParams = Triple(activityId, startOfDay, endOfDay)
        val target = checkInsList
            .filter { it.activityId == activityId && it.timestamp in startOfDay until endOfDay }
            .maxByOrNull { it.timestamp }
        if (target != null) {
            checkInsList.remove(target)
            checkInsFlow.value = checkInsList.toList()
        }
    }

    override fun getCheckInsBetween(startTimestamp: Long, endTimestamp: Long): Flow<List<ActivityCheckIn>> {
        return checkInsFlow.map { list ->
            list.filter { it.timestamp in startTimestamp until endTimestamp }
        }
    }

    override fun getCheckInsForActivity(activityId: Long): Flow<List<ActivityCheckIn>> {
        return checkInsFlow.map { list ->
            list.filter { it.activityId == activityId }
        }
    }

    override fun getAllCheckIns(): Flow<List<ActivityCheckIn>> = checkInsFlow
}

@OptIn(ExperimentalCoroutinesApi::class)
class TrackerViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeDao: FakeTrackerDAO
    private lateinit var viewModel: TrackerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeTrackerDAO()
        viewModel = TrackerViewModel(fakeDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addActivity_insertsActivityIntoDao() = runTest {
        viewModel.addActivity(
            name = "Read Book",
            taskType = TaskType.BINARY,
            sentiment = Sentiment.GOOD,
            target = 1
        )

        assertEquals(1, fakeDao.activitiesList.size)
        val inserted = fakeDao.activitiesList.first()
        assertEquals("Read Book", inserted.name)
        assertEquals(TaskType.BINARY, inserted.taskType)
        assertEquals(Sentiment.GOOD, inserted.sentiment)
        assertEquals(1, inserted.target)
    }

    @Test
    fun updateActivity_updatesExistingActivityInDao() = runTest {
        val initialActivity = TrackerActivity(
            id = 1L,
            name = "Exercise",
            taskType = TaskType.COUNTER,
            sentiment = Sentiment.GOOD,
            target = 3
        )
        fakeDao.insertActivity(initialActivity)

        val updated = initialActivity.copy(name = "Gym Workout", target = 5)
        viewModel.updateActivity(updated)

        assertEquals(1, fakeDao.activitiesList.size)
        val current = fakeDao.activitiesList.first()
        assertEquals("Gym Workout", current.name)
        assertEquals(5, current.target)
    }

    @Test
    fun deleteActivity_removesActivityFromDao() = runTest {
        val activity = TrackerActivity(
            id = 1L,
            name = "Quit Smoking",
            taskType = TaskType.BINARY,
            sentiment = Sentiment.BAD,
            target = 0
        )
        fakeDao.insertActivity(activity)
        assertEquals(1, fakeDao.activitiesList.size)

        viewModel.deleteActivity(activity)

        assertTrue(fakeDao.activitiesList.isEmpty())
    }

    @Test
    fun onIncrement_insertsCheckInWithTimestamp() = runTest {
        val activity = TrackerActivity(
            id = 42L,
            name = "Drink Water",
            taskType = TaskType.COUNTER,
            sentiment = Sentiment.GOOD,
            target = 8
        )
        val testTimestamp = 1700000000000L

        viewModel.onIncrement(activity, testTimestamp)

        assertEquals(1, fakeDao.checkInsList.size)
        val checkIn = fakeDao.checkInsList.first()
        assertEquals(42L, checkIn.activityId)
        assertEquals(testTimestamp, checkIn.timestamp)
    }

    @Test
    fun onCheckIn_delegatesToOnIncrement() = runTest {
        val activity = TrackerActivity(
            id = 42L,
            name = "Drink Water",
            taskType = TaskType.COUNTER,
            sentiment = Sentiment.GOOD,
            target = 8
        )

        viewModel.onCheckIn(activity)

        assertEquals(1, fakeDao.checkInsList.size)
        assertEquals(42L, fakeDao.checkInsList.first().activityId)
    }

    @Test
    fun onDecrement_callsDeleteLatestCheckInForDayWithExpectedBounds() = runTest {
        val activity = TrackerActivity(
            id = 42L,
            name = "Drink Water",
            taskType = TaskType.COUNTER,
            sentiment = Sentiment.GOOD,
            target = 8
        )
        val now = 1700000000000L
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val (expectedStart, expectedEnd) = DateUtils.getDayStartEnd(date, zone)

        fakeDao.insertCheckIn(ActivityCheckIn(activityId = 42L, timestamp = now))

        viewModel.onDecrement(activity, now)

        assertEquals(Triple(42L, expectedStart, expectedEnd), fakeDao.lastDeletedCheckInParams)
        assertTrue(fakeDao.checkInsList.isEmpty())
    }

    @Test
    fun onToggleBinary_whenZeroCheckInsToday_increments() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.activityCheckIns.collect()
        }
        val activity = TrackerActivity(
            id = 10L,
            name = "Read",
            taskType = TaskType.BINARY,
            sentiment = Sentiment.GOOD,
            target = 1
        )
        val now = 1700000000000L

        viewModel.onToggleBinary(activity, now)

        assertEquals(1, fakeDao.checkInsList.size)
        assertEquals(10L, fakeDao.checkInsList.first().activityId)
        assertEquals(now, fakeDao.checkInsList.first().timestamp)
    }

    @Test
    fun onToggleBinary_whenCheckInsExistToday_decrements() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.activityCheckIns.collect()
        }
        val activity = TrackerActivity(
            id = 10L,
            name = "Read",
            taskType = TaskType.BINARY,
            sentiment = Sentiment.GOOD,
            target = 1
        )
        val now = 1700000000000L
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val (startOfDay, endOfDay) = DateUtils.getDayStartEnd(date, zone)

        fakeDao.insertCheckIn(ActivityCheckIn(activityId = 10L, timestamp = startOfDay + 1000L))

        viewModel.onToggleBinary(activity, now)

        assertEquals(Triple(10L, startOfDay, endOfDay), fakeDao.lastDeletedCheckInParams)
        assertTrue(fakeDao.checkInsList.isEmpty())
    }

    @Test
    fun onToggleBinary_whenCheckInsOnlyFromYesterday_increments() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.activityCheckIns.collect()
        }
        val activity = TrackerActivity(
            id = 10L,
            name = "Read",
            taskType = TaskType.BINARY,
            sentiment = Sentiment.GOOD,
            target = 1
        )
        val now = 1700000000000L
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val (startOfDay, _) = DateUtils.getDayStartEnd(date, zone)

        val yesterdayTimestamp = startOfDay - 1000L
        fakeDao.insertCheckIn(ActivityCheckIn(activityId = 10L, timestamp = yesterdayTimestamp))

        viewModel.onToggleBinary(activity, now)

        assertEquals(2, fakeDao.checkInsList.size)
        assertTrue(fakeDao.checkInsList.any { it.timestamp == now })
    }
}
