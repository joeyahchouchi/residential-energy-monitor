package com.univ.energymonitor.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Main Room database for the Energy Monitor app.
 *
 * Contains two tables:
 *   - users    (login accounts)
 *   - surveys  (completed household surveys with SurveyData + EnergyReport as JSON)
 *
 * Version is bumped whenever the schema changes (entities or columns).
 */
@Database(
    entities = [UserEntity::class, SurveyEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun surveyDao(): SurveyDao

    companion object {
        // Volatile: ensures writes to INSTANCE are visible to all threads immediately
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton database instance, creating it on first access.
         * Thread-safe via double-checked locking (synchronized block).
         */
        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "energy_monitor.db"
                )
                    // For Phase 1: destroy + recreate on schema change (dev-only).
                    // Before release, replace with proper migrations.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}