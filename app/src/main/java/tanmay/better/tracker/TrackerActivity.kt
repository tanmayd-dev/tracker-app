package tanmay.better.tracker

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskType {
    BINARY,
    COUNTER
}

enum class Sentiment {
    GOOD,
    BAD
}

@Entity(tableName = "activities")
data class TrackerActivity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val taskType: TaskType,
    val sentiment: Sentiment,
    val target: Int
)
