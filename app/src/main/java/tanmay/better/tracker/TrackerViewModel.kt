package tanmay.better.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

class TrackerViewModel(
    private val dao: TrackerDAO
) : ViewModel() {
    val activities: StateFlow<List<TrackerActivity>> = dao.getAllActivities().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val activityCheckIns: StateFlow<List<ActivityCheckIn>> = dao.getAllCheckIns().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addActivity(name: String, taskType: TaskType, sentiment: Sentiment, target: Int) {
        viewModelScope.launch {
            dao.insertActivity(
                TrackerActivity(
                    name = name,
                    taskType = taskType,
                    sentiment = sentiment,
                    target = target
                )
            )
        }
    }

    fun addActivity(activity: TrackerActivity) {
        viewModelScope.launch {
            dao.insertActivity(activity)
        }
    }

    fun updateActivity(activity: TrackerActivity) {
        viewModelScope.launch {
            dao.updateActivity(activity)
        }
    }

    fun deleteActivity(activity: TrackerActivity) {
        viewModelScope.launch {
            dao.deleteActivity(activity)
        }
    }

    fun onIncrement(activity: TrackerActivity, timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            dao.insertCheckIn(
                ActivityCheckIn(
                    activityId = activity.id,
                    timestamp = timestamp
                )
            )
        }
    }

    fun onDecrement(activity: TrackerActivity, now: Long = System.currentTimeMillis()) {
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val (startOfDay, endOfDay) = DateUtils.getDayStartEnd(date, zone)
        viewModelScope.launch {
            dao.deleteLatestCheckInForDay(activity.id, startOfDay, endOfDay)
        }
    }

    fun onToggleBinary(activity: TrackerActivity, now: Long = System.currentTimeMillis()) {
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val (startOfDay, endOfDay) = DateUtils.getDayStartEnd(date, zone)
        val checkInsToday = activityCheckIns.value.count { checkIn ->
            checkIn.activityId == activity.id && checkIn.timestamp in startOfDay until endOfDay
        }
        if (checkInsToday > 0) {
            onDecrement(activity, now)
        } else {
            onIncrement(activity, now)
        }
    }

    fun onCheckIn(activity: TrackerActivity) {
        onIncrement(activity)
    }
}

class TrackerViewModelFactory(
    private val dao: TrackerDAO
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrackerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrackerViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
