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

    @Query("SELECT * FROM workouts")
    suspend fun getAllWorkouts(): List<WorkoutEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateWorkouts(workouts: List<WorkoutEntity>)

    // Order by descending ID to show most recent workouts first
    @Query("SELECT * FROM workouts WHERE dayOfWeek = :day ORDER BY id DESC")
    fun getWorkoutsForDay(day: String): Flow<List<WorkoutEntity>>
}

@Database(entities = [WorkoutEntity::class], version = 7, exportSchema = false)
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
                    .addCallback(object : Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Add check constraints manually as Room doesn't support them via @Entity
                            // Max weight is 2000 lbs (stored in DB as lbs based on current implementation logic, 
                            // or rather the app layer handles conversion, but let's enforce a broad upper bound).
                            // Based on requirements: sets/reps max 100, weight max 2000.
                            db.execSQL("""
                                CREATE TRIGGER IF NOT EXISTS validate_workout_bounds
                                BEFORE INSERT ON workouts
                                FOR EACH ROW
                                BEGIN
                                    SELECT CASE
                                        WHEN NEW.sets > 100 THEN RAISE(ABORT, 'Sets cannot exceed 100')
                                        WHEN NEW.reps > 100 THEN RAISE(ABORT, 'Reps cannot exceed 100')
                                        WHEN NEW.weight > 2000 THEN RAISE(ABORT, 'Weight cannot exceed 2000')
                                    END;
                                END;
                            """.trimIndent())
                        }
                    })
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
