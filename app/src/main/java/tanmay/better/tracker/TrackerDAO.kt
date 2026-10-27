package tanmay.better.tracker

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: TrackerActivity): Long

    @Update
    suspend fun updateActivity(activity: TrackerActivity)

    @Delete
    suspend fun deleteActivity(activity: TrackerActivity)

    @Query("SELECT * FROM activities")
    fun getAllActivities(): Flow<List<TrackerActivity>>

    @Insert
    suspend fun insertCheckIn(checkIn: ActivityCheckIn)

    @Query("""
        DELETE FROM checkins 
        WHERE activityId = :activityId 
          AND timestamp >= :startOfDay 
          AND timestamp < :endOfDay 
          AND timestamp = (
              SELECT MAX(timestamp) FROM checkins 
              WHERE activityId = :activityId 
                AND timestamp >= :startOfDay 
                AND timestamp < :endOfDay
          )
    """)
    suspend fun deleteLatestCheckInForDay(activityId: Long, startOfDay: Long, endOfDay: Long)

    @Query("SELECT * FROM checkins WHERE timestamp >= :startTimestamp AND timestamp < :endTimestamp")
    fun getCheckInsBetween(startTimestamp: Long, endTimestamp: Long): Flow<List<ActivityCheckIn>>

    @Query("SELECT * FROM checkins WHERE activityId = :activityId")
    fun getCheckInsForActivity(activityId: Long): Flow<List<ActivityCheckIn>>

    @Query("SELECT * FROM checkins")
    fun getAllCheckIns(): Flow<List<ActivityCheckIn>>

}

@Database(
    entities = [TrackerActivity::class, ActivityCheckIn::class],
    version = 2
)
abstract class TrackerDatabase : RoomDatabase() {
    abstract fun trackerDao(): TrackerDAO
}
