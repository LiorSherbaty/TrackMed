package com.trackmed.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.trackmed.data.local.dao.IntakeLogDao
import com.trackmed.data.local.dao.MedicineDao
import com.trackmed.data.local.dao.ScheduleDao
import com.trackmed.data.local.dao.UserSettingsDao
import com.trackmed.data.local.entity.IntakeLog
import com.trackmed.data.local.entity.Medicine
import com.trackmed.data.local.entity.Schedule
import com.trackmed.data.local.entity.UserSettings

@Database(
    entities = [
        Medicine::class,
        Schedule::class,
        IntakeLog::class,
        UserSettings::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class TrackMedDatabase : RoomDatabase() {

    abstract fun medicineDao(): MedicineDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun intakeLogDao(): IntakeLogDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        const val DATABASE_NAME = "trackmed.db"

        @Volatile
        private var INSTANCE: TrackMedDatabase? = null

        /**
         * Migration from version 1 to 2: Add themeMode column to user_settings
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE user_settings ADD COLUMN themeMode TEXT NOT NULL DEFAULT 'SYSTEM'"
                )
            }
        }

        fun getInstance(context: Context): TrackMedDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrackMedDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
