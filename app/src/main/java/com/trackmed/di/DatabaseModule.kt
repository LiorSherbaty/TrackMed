package com.trackmed.di

import android.content.Context
import androidx.room.Room
import com.trackmed.data.local.TrackMedDatabase
import com.trackmed.data.local.dao.IntakeLogDao
import com.trackmed.data.local.dao.MedicineDao
import com.trackmed.data.local.dao.ScheduleDao
import com.trackmed.data.local.dao.UserSettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TrackMedDatabase {
        return Room.databaseBuilder(
            context,
            TrackMedDatabase::class.java,
            TrackMedDatabase.DATABASE_NAME
        )
            .addMigrations(TrackMedDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideMedicineDao(database: TrackMedDatabase): MedicineDao {
        return database.medicineDao()
    }

    @Provides
    @Singleton
    fun provideScheduleDao(database: TrackMedDatabase): ScheduleDao {
        return database.scheduleDao()
    }

    @Provides
    @Singleton
    fun provideIntakeLogDao(database: TrackMedDatabase): IntakeLogDao {
        return database.intakeLogDao()
    }

    @Provides
    @Singleton
    fun provideUserSettingsDao(database: TrackMedDatabase): UserSettingsDao {
        return database.userSettingsDao()
    }
}
