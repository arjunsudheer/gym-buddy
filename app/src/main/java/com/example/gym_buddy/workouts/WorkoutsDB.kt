package com.example.gym_buddy.workouts

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dayOfWeek: String, // "Monday", "Tuesday", etc.
    var exerciseName: String = "",
    var sets: Int = 0,
    var reps: Int = 0,
    var weight: Int = 0,
    var notes: String = "",
    var type: String = "WEIGHT", // "WEIGHT" or "REST"
)

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Query("DELETE FROM workouts WHERE dayOfWeek = :day")
    suspend fun deleteAllWorkoutsForDay(day: String)

    // Order by descending ID to show most recent workouts first
    @Query("SELECT * FROM workouts WHERE dayOfWeek = :day ORDER BY id DESC")
    fun getWorkoutsForDay(day: String): Flow<List<WorkoutEntity>>
}

@Database(entities = [WorkoutEntity::class], version = 6, exportSchema = false)
abstract class WorkoutsDB : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutsDB? = null

        fun getDatabase(context: Context): WorkoutsDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutsDB::class.java,
                    "workout_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
