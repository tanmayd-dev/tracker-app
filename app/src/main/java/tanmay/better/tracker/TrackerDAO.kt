package tanmay.better.tracker

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Query
import androidx.room.OnConflictStrategy
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: TrackerActivity)

    @Delete
    suspend fun deleteActivity(activity: TrackerActivity)

    @Query("SELECT * FROM activities")
    fun getAllActivities(): Flow<List<TrackerActivity>>

    @Insert
    suspend fun insertCheckIn(checkIn: ActivityCheckIn)

    @Query("SELECT * FROM checkins WHERE activityId = :activityId")
    fun getCheckInsForActivity(activityId: Int): Flow<List<ActivityCheckIn>>

    @Query("SELECT * FROM checkins")
    fun getAllCheckIns(): Flow<List<ActivityCheckIn>>

}

@Database(
    entities = [TrackerActivity::class, ActivityCheckIn::class],
    version = 1
)
abstract class TrackerDatabase : RoomDatabase() {
    abstract fun trackerDao(): TrackerDAO
}
