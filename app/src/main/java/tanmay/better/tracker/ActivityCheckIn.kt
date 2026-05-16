package tanmay.better.tracker

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE

@Entity(tableName = "checkins", primaryKeys = ["activityId", "timestamp"], foreignKeys = [
    (ForeignKey(
        entity = TrackerActivity::class,
        parentColumns = ["id"],
        childColumns = ["activityId"],
        onDelete = CASCADE,
    ))
])
data class ActivityCheckIn(
    val activityId: Int,
    val timestamp: Long,
)
