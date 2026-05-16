package tanmay.better.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrackerViewModel(
    private val dao: TrackerDAO
): ViewModel() {
    val activities = dao.getAllActivities().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val activityCheckIns = dao.getAllCheckIns().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    fun addActivity(activity: TrackerActivity) {
        viewModelScope.launch {
            dao.insertActivity(activity)
        }
    }
    fun deleteActivity(activity: TrackerActivity) {
        viewModelScope.launch {
            dao.deleteActivity(activity)
        }
    }
    fun onCheckIn(activity: TrackerActivity) {
        val activityCheckIn = ActivityCheckIn(
            activity.id,
            System.currentTimeMillis()
        )
        viewModelScope.launch {
            dao.insertCheckIn(activityCheckIn)
        }
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
