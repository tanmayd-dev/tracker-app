package tanmay.better.tracker

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Frequency {
    DAILY,
    WEEKLY,
}

enum class GoalType {
    AT_LEAST,
    AT_MOST,
}

@Entity(tableName = "activities")
data class TrackerActivity (
    @PrimaryKey val id: Int,
    val name: String,
    val target: Int,
    val goalType: GoalType,
    val frequency: Frequency,
)
