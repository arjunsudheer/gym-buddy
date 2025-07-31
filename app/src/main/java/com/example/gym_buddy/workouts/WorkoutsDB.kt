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
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dayOfWeek: String, // "Monday", "Tuesday", etc.
    var exerciseName: String = "",
    var sets: String = "",
    var reps: String = "",
    var weight: String = "",
)

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    // Order by descending ID to show most recent workouts first
    @Query("SELECT * FROM workouts WHERE dayOfWeek = :day ORDER BY id DESC")
    fun getWorkoutsForDay(day: String): Flow<List<WorkoutEntity>>
}

@Database(entities = [WorkoutEntity::class], version = 3, exportSchema = false)
abstract class WorkoutsDB : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutsDB? = null

        val WORKOUT_DB_MIGRATION = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Provide a migration strategy if database columns need to be changed
                db.execSQL("ALTER TABLE workouts DROP COLUMN leftWeight")
                db.execSQL("ALTER TABLE workouts DROP COLUMN rightWeight")
                db.execSQL("ALTER TABLE workouts ADD COLUMN weight TEXT DEFAULT '' NOT NULL")
            }
        }

        fun getDatabase(context: Context): WorkoutsDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutsDB::class.java,
                    "workout_database"
                )
                    .addMigrations(WORKOUT_DB_MIGRATION)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}