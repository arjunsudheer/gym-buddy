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
    var sets: Int = 0,
    var reps: Int = 0,
    var weight: Int = 0,
    var notes: String = "",
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

@Database(entities = [WorkoutEntity::class], version = 5, exportSchema = false)
abstract class WorkoutsDB : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutsDB? = null

        val WORKOUT_DB_MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create the new table
                db.execSQL(
                    "CREATE TABLE workouts_new (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "dayOfWeek TEXT NOT NULL, " +
                            "exerciseName TEXT NOT NULL, " +
                            "sets INTEGER NOT NULL, " +
                            "reps INTEGER NOT NULL, " +
                            "weight INTEGER NOT NULL, " +
                            "notes TEXT NOT NULL)"
                )
                // Copy the data
                db.execSQL(
                    "INSERT INTO workouts_new (id, dayOfWeek, exerciseName, sets, reps, weight, notes) " +
                            "SELECT id, dayOfWeek, exerciseName, " +
                            "CAST(sets AS INTEGER), CAST(reps AS INTEGER), CAST(weight AS INTEGER), notes " +
                            "FROM workouts"
                )
                // Remove the old table
                db.execSQL("DROP TABLE workouts")
                // Rename the new table
                db.execSQL("ALTER TABLE workouts_new RENAME TO workouts")
            }
        }

        val WORKOUT_DB_MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE workouts ADD COLUMN notes TEXT DEFAULT '' NOT NULL")
                db.execSQL("ALTER TABLE workouts ADD COLUMN weightHistory TEXT DEFAULT '' NOT NULL")
            }
        }

        val WORKOUT_DB_MIGRATION_2_3 = object : Migration(2, 3) {
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
                    .addMigrations(
                        WORKOUT_DB_MIGRATION_2_3,
                        WORKOUT_DB_MIGRATION_3_4,
                        WORKOUT_DB_MIGRATION_4_5
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
