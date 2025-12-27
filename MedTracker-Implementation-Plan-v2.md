# TrackMed - Android Implementation Plan v2.0

> **Version:** 2.0 (December 2025)
> **Status:** Ready for Implementation
> **License:** Apache 2.0 (Open Source)
> **Target:** Google Play Store

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Tech Stack](#tech-stack)
3. [Features Breakdown](#features-breakdown)
4. [Data Models](#data-models)
5. [Database Schema](#database-schema)
6. [Project Structure](#project-structure)
7. [Screen Specifications](#screen-specifications)
8. [Notification System](#notification-system)
9. [Widget Implementation](#widget-implementation)
10. [Backup & Restore](#backup--restore)
11. [Implementation Phases](#implementation-phases)
12. [Dependencies](#dependencies)
13. [Manifest & Permissions](#manifest--permissions)
14. [Key Algorithms](#key-algorithms)
15. [Testing Strategy](#testing-strategy)
16. [Open Source & Google Play](#open-source--google-play)
17. [Future Enhancements](#future-enhancements)

---

## Project Overview

### Vision

A clean, simple, native Android application for tracking medicine and vitamin intake. Built with modern Android development practices, designed for personal use but polished enough for public distribution on Google Play.

### Problem Statement

Users need an easy way to:
- Add/remove medicines with flexible scheduling (morning, lunch, evening, custom times, weekly)
- Track intake status (taken, skipped, missed)
- Monitor inventory and receive restock reminders
- Get notifications with snooze/follow-up capability
- Quick-access widget for daily tracking

### Target User

- Primary: Single user, personal use
- Storage: Local-only with JSON export/import for backup and device migration
- Distribution: Google Play Store (free, open source)

### Design Philosophy

- **Simplicity first** - No account required, no cloud sync, no complexity
- **Privacy by design** - All data stays on device
- **Battery conscious** - Efficient background work
- **Accessibility** - Material 3 with proper contrast and touch targets

---

## Tech Stack

| Component | Technology | Version | Rationale |
|-----------|------------|---------|-----------|
| Language | Kotlin | 2.2.20 | Native Android, best tooling |
| Min SDK | 26 (Android 8.0) | - | WorkManager, notification channels |
| Target SDK | 35 | - | Latest Android requirements |
| Compile SDK | 35 | - | Latest features |
| UI Framework | Jetpack Compose | BOM 2025.12.00 | Modern, declarative UI |
| Architecture | MVVM + Clean Architecture | - | Testable, maintainable |
| Database | Room | 2.8.4 | SQLite abstraction, Flow support |
| Annotation Processing | KSP | 2.2.20-2.0.4 | 2x faster than kapt |
| Dependency Injection | Hilt | 2.57.1 | Official Android DI |
| Async | Kotlin Coroutines + Flow | 1.9.0 | Reactive data streams |
| Navigation | Navigation 3 | 1.0.0 | Compose-first navigation |
| Notifications | AlarmManager + BroadcastReceiver | - | Exact timing for reminders |
| Background Work | WorkManager | 2.11.0 | Reliable background tasks |
| Widget | Glance | 1.1.1 | Compose-based widgets |
| Serialization | Kotlinx Serialization | 1.9.0 | JSON export/import |
| Date/Time | java.time | - | Modern date handling |

---

## Features Breakdown

### Core Features (MVP)

| # | Feature | Description |
|---|---------|-------------|
| 1 | **Medicine Management** | CRUD operations for medicines with name, notes, stock info |
| 2 | **Flexible Scheduling** | Daily/weekly, multiple time windows per medicine |
| 3 | **Intake Tracking** | Mark as taken/skipped, auto-mark missed after follow-ups |
| 4 | **Inventory Tracking** | Current stock, auto-decrement on intake, low stock alerts |
| 5 | **Notifications** | Scheduled reminders with Take/Skip/Snooze actions |
| 6 | **Home Screen Widget** | Interactive daily checklist |

### Additional Features

| # | Feature | Description |
|---|---------|-------------|
| 7 | **Vacation/Pause Mode** | Per-medicine or global pause capability |
| 8 | **Notes** | Per-medicine notes (e.g., "take with food", "empty stomach") |
| 9 | **Data Backup** | JSON export/import via system file picker |
| 10 | **Settings** | Configurable time windows, notification behavior |
| 11 | **Dark Mode** | System-aware theme via Material 3 |
| 12 | **Onboarding** | First-launch welcome and permission setup |

---

## Data Models

### Design Decisions

1. **Removed `withFood` boolean** - The `notes` field in Medicine handles all intake instructions (with food, empty stomach, with water, etc.)
2. **Using ISO date strings** - `scheduledDate` stored as "YYYY-MM-DD" for easy querying and readability
3. **Enum prefixes** - Following `E` prefix convention from CLAUDE.md

### 1. Medicine Entity

```kotlin
@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Basic Info
    val name: String,
    val notes: String? = null,  // Intake instructions: "Take with food", "Empty stomach", etc.

    // Inventory
    val currentStock: Int,
    val restockThreshold: Int,
    val pillsPerDose: Int = 1,

    // State
    val isActive: Boolean = true,
    val isPaused: Boolean = false,

    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

### 2. Schedule Entity

```kotlin
@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicineId")]
)
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val medicineId: Long,

    // Frequency
    val frequencyType: EFrequencyType,
    val dayOfWeek: Int? = null,  // 1=Monday, 7=Sunday (for WEEKLY only)

    // Timing
    val timeWindow: ETimeWindow,
    val customTimeHour: Int? = null,   // 0-23 (for CUSTOM only)
    val customTimeMinute: Int? = null, // 0-59 (for CUSTOM only)

    // Dosage
    val dosageAmount: Int = 1,

    // State
    val isActive: Boolean = true,

    val createdAt: Long = System.currentTimeMillis()
)

enum class EFrequencyType {
    DAILY,
    WEEKLY
}

enum class ETimeWindow {
    MORNING,
    LUNCH,
    EVENING,
    CUSTOM
}
```

### 3. IntakeLog Entity

```kotlin
@Entity(
    tableName = "intake_logs",
    foreignKeys = [
        ForeignKey(
            entity = Schedule::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("scheduleId"),
        Index("medicineId"),
        Index("scheduledDate"),
        Index(value = ["scheduleId", "scheduledDate"], unique = true)
    ]
)
data class IntakeLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val scheduleId: Long,
    val medicineId: Long,

    // Scheduled
    val scheduledDate: String,         // ISO format: "2025-12-25"
    val scheduledTimeHour: Int,        // 0-23
    val scheduledTimeMinute: Int,      // 0-59
    val timeWindow: ETimeWindow,       // For grouping in UI

    // Dosage (copied from schedule for historical accuracy)
    val dosageAmount: Int,

    // Status
    val status: EIntakeStatus = EIntakeStatus.PENDING,

    // Actual intake
    val actualTimestamp: Long? = null,

    // Follow-up tracking
    val followUpCount: Int = 0,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class EIntakeStatus {
    PENDING,
    TAKEN,
    SKIPPED,
    MISSED
}
```

### 4. UserSettings Entity

```kotlin
@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 1,  // Singleton pattern

    // Notification Settings
    val followUpIntervalMinutes: Int = 30,
    val maxFollowUps: Int = 3,
    val notificationsEnabled: Boolean = true,

    // Time Windows (stored as "HH:mm")
    val morningStart: String = "07:00",
    val morningEnd: String = "09:00",
    val lunchStart: String = "12:00",
    val lunchEnd: String = "14:00",
    val eveningStart: String = "19:00",
    val eveningEnd: String = "21:00",

    // App Settings
    val use24HourFormat: Boolean = true,
    val vacationModeActive: Boolean = false,

    // First Launch
    val onboardingCompleted: Boolean = false,

    val updatedAt: Long = System.currentTimeMillis()
)
```

### 5. Backup Data Models

```kotlin
@Serializable
data class BackupData(
    val version: Int = 1,
    val appVersion: String,
    val exportedAt: String,  // ISO timestamp
    val medicines: List<MedicineBackup>,
    val schedules: List<ScheduleBackup>,
    val intakeLogs: List<IntakeLogBackup>,
    val settings: UserSettingsBackup
)

@Serializable
data class MedicineBackup(
    val id: Long,
    val name: String,
    val notes: String?,
    val currentStock: Int,
    val restockThreshold: Int,
    val pillsPerDose: Int,
    val isActive: Boolean,
    val isPaused: Boolean,
    val createdAt: Long
)

@Serializable
data class ScheduleBackup(
    val id: Long,
    val medicineId: Long,
    val frequencyType: String,
    val dayOfWeek: Int?,
    val timeWindow: String,
    val customTimeHour: Int?,
    val customTimeMinute: Int?,
    val dosageAmount: Int,
    val isActive: Boolean,
    val createdAt: Long
)

@Serializable
data class IntakeLogBackup(
    val id: Long,
    val scheduleId: Long,
    val medicineId: Long,
    val scheduledDate: String,
    val scheduledTimeHour: Int,
    val scheduledTimeMinute: Int,
    val timeWindow: String,
    val dosageAmount: Int,
    val status: String,
    val actualTimestamp: Long?,
    val followUpCount: Int,
    val createdAt: Long
)

@Serializable
data class UserSettingsBackup(
    val followUpIntervalMinutes: Int = 30,
    val maxFollowUps: Int = 3,
    val notificationsEnabled: Boolean = true,
    val morningStart: String = "07:00",
    val morningEnd: String = "09:00",
    val lunchStart: String = "12:00",
    val lunchEnd: String = "14:00",
    val eveningStart: String = "19:00",
    val eveningEnd: String = "21:00",
    val use24HourFormat: Boolean = true,
    val vacationModeActive: Boolean = false
)
```

### 6. UI State Models (Domain Layer)

```kotlin
// Combined view for daily display
data class DailyIntakeItem(
    val logId: Long,
    val medicineId: Long,
    val scheduleId: Long,
    val medicineName: String,
    val notes: String?,
    val dosageAmount: Int,
    val scheduledTime: LocalTime,
    val timeWindow: ETimeWindow,
    val status: EIntakeStatus,
    val currentStock: Int,
    val restockThreshold: Int
) {
    val isLowStock: Boolean
        get() = currentStock <= restockThreshold
}

// Medicine with all its schedules
data class MedicineWithSchedules(
    @Embedded val medicine: Medicine,
    @Relation(
        parentColumn = "id",
        entityColumn = "medicineId"
    )
    val schedules: List<Schedule>
)

// Time window configuration for display
data class TimeWindowConfig(
    val window: ETimeWindow,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val displayName: String,
    val icon: ImageVector
)
```

---

## Database Schema

### Room Database

```kotlin
@Database(
    entities = [
        Medicine::class,
        Schedule::class,
        IntakeLog::class,
        UserSettings::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class TrackMedDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun intakeLogDao(): IntakeLogDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        const val DATABASE_NAME = "medtracker.db"
    }
}
```

### Type Converters

```kotlin
class Converters {
    @TypeConverter
    fun fromFrequencyType(value: EFrequencyType): String = value.name

    @TypeConverter
    fun toFrequencyType(value: String): EFrequencyType = EFrequencyType.valueOf(value)

    @TypeConverter
    fun fromTimeWindow(value: ETimeWindow): String = value.name

    @TypeConverter
    fun toTimeWindow(value: String): ETimeWindow = ETimeWindow.valueOf(value)

    @TypeConverter
    fun fromIntakeStatus(value: EIntakeStatus): String = value.name

    @TypeConverter
    fun toIntakeStatus(value: String): EIntakeStatus = EIntakeStatus.valueOf(value)
}
```

---

## DAO Interfaces

### MedicineDao

```kotlin
@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines ORDER BY name ASC")
    fun getAllMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines ORDER BY name ASC")
    suspend fun getAllMedicinesOnce(): List<Medicine>

    @Query("SELECT * FROM medicines WHERE isActive = 1 AND isPaused = 0 ORDER BY name ASC")
    fun getActiveMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: Long): Medicine?

    @Query("SELECT * FROM medicines WHERE id = :id")
    fun getMedicineByIdFlow(id: Long): Flow<Medicine?>

    @Query("SELECT * FROM medicines WHERE currentStock <= restockThreshold AND isActive = 1")
    fun getLowStockMedicines(): Flow<List<Medicine>>

    @Transaction
    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineWithSchedules(id: Long): MedicineWithSchedules?

    @Transaction
    @Query("SELECT * FROM medicines ORDER BY name ASC")
    fun getAllMedicinesWithSchedules(): Flow<List<MedicineWithSchedules>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicine: Medicine): Long

    @Update
    suspend fun update(medicine: Medicine)

    @Query("UPDATE medicines SET currentStock = currentStock - :amount, updatedAt = :timestamp WHERE id = :medicineId AND currentStock >= :amount")
    suspend fun decrementStock(medicineId: Long, amount: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE medicines SET currentStock = :newStock, updatedAt = :timestamp WHERE id = :medicineId")
    suspend fun updateStock(medicineId: Long, newStock: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE medicines SET isPaused = :isPaused, updatedAt = :timestamp WHERE id = :medicineId")
    suspend fun setPausedState(medicineId: Long, isPaused: Boolean, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun delete(medicine: Medicine)

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun deleteById(id: Long)
}
```

### ScheduleDao

```kotlin
@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules WHERE medicineId = :medicineId AND isActive = 1")
    fun getSchedulesForMedicine(medicineId: Long): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE medicineId = :medicineId AND isActive = 1")
    suspend fun getSchedulesForMedicineOnce(medicineId: Long): List<Schedule>

    @Query("SELECT * FROM schedules WHERE isActive = 1")
    fun getAllActiveSchedules(): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules ORDER BY id ASC")
    suspend fun getAllSchedulesOnce(): List<Schedule>

    @Query("""
        SELECT s.* FROM schedules s
        INNER JOIN medicines m ON s.medicineId = m.id
        WHERE s.isActive = 1
        AND m.isActive = 1
        AND m.isPaused = 0
        AND (s.frequencyType = 'DAILY' OR s.dayOfWeek = :dayOfWeek)
        ORDER BY
            CASE s.timeWindow
                WHEN 'MORNING' THEN 1
                WHEN 'LUNCH' THEN 2
                WHEN 'EVENING' THEN 3
                WHEN 'CUSTOM' THEN 4
            END,
            s.customTimeHour,
            s.customTimeMinute
    """)
    fun getSchedulesForDay(dayOfWeek: Int): Flow<List<Schedule>>

    @Query("""
        SELECT s.* FROM schedules s
        INNER JOIN medicines m ON s.medicineId = m.id
        WHERE s.isActive = 1
        AND m.isActive = 1
        AND m.isPaused = 0
        AND (s.frequencyType = 'DAILY' OR s.dayOfWeek = :dayOfWeek)
    """)
    suspend fun getActiveSchedulesForDayOnce(dayOfWeek: Int): List<Schedule>

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): Schedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: Schedule): Long

    @Update
    suspend fun update(schedule: Schedule)

    @Delete
    suspend fun delete(schedule: Schedule)

    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM schedules WHERE medicineId = :medicineId")
    suspend fun deleteByMedicineId(medicineId: Long)
}
```

### IntakeLogDao

```kotlin
@Dao
interface IntakeLogDao {
    @Query("""
        SELECT * FROM intake_logs
        WHERE scheduledDate = :date
        ORDER BY scheduledTimeHour, scheduledTimeMinute
    """)
    fun getLogsForDate(date: String): Flow<List<IntakeLog>>

    @Query("""
        SELECT * FROM intake_logs
        WHERE scheduledDate = :date
        ORDER BY scheduledTimeHour, scheduledTimeMinute
    """)
    suspend fun getLogsForDateOnce(date: String): List<IntakeLog>

    @Query("SELECT * FROM intake_logs WHERE scheduleId = :scheduleId AND scheduledDate = :date")
    suspend fun getLogForScheduleAndDate(scheduleId: Long, date: String): IntakeLog?

    @Query("SELECT * FROM intake_logs WHERE id = :id")
    suspend fun getLogById(id: Long): IntakeLog?

    @Query("""
        SELECT * FROM intake_logs
        WHERE status = 'PENDING'
        AND scheduledDate = :date
    """)
    fun getPendingLogsForDate(date: String): Flow<List<IntakeLog>>

    @Query("""
        SELECT * FROM intake_logs
        WHERE status = 'PENDING'
        AND scheduledDate = :date
    """)
    suspend fun getPendingLogsForDateOnce(date: String): List<IntakeLog>

    @Query("""
        SELECT * FROM intake_logs
        WHERE medicineId = :medicineId
        AND scheduledDate BETWEEN :startDate AND :endDate
        ORDER BY scheduledDate DESC, scheduledTimeHour DESC
    """)
    fun getLogsForMedicineInRange(medicineId: Long, startDate: String, endDate: String): Flow<List<IntakeLog>>

    @Query("SELECT * FROM intake_logs ORDER BY id ASC")
    suspend fun getAllLogsOnce(): List<IntakeLog>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(log: IntakeLog): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(logs: List<IntakeLog>)

    @Update
    suspend fun update(log: IntakeLog)

    @Query("""
        UPDATE intake_logs
        SET status = :status, actualTimestamp = :timestamp, updatedAt = :updatedAt
        WHERE id = :logId
    """)
    suspend fun updateStatus(
        logId: Long,
        status: EIntakeStatus,
        timestamp: Long? = null,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE intake_logs SET followUpCount = followUpCount + 1, updatedAt = :updatedAt WHERE id = :logId")
    suspend fun incrementFollowUpCount(logId: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM intake_logs WHERE scheduledDate < :date")
    suspend fun deleteLogsOlderThan(date: String)

    @Query("DELETE FROM intake_logs")
    suspend fun deleteAll()
}
```

### UserSettingsDao

```kotlin
@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getSettings(): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getSettingsOnce(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: UserSettings)

    @Update
    suspend fun update(settings: UserSettings)

    @Query("UPDATE user_settings SET onboardingCompleted = 1, updatedAt = :timestamp WHERE id = 1")
    suspend fun markOnboardingCompleted(timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE user_settings SET vacationModeActive = :isActive, updatedAt = :timestamp WHERE id = 1")
    suspend fun setVacationMode(isActive: Boolean, timestamp: Long = System.currentTimeMillis())
}
```

---

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/medtracker/
│   │   │   ├── TrackMedApplication.kt
│   │   │   │
│   │   │   ├── data/
│   │   │   │   ├── local/
│   │   │   │   │   ├── TrackMedDatabase.kt
│   │   │   │   │   ├── Converters.kt
│   │   │   │   │   ├── dao/
│   │   │   │   │   │   ├── MedicineDao.kt
│   │   │   │   │   │   ├── ScheduleDao.kt
│   │   │   │   │   │   ├── IntakeLogDao.kt
│   │   │   │   │   │   └── UserSettingsDao.kt
│   │   │   │   │   └── entity/
│   │   │   │   │       ├── Medicine.kt
│   │   │   │   │       ├── Schedule.kt
│   │   │   │   │       ├── IntakeLog.kt
│   │   │   │   │       ├── UserSettings.kt
│   │   │   │   │       └── Enums.kt
│   │   │   │   │
│   │   │   │   ├── repository/
│   │   │   │   │   ├── MedicineRepository.kt
│   │   │   │   │   ├── ScheduleRepository.kt
│   │   │   │   │   ├── IntakeLogRepository.kt
│   │   │   │   │   └── SettingsRepository.kt
│   │   │   │   │
│   │   │   │   └── backup/
│   │   │   │       ├── BackupModels.kt
│   │   │   │       └── BackupManager.kt
│   │   │   │
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   │   ├── DailyIntakeItem.kt
│   │   │   │   │   ├── MedicineWithSchedules.kt
│   │   │   │   │   └── TimeWindowConfig.kt
│   │   │   │   │
│   │   │   │   └── usecase/
│   │   │   │       ├── GetDailyIntakesUseCase.kt
│   │   │   │       ├── MarkIntakeUseCase.kt
│   │   │   │       ├── GenerateDailyLogsUseCase.kt
│   │   │   │       ├── CalculateDaysUntilEmptyUseCase.kt
│   │   │   │       └── CheckLowStockUseCase.kt
│   │   │   │
│   │   │   ├── ui/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   │
│   │   │   │   ├── navigation/
│   │   │   │   │   ├── TrackMedNavHost.kt
│   │   │   │   │   └── Screen.kt
│   │   │   │   │
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Color.kt
│   │   │   │   │   ├── Theme.kt
│   │   │   │   │   └── Type.kt
│   │   │   │   │
│   │   │   │   ├── components/
│   │   │   │   │   ├── IntakeCard.kt
│   │   │   │   │   ├── MedicineCard.kt
│   │   │   │   │   ├── TimeWindowPicker.kt
│   │   │   │   │   ├── DosagePicker.kt
│   │   │   │   │   ├── StockIndicator.kt
│   │   │   │   │   ├── TimeWindowIcon.kt
│   │   │   │   │   ├── ConfirmationDialog.kt
│   │   │   │   │   └── LoadingIndicator.kt
│   │   │   │   │
│   │   │   │   ├── screens/
│   │   │   │   │   ├── onboarding/
│   │   │   │   │   │   ├── OnboardingScreen.kt
│   │   │   │   │   │   └── OnboardingViewModel.kt
│   │   │   │   │   │
│   │   │   │   │   ├── home/
│   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   └── HomeViewModel.kt
│   │   │   │   │   │
│   │   │   │   │   ├── medicines/
│   │   │   │   │   │   ├── MedicineListScreen.kt
│   │   │   │   │   │   ├── MedicineListViewModel.kt
│   │   │   │   │   │   ├── AddEditMedicineScreen.kt
│   │   │   │   │   │   └── AddEditMedicineViewModel.kt
│   │   │   │   │   │
│   │   │   │   │   ├── schedule/
│   │   │   │   │   │   ├── ScheduleListScreen.kt
│   │   │   │   │   │   ├── AddScheduleScreen.kt
│   │   │   │   │   │   └── AddScheduleViewModel.kt
│   │   │   │   │   │
│   │   │   │   │   └── settings/
│   │   │   │   │       ├── SettingsScreen.kt
│   │   │   │   │       └── SettingsViewModel.kt
│   │   │   │   │
│   │   │   │   └── widget/
│   │   │   │       ├── TrackMedWidget.kt
│   │   │   │       ├── TrackMedWidgetReceiver.kt
│   │   │   │       └── WidgetRefreshWorker.kt
│   │   │   │
│   │   │   ├── notification/
│   │   │   │   ├── NotificationHelper.kt
│   │   │   │   ├── NotificationScheduler.kt
│   │   │   │   ├── AlarmReceiver.kt
│   │   │   │   ├── NotificationActionReceiver.kt
│   │   │   │   ├── BootReceiver.kt
│   │   │   │   ├── FollowUpWorker.kt
│   │   │   │   └── DailyLogGeneratorWorker.kt
│   │   │   │
│   │   │   ├── di/
│   │   │   │   ├── AppModule.kt
│   │   │   │   ├── DatabaseModule.kt
│   │   │   │   ├── RepositoryModule.kt
│   │   │   │   └── WorkerModule.kt
│   │   │   │
│   │   │   └── util/
│   │   │       ├── DateTimeUtil.kt
│   │   │       ├── Extensions.kt
│   │   │       └── Constants.kt
│   │   │
│   │   ├── res/
│   │   │   ├── drawable/
│   │   │   │   ├── ic_launcher_foreground.xml
│   │   │   │   ├── ic_launcher_background.xml
│   │   │   │   ├── ic_notification.xml
│   │   │   │   ├── ic_morning.xml
│   │   │   │   ├── ic_lunch.xml
│   │   │   │   ├── ic_evening.xml
│   │   │   │   ├── ic_custom_time.xml
│   │   │   │   ├── ic_check.xml
│   │   │   │   ├── ic_skip.xml
│   │   │   │   ├── ic_snooze.xml
│   │   │   │   └── widget_preview.png
│   │   │   │
│   │   │   ├── mipmap-*/
│   │   │   │   ├── ic_launcher.xml
│   │   │   │   └── ic_launcher_round.xml
│   │   │   │
│   │   │   ├── values/
│   │   │   │   ├── strings.xml
│   │   │   │   ├── colors.xml
│   │   │   │   └── themes.xml
│   │   │   │
│   │   │   ├── values-night/
│   │   │   │   └── themes.xml
│   │   │   │
│   │   │   └── xml/
│   │   │       ├── backup_rules.xml
│   │   │       ├── data_extraction_rules.xml
│   │   │       └── widget_info.xml
│   │   │
│   │   └── AndroidManifest.xml
│   │
│   ├── test/
│   │   └── java/com/medtracker/
│   │       ├── data/
│   │       │   └── repository/
│   │       │       └── MedicineRepositoryTest.kt
│   │       └── domain/
│   │           └── usecase/
│   │               ├── GenerateDailyLogsUseCaseTest.kt
│   │               └── CalculateDaysUntilEmptyUseCaseTest.kt
│   │
│   └── androidTest/
│       └── java/com/medtracker/
│           ├── data/
│           │   └── local/
│           │       └── TrackMedDatabaseTest.kt
│           └── ui/
│               └── screens/
│                   └── HomeScreenTest.kt
│
├── build.gradle.kts
├── proguard-rules.pro
├── .gitignore
├── README.md
├── LICENSE
└── CHANGELOG.md

// Project root
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── local.properties (gitignored)
└── gradle/
    └── libs.versions.toml
```

---

## Screen Specifications

### 0. Splash Screen (System API)

Uses Android 12+ Splash Screen API - configured in theme, no custom implementation needed.

### 1. Onboarding Screen

**Purpose:** Welcome new users and request necessary permissions

**Flow:**
1. Welcome message and app description
2. Notification permission request (Android 13+)
3. Exact alarm permission explanation
4. Quick tour of features (optional skip)

**ViewModel State:**
```kotlin
data class OnboardingUiState(
    val currentPage: Int = 0,
    val notificationPermissionGranted: Boolean = false,
    val alarmPermissionGranted: Boolean = false,
    val isComplete: Boolean = false
)
```

### 2. Home Screen (Daily View)

**Purpose:** Show today's intake schedule with quick actions

**UI Elements:**
- Header with current date and vacation mode toggle
- Progress indicator (e.g., "3 of 8 taken")
- Grouped sections: Morning, Lunch, Evening, Custom times
- Each intake item shows:
  - Medicine name
  - Dosage (e.g., "2 capsules")
  - Notes preview if exists
  - Time window icon (Material Icons, not emoji)
  - Status indicator (pending/taken/skipped/missed)
  - Low stock warning icon if applicable
- Swipe actions: swipe right = taken, swipe left = skipped
- Tap = expand to show notes + action buttons
- FAB to add new medicine
- Bottom navigation: Home, Medicines, Settings

**ViewModel State:**
```kotlin
data class HomeUiState(
    val currentDate: LocalDate = LocalDate.now(),
    val isVacationMode: Boolean = false,
    val morningIntakes: List<DailyIntakeItem> = emptyList(),
    val lunchIntakes: List<DailyIntakeItem> = emptyList(),
    val eveningIntakes: List<DailyIntakeItem> = emptyList(),
    val customIntakes: List<DailyIntakeItem> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val lowStockCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)
```

### 3. Medicine List Screen

**Purpose:** Manage all medicines and their inventory

**UI Elements:**
- Search bar
- List of medicine cards showing:
  - Name
  - Current stock with visual indicator (green/yellow/red)
  - Days until empty (calculated)
  - Active schedules count
  - Paused badge if applicable
- Sort/filter options: Name, Stock level, Recently added
- Swipe to archive (soft delete)
- Tap to view/edit
- FAB to add new medicine

**ViewModel State:**
```kotlin
data class MedicineListUiState(
    val medicines: List<MedicineWithSchedules> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: ESortOrder = ESortOrder.NAME,
    val isLoading: Boolean = true,
    val error: String? = null
)

enum class ESortOrder {
    NAME,
    STOCK_LOW_FIRST,
    RECENTLY_ADDED
}
```

### 4. Add/Edit Medicine Screen

**Purpose:** Create or modify a medicine entry

**Form Fields:**
- Medicine name (required, text input)
- Notes (optional, multiline - for intake instructions)
- Current stock (number input with +/- buttons)
- Restock threshold (number input)
- Pills per dose default (number input, default 1)
- Active toggle

**Validation:**
- Name is required and non-empty
- Stock values must be >= 0
- Threshold must be <= current stock (warning, not blocking)

**After Save:**
- If new medicine: prompt to add first schedule
- If edit: return to medicine list

### 5. Schedule Management

**Add Schedule Screen:**

**Form Fields:**
- Frequency: Daily / Weekly (segmented button)
- Day of week (if Weekly) - single select chips
- Time window: Morning / Lunch / Evening / Custom (segmented button)
- Custom time picker (if Custom selected)
- Dosage amount (number input with +/- buttons)

**Schedule List (within Medicine detail):**
- Shows all schedules for a medicine
- Each schedule shows: frequency, time window, dosage
- Swipe to delete
- Tap to edit

### 6. Settings Screen

**Purpose:** Configure app behavior

**Sections:**

1. **Time Windows**
   - Morning: start/end time pickers
   - Lunch: start/end time pickers
   - Evening: start/end time pickers

2. **Notifications**
   - Enable/disable toggle
   - Follow-up interval: 15/30/45/60 min (dropdown)
   - Max follow-ups: 1-5 (slider)
   - Test notification button

3. **Display**
   - 24-hour format toggle

4. **Data**
   - Export backup button (opens file picker)
   - Import backup button (opens file picker)
   - Clear all data (with confirmation dialog)

5. **About**
   - Version info
   - Open source licenses
   - GitHub link
   - Privacy policy link

**ViewModel State:**
```kotlin
data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val isLoading: Boolean = true,
    val exportStatus: EExportStatus = EExportStatus.IDLE,
    val importStatus: EImportStatus = EImportStatus.IDLE,
    val message: String? = null,
    val error: String? = null
)

enum class EExportStatus { IDLE, EXPORTING, SUCCESS, ERROR }
enum class EImportStatus { IDLE, IMPORTING, SUCCESS, ERROR }
```

---

## Notification System

### Architecture Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    NOTIFICATION FLOW                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  DailyLogGeneratorWorker (runs at midnight + on boot)   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                           │                                     │
│                           ▼                                     │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  Generate IntakeLogs for today's schedules              │   │
│  │  (Skip if log already exists for schedule+date)         │   │
│  └─────────────────────────────────────────────────────────┘   │
│                           │                                     │
│                           ▼                                     │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  NotificationScheduler.scheduleAllForDate()             │   │
│  │  Sets exact alarms via AlarmManager                     │   │
│  └─────────────────────────────────────────────────────────┘   │
│                           │                                     │
│                           ▼                                     │
│         ──────── TIME PASSES ────────                          │
│                           │                                     │
│                           ▼                                     │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  AlarmReceiver.onReceive() triggered                    │   │
│  └─────────────────────────────────────────────────────────┘   │
│                           │                                     │
│                           ▼                                     │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  Show notification with actions:                        │   │
│  │  [✓ Take]  [✗ Skip]  [⏰ Snooze]                        │   │
│  └─────────────────────────────────────────────────────────┘   │
│                           │                                     │
│       ┌───────────────────┼───────────────────┐                │
│       │                   │                   │                 │
│       ▼                   ▼                   ▼                 │
│   [Take]              [Skip]             [Snooze/No Action]    │
│       │                   │                   │                 │
│       ▼                   ▼                   ▼                 │
│  Update log          Update log         Schedule follow-up     │
│  status=TAKEN        status=SKIPPED     via WorkManager        │
│  Decrement stock     Dismiss            after interval         │
│  Dismiss             notification                              │
│  notification                                │                  │
│       │                   │                   │                 │
│       │                   │                   ▼                 │
│       │                   │         ┌────────────────────┐     │
│       │                   │         │ FollowUpWorker     │     │
│       │                   │         │ Checks if still    │     │
│       │                   │         │ PENDING            │     │
│       │                   │         └────────────────────┘     │
│       │                   │                   │                 │
│       │                   │         ┌─────────┴─────────┐      │
│       │                   │         ▼                   ▼      │
│       │                   │    followUpCount     followUpCount │
│       │                   │    < maxFollowUps    >= maxFollowUps│
│       │                   │         │                   │      │
│       │                   │         ▼                   ▼      │
│       │                   │    Send follow-up     Mark MISSED  │
│       │                   │    notification       Notify once  │
│       │                   │    Increment count                 │
│       │                   │                                    │
│       ▼                   ▼                   ▼                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  Update Widget                                          │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### NotificationHelper.kt

```kotlin
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_REMINDERS = "med_reminders"
        const val CHANNEL_RESTOCK = "med_restock"
        const val CHANNEL_MISSED = "med_missed"
        const val NOTIFICATION_GROUP = "med_tracker_group"

        const val ACTION_TAKE = "com.trackmed.ACTION_TAKE"
        const val ACTION_SKIP = "com.trackmed.ACTION_SKIP"
        const val ACTION_SNOOZE = "com.trackmed.ACTION_SNOOZE"

        const val EXTRA_LOG_ID = "log_id"
        const val EXTRA_MEDICINE_ID = "medicine_id"
        const val EXTRA_DOSAGE = "dosage"
    }

    fun createNotificationChannels() {
        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDERS,
            context.getString(R.string.channel_reminders),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_reminders_desc)
            enableVibration(true)
            setShowBadge(true)
        }

        val restockChannel = NotificationChannel(
            CHANNEL_RESTOCK,
            context.getString(R.string.channel_restock),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.channel_restock_desc)
        }

        val missedChannel = NotificationChannel(
            CHANNEL_MISSED,
            context.getString(R.string.channel_missed),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.channel_missed_desc)
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(listOf(reminderChannel, restockChannel, missedChannel))
    }

    fun buildIntakeReminder(
        logId: Long,
        medicineId: Long,
        medicineName: String,
        dosage: Int,
        notes: String?
    ): Notification {
        val contentIntent = createContentIntent(logId)
        val takeIntent = createActionIntent(ACTION_TAKE, logId, medicineId, dosage)
        val skipIntent = createActionIntent(ACTION_SKIP, logId, medicineId, dosage)
        val snoozeIntent = createActionIntent(ACTION_SNOOZE, logId, medicineId, dosage)

        val dosageText = context.resources.getQuantityString(
            R.plurals.pills_count, dosage, dosage
        )

        val contentText = buildString {
            append(dosageText)
            notes?.let { append("\n$it") }
        }

        return NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title, medicineName))
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .addAction(R.drawable.ic_check, context.getString(R.string.action_take), takeIntent)
            .addAction(R.drawable.ic_skip, context.getString(R.string.action_skip), skipIntent)
            .addAction(R.drawable.ic_snooze, context.getString(R.string.action_snooze), snoozeIntent)
            .setGroup(NOTIFICATION_GROUP)
            .build()
    }

    fun showRestockAlert(medicines: List<Medicine>) {
        if (medicines.isEmpty()) return

        val title = context.getString(R.string.restock_alert_title)
        val content = medicines.joinToString(", ") { it.name }

        val notification = NotificationCompat.Builder(context, CHANNEL_RESTOCK)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(RESTOCK_NOTIFICATION_ID, notification)
    }

    private fun createContentIntent(logId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_LOG_ID, logId)
        }
        return PendingIntent.getActivity(
            context, logId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createActionIntent(action: String, logId: Long, medicineId: Long, dosage: Int): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_LOG_ID, logId)
            putExtra(EXTRA_MEDICINE_ID, medicineId)
            putExtra(EXTRA_DOSAGE, dosage)
        }
        val requestCode = (logId * 10 + when(action) {
            ACTION_TAKE -> 1
            ACTION_SKIP -> 2
            else -> 3
        }).toInt()

        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val RESTOCK_NOTIFICATION_ID = 999999
    }
}
```

### NotificationScheduler.kt

```kotlin
@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scheduleRepository: ScheduleRepository,
    private val intakeLogRepository: IntakeLogRepository,
    private val settingsRepository: SettingsRepository
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    suspend fun scheduleAllForDate(date: LocalDate) {
        val settings = settingsRepository.getSettingsOnce() ?: return
        if (!settings.notificationsEnabled) return

        val logs = intakeLogRepository.getPendingLogsForDateOnce(date.toString())

        logs.forEach { log ->
            val triggerTime = calculateTriggerTime(date, log, settings)
            if (triggerTime > System.currentTimeMillis()) {
                scheduleAlarm(log.id, triggerTime)
            }
        }
    }

    private fun calculateTriggerTime(
        date: LocalDate,
        log: IntakeLog,
        settings: UserSettings
    ): Long {
        val time = LocalTime.of(log.scheduledTimeHour, log.scheduledTimeMinute)
        return LocalDateTime.of(date, time)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun scheduleAlarm(logId: Long, triggerTimeMillis: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(NotificationHelper.EXTRA_LOG_ID, logId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            logId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(logId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            logId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    fun scheduleFollowUp(logId: Long, delayMinutes: Int) {
        val request = OneTimeWorkRequestBuilder<FollowUpWorker>()
            .setInitialDelay(delayMinutes.toLong(), TimeUnit.MINUTES)
            .setInputData(workDataOf(NotificationHelper.EXTRA_LOG_ID to logId))
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}
```

### DailyLogGeneratorWorker.kt

```kotlin
@HiltWorker
class DailyLogGeneratorWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val generateDailyLogsUseCase: GenerateDailyLogsUseCase,
    private val notificationScheduler: NotificationScheduler,
    private val checkLowStockUseCase: CheckLowStockUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val today = LocalDate.now()

            // Generate intake logs for today
            generateDailyLogsUseCase(today)

            // Schedule notifications
            notificationScheduler.scheduleAllForDate(today)

            // Check for low stock and notify
            checkLowStockUseCase()

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val WORK_NAME = "daily_log_generator"

        fun scheduleDailyWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()

            // Calculate delay until next midnight
            val now = LocalDateTime.now()
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
            val delayMillis = Duration.between(now, nextMidnight).toMillis()

            val request = PeriodicWorkRequestBuilder<DailyLogGeneratorWorker>(
                1, TimeUnit.DAYS
            )
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun runImmediately(context: Context) {
            val request = OneTimeWorkRequestBuilder<DailyLogGeneratorWorker>()
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
```

---

## Widget Implementation

### TrackMedWidget.kt (Glance)

```kotlin
class TrackMedWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(120.dp, 120.dp),  // Small
            DpSize(250.dp, 180.dp),  // Medium
            DpSize(300.dp, 300.dp)   // Large
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = TrackMedDatabase.getInstance(context)
        val today = LocalDate.now().toString()

        val logs = database.intakeLogDao().getLogsForDateOnce(today)
        val medicines = database.medicineDao().getAllMedicinesOnce()
            .associateBy { it.id }

        val intakes = logs.mapNotNull { log ->
            medicines[log.medicineId]?.let { medicine ->
                WidgetIntakeItem(
                    logId = log.id,
                    medicineName = medicine.name,
                    dosageAmount = log.dosageAmount,
                    timeWindow = log.timeWindow,
                    status = log.status
                )
            }
        }

        provideContent {
            GlanceTheme {
                TrackMedWidgetContent(intakes)
            }
        }
    }
}

data class WidgetIntakeItem(
    val logId: Long,
    val medicineName: String,
    val dosageAmount: Int,
    val timeWindow: ETimeWindow,
    val status: EIntakeStatus
)

@Composable
fun TrackMedWidgetContent(intakes: List<WidgetIntakeItem>) {
    val context = LocalContext.current
    val size = LocalSize.current

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(16.dp)
            .padding(12.dp)
    ) {
        // Header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = context.getString(R.string.widget_title),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GlanceTheme.colors.onSurface
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Progress
        val completed = intakes.count { it.status == EIntakeStatus.TAKEN }
        val total = intakes.size

        Text(
            text = "$completed / $total",
            style = TextStyle(
                fontSize = 12.sp,
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Intake list (show more items for larger widgets)
        val maxItems = when {
            size.height >= 300.dp -> 8
            size.height >= 180.dp -> 5
            else -> 3
        }

        LazyColumn {
            items(intakes.take(maxItems)) { intake ->
                WidgetIntakeItem(intake)
            }
        }

        if (intakes.size > maxItems) {
            Text(
                text = context.getString(R.string.widget_more, intakes.size - maxItems),
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun WidgetIntakeItem(intake: WidgetIntakeItem) {
    val context = LocalContext.current

    val actionTake = actionRunCallback<TakeActionCallback>(
        parameters = actionParametersOf(
            ActionParameters.Key<Long>("log_id") to intake.logId
        )
    )

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(actionTake),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status indicator
        Box(
            modifier = GlanceModifier
                .size(24.dp)
                .cornerRadius(6.dp)
                .background(
                    when (intake.status) {
                        EIntakeStatus.TAKEN -> ColorProvider(Color(0xFF10B981))
                        EIntakeStatus.SKIPPED -> ColorProvider(Color(0xFFF59E0B))
                        EIntakeStatus.MISSED -> ColorProvider(Color(0xFFEF4444))
                        EIntakeStatus.PENDING -> ColorProvider(Color(0xFFE2E8F0))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (intake.status == EIntakeStatus.TAKEN) {
                Image(
                    provider = ImageProvider(R.drawable.ic_check),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(ColorProvider(Color.White))
                )
            }
        }

        Spacer(modifier = GlanceModifier.width(12.dp))

        // Medicine info
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = intake.medicineName,
                maxLines = 1,
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurface,
                    textDecoration = if (intake.status == EIntakeStatus.TAKEN)
                        TextDecoration.LineThrough else null
                )
            )
            Text(
                text = context.resources.getQuantityString(
                    R.plurals.pills_count,
                    intake.dosageAmount,
                    intake.dosageAmount
                ),
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }

        // Time window icon
        Image(
            provider = ImageProvider(
                when (intake.timeWindow) {
                    ETimeWindow.MORNING -> R.drawable.ic_morning
                    ETimeWindow.LUNCH -> R.drawable.ic_lunch
                    ETimeWindow.EVENING -> R.drawable.ic_evening
                    ETimeWindow.CUSTOM -> R.drawable.ic_custom_time
                }
            ),
            contentDescription = null,
            modifier = GlanceModifier.size(20.dp),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant)
        )
    }
}

class TakeActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val logId = parameters[ActionParameters.Key<Long>("log_id")] ?: return

        val database = TrackMedDatabase.getInstance(context)
        val log = database.intakeLogDao().getLogById(logId) ?: return

        if (log.status == EIntakeStatus.PENDING) {
            // Update log status
            database.intakeLogDao().updateStatus(
                logId = logId,
                status = EIntakeStatus.TAKEN,
                timestamp = System.currentTimeMillis()
            )

            // Decrement stock
            database.medicineDao().decrementStock(log.medicineId, log.dosageAmount)

            // Update widget
            TrackMedWidget().update(context, glanceId)
        }
    }
}
```

### TrackMedWidgetReceiver.kt

```kotlin
class TrackMedWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TrackMedWidget()
}
```

---

## Backup & Restore

### BackupManager.kt

```kotlin
@Singleton
class BackupManager @Inject constructor(
    private val database: TrackMedDatabase,
    @ApplicationContext private val context: Context
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun exportToJson(): String {
        val medicines = database.medicineDao().getAllMedicinesOnce()
        val schedules = database.scheduleDao().getAllSchedulesOnce()
        val logs = database.intakeLogDao().getAllLogsOnce()
        val settings = database.userSettingsDao().getSettingsOnce()

        val backup = BackupData(
            version = BACKUP_VERSION,
            appVersion = BuildConfig.VERSION_NAME,
            exportedAt = Instant.now().toString(),
            medicines = medicines.map { it.toBackup() },
            schedules = schedules.map { it.toBackup() },
            intakeLogs = logs.map { it.toBackup() },
            settings = settings?.toBackup() ?: UserSettingsBackup()
        )

        return json.encodeToString(backup)
    }

    suspend fun importFromJson(jsonString: String): ImportResult {
        return try {
            val backup = json.decodeFromString<BackupData>(jsonString)

            // Validate version
            if (backup.version > BACKUP_VERSION) {
                return ImportResult.Error(
                    context.getString(R.string.import_error_version)
                )
            }

            database.withTransaction {
                // Clear existing data
                database.intakeLogDao().deleteAll()
                database.scheduleDao().run { /* clear */ }
                database.medicineDao().run { /* clear */ }

                // Import medicines (maintaining IDs for relationships)
                backup.medicines.forEach { med ->
                    database.medicineDao().insert(med.toEntity())
                }

                // Import schedules
                backup.schedules.forEach { schedule ->
                    database.scheduleDao().insert(schedule.toEntity())
                }

                // Import logs
                backup.intakeLogs.forEach { log ->
                    database.intakeLogDao().insert(log.toEntity())
                }

                // Import settings
                database.userSettingsDao().insert(backup.settings.toEntity())
            }

            ImportResult.Success(
                medicinesImported = backup.medicines.size,
                schedulesImported = backup.schedules.size
            )
        } catch (e: SerializationException) {
            ImportResult.Error(context.getString(R.string.import_error_invalid))
        } catch (e: Exception) {
            ImportResult.Error(e.message ?: context.getString(R.string.import_error_unknown))
        }
    }

    companion object {
        const val BACKUP_VERSION = 1
        const val BACKUP_FILE_NAME = "medtracker_backup.json"
        const val BACKUP_MIME_TYPE = "application/json"
    }
}

sealed class ImportResult {
    data class Success(
        val medicinesImported: Int,
        val schedulesImported: Int
    ) : ImportResult()

    data class Error(val message: String) : ImportResult()
}

// Extension functions for conversion
private fun Medicine.toBackup() = MedicineBackup(
    id = id,
    name = name,
    notes = notes,
    currentStock = currentStock,
    restockThreshold = restockThreshold,
    pillsPerDose = pillsPerDose,
    isActive = isActive,
    isPaused = isPaused,
    createdAt = createdAt
)

private fun MedicineBackup.toEntity() = Medicine(
    id = id,
    name = name,
    notes = notes,
    currentStock = currentStock,
    restockThreshold = restockThreshold,
    pillsPerDose = pillsPerDose,
    isActive = isActive,
    isPaused = isPaused,
    createdAt = createdAt
)

// Similar extension functions for Schedule, IntakeLog, UserSettings...
```

---

## Implementation Phases

### Phase 1: Foundation

**Goals:** Project compiles, database works, navigation in place

**Tasks:**
1. Create new Android project in Android Studio
2. Configure build.gradle with all dependencies
3. Set up version catalog (libs.versions.toml)
4. Implement Room database with all entities and DAOs
5. Set up Hilt dependency injection modules
6. Create repository layer
7. Set up Compose theme (Material 3, colors, typography)
8. Implement Navigation 3 with screen definitions
9. Create MainActivity with NavHost
10. Add app icon (adaptive icon)
11. Configure splash screen

**Verification:**
- [ ] App builds and runs
- [ ] Can insert/read from database via repository
- [ ] Navigation between placeholder screens works
- [ ] Theme applies correctly (light/dark)
- [ ] App icon displays correctly

### Phase 2: Core Features

**Goals:** Fully functional medicine and intake management

**Tasks:**
1. Implement Medicine List screen + ViewModel
2. Implement Add/Edit Medicine screen + ViewModel
3. Implement Schedule management screens
4. Implement Home screen with daily view
5. Implement intake card component with swipe actions
6. Implement GenerateDailyLogsUseCase
7. Implement MarkIntakeUseCase (take/skip)
8. Implement stock decrement logic
9. Implement CalculateDaysUntilEmptyUseCase
10. Add low stock indicator components
11. Implement bottom navigation

**Verification:**
- [ ] Can add/edit/delete medicines
- [ ] Can add multiple schedules per medicine
- [ ] Home screen shows today's intakes grouped by time window
- [ ] Can mark intake as taken (stock decrements)
- [ ] Can mark intake as skipped
- [ ] Low stock medicines show warning
- [ ] Swipe gestures work correctly

### Phase 3: Notifications

**Goals:** Working notification system with follow-ups

**Tasks:**
1. Create notification channels
2. Implement NotificationHelper
3. Implement NotificationScheduler
4. Implement AlarmReceiver
5. Implement NotificationActionReceiver
6. Implement BootReceiver (reschedule after reboot)
7. Implement DailyLogGeneratorWorker
8. Implement FollowUpWorker
9. Add Android 13+ notification permission handling
10. Add exact alarm permission handling (Android 12+)
11. Test notification timing accuracy

**Verification:**
- [ ] Notifications appear at scheduled times
- [ ] Take action marks as taken and decrements stock
- [ ] Skip action marks as skipped
- [ ] Snooze reschedules follow-up
- [ ] Auto-miss after max follow-ups
- [ ] Notifications reschedule after device reboot
- [ ] Works on Android 13+ (permission granted)

### Phase 4: Widget

**Goals:** Working interactive home screen widget

**Tasks:**
1. Implement TrackMedWidget with Glance
2. Implement widget receiver
3. Create time window icons (ic_morning, ic_lunch, ic_evening, ic_custom_time)
4. Implement TakeActionCallback for widget interaction
5. Implement widget refresh when data changes
6. Create widget preview image
7. Configure widget_info.xml
8. Test on different widget sizes

**Verification:**
- [ ] Widget can be added to home screen
- [ ] Widget shows today's intakes
- [ ] Tapping item marks as taken
- [ ] Widget updates when data changes
- [ ] Widget handles different sizes gracefully
- [ ] Widget respects dark mode

### Phase 5: Settings & Polish

**Goals:** Complete settings, backup/restore, polish

**Tasks:**
1. Implement Settings screen
2. Implement time window configuration
3. Implement notification settings
4. Implement BackupManager
5. Add export functionality with file picker
6. Add import functionality with file picker
7. Implement onboarding flow
8. Add vacation mode (global + per-medicine)
9. Implement data clear with confirmation
10. Add loading states and error handling throughout
11. Add empty states
12. Polish animations and transitions
13. Accessibility review (content descriptions, touch targets)
14. Prepare for release (ProGuard, signing)

**Verification:**
- [ ] All settings persist and take effect
- [ ] Backup exports valid JSON file
- [ ] Import restores data correctly
- [ ] Onboarding shows on first launch only
- [ ] Vacation mode pauses all notifications
- [ ] App handles errors gracefully
- [ ] No crashes in normal usage
- [ ] Release build works correctly

---

## Dependencies

### gradle/libs.versions.toml

```toml
[versions]
# SDK
compileSdk = "35"
minSdk = "26"
targetSdk = "35"

# Kotlin
kotlin = "2.2.20"
ksp = "2.2.20-2.0.4"
coroutines = "1.9.0"
serialization = "1.9.0"

# Android
agp = "8.7.3"
coreKtx = "1.15.0"
lifecycleKtx = "2.8.7"
activityCompose = "1.9.3"
splashscreen = "1.0.1"

# Compose
composeBom = "2025.12.00"
navigation3 = "1.0.0"

# Room
room = "2.8.4"

# Hilt
hilt = "2.57.1"
hiltNavigation = "1.2.0"
hiltWork = "1.2.0"

# WorkManager
work = "2.11.0"

# Glance
glance = "1.1.1"

# Testing
junit = "4.13.2"
junitExt = "1.2.1"
espresso = "3.6.1"
mockk = "1.13.13"
turbine = "1.2.0"
coroutinesTest = "1.9.0"

[libraries]
# Core Android
core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleKtx" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleKtx" }
activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
splashscreen = { group = "androidx.core", name = "core-splashscreen", version.ref = "splashscreen" }

# Compose BOM
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-material-icons = { group = "androidx.compose.material", name = "material-icons-extended" }

# Navigation 3
navigation3-runtime = { group = "androidx.navigation3", name = "navigation3-runtime", version.ref = "navigation3" }
navigation3-compose = { group = "androidx.navigation3", name = "navigation3-ui", version.ref = "navigation3" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigation" }
hilt-work = { group = "androidx.hilt", name = "hilt-work", version.ref = "hiltWork" }
hilt-work-compiler = { group = "androidx.hilt", name = "hilt-compiler", version.ref = "hiltWork" }

# WorkManager
work-runtime = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }

# Glance (Widget)
glance-appwidget = { group = "androidx.glance", name = "glance-appwidget", version.ref = "glance" }
glance-material3 = { group = "androidx.glance", name = "glance-material3", version.ref = "glance" }

# Serialization
serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "serialization" }

# Coroutines
coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Testing
junit = { group = "junit", name = "junit", version.ref = "junit" }
junit-ext = { group = "androidx.test.ext", name = "junit", version.ref = "junitExt" }
espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espresso" }
compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutinesTest" }
room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

### app/build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.trackmed"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.trackmed"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Room schema export
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Signing config for release - configure in local.properties
            // signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core desugaring for java.time on older APIs
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

    // Core Android
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.activity.compose)
    implementation(libs.splashscreen)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)

    // Navigation 3
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.work.compiler)

    // WorkManager
    implementation(libs.work.runtime)

    // Glance (Widget)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    // Serialization
    implementation(libs.serialization.json)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Debug
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.coroutines.test)

    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.room.testing)
}
```

---

## Manifest & Permissions

### AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Notifications -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <!-- Exact alarms for reminders -->
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.USE_EXACT_ALARM" />

    <!-- Wake device for alarms -->
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <!-- Boot receiver to reschedule alarms -->
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <!-- Foreground service for reliable notifications -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />

    <application
        android:name=".TrackMedApplication"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.TrackMed">

        <!-- Main Activity -->
        <activity
            android:name=".ui.MainActivity"
            android:exported="true"
            android:theme="@style/Theme.TrackMed.Splash">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Alarm Receiver -->
        <receiver
            android:name=".notification.AlarmReceiver"
            android:exported="false" />

        <!-- Notification Action Receiver -->
        <receiver
            android:name=".notification.NotificationActionReceiver"
            android:exported="false" />

        <!-- Boot Receiver -->
        <receiver
            android:name=".notification.BootReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.QUICKBOOT_POWERON" />
            </intent-filter>
        </receiver>

        <!-- Widget -->
        <receiver
            android:name=".ui.widget.TrackMedWidgetReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/widget_info" />
        </receiver>

        <!-- WorkManager Initializer (disabled for Hilt) -->
        <provider
            android:name="androidx.startup.InitializationProvider"
            android:authorities="${applicationId}.androidx-startup"
            android:exported="false"
            tools:node="merge">
            <meta-data
                android:name="androidx.work.WorkManagerInitializer"
                android:value="androidx.startup"
                tools:node="remove" />
        </provider>

    </application>

</manifest>
```

---

## Key Algorithms

### CalculateDaysUntilEmptyUseCase

```kotlin
class CalculateDaysUntilEmptyUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(medicine: Medicine): Int? {
        if (medicine.currentStock <= 0) return 0

        val schedules = scheduleRepository.getSchedulesForMedicineOnce(medicine.id)
            .filter { it.isActive }

        if (schedules.isEmpty()) return null

        // Calculate weekly consumption
        val weeklyConsumption = schedules.sumOf { schedule ->
            when (schedule.frequencyType) {
                EFrequencyType.DAILY -> schedule.dosageAmount * 7
                EFrequencyType.WEEKLY -> schedule.dosageAmount
            }
        }

        if (weeklyConsumption <= 0) return null

        val dailyConsumption = weeklyConsumption / 7.0
        return (medicine.currentStock / dailyConsumption).toInt()
    }
}
```

### GenerateDailyLogsUseCase

```kotlin
class GenerateDailyLogsUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val intakeLogRepository: IntakeLogRepository,
    private val medicineRepository: MedicineRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(date: LocalDate) {
        val dayOfWeek = date.dayOfWeek.value // 1 = Monday, 7 = Sunday
        val dateString = date.toString()
        val settings = settingsRepository.getSettingsOnce() ?: UserSettings()

        // Skip if vacation mode is active
        if (settings.vacationModeActive) return

        val schedules = scheduleRepository.getActiveSchedulesForDayOnce(dayOfWeek)

        val logsToInsert = schedules.mapNotNull { schedule ->
            // Check if log already exists
            val existingLog = intakeLogRepository.getLogForScheduleAndDate(
                schedule.id,
                dateString
            )

            if (existingLog != null) return@mapNotNull null

            val (hour, minute) = getScheduledTime(schedule, settings)

            IntakeLog(
                scheduleId = schedule.id,
                medicineId = schedule.medicineId,
                scheduledDate = dateString,
                scheduledTimeHour = hour,
                scheduledTimeMinute = minute,
                timeWindow = schedule.timeWindow,
                dosageAmount = schedule.dosageAmount,
                status = EIntakeStatus.PENDING
            )
        }

        if (logsToInsert.isNotEmpty()) {
            intakeLogRepository.insertAll(logsToInsert)
        }
    }

    private fun getScheduledTime(schedule: Schedule, settings: UserSettings): Pair<Int, Int> {
        return when (schedule.timeWindow) {
            ETimeWindow.MORNING -> parseTime(settings.morningStart)
            ETimeWindow.LUNCH -> parseTime(settings.lunchStart)
            ETimeWindow.EVENING -> parseTime(settings.eveningStart)
            ETimeWindow.CUSTOM -> Pair(
                schedule.customTimeHour ?: 8,
                schedule.customTimeMinute ?: 0
            )
        }
    }

    private fun parseTime(timeString: String): Pair<Int, Int> {
        val parts = timeString.split(":")
        return Pair(parts[0].toInt(), parts[1].toInt())
    }
}
```

### MarkIntakeUseCase

```kotlin
class MarkIntakeUseCase @Inject constructor(
    private val intakeLogRepository: IntakeLogRepository,
    private val medicineRepository: MedicineRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(
        logId: Long,
        status: EIntakeStatus
    ): Result<Unit> {
        val log = intakeLogRepository.getLogById(logId)
            ?: return Result.failure(IllegalArgumentException("Log not found"))

        // Update log status
        intakeLogRepository.updateStatus(
            logId = logId,
            status = status,
            timestamp = if (status == EIntakeStatus.TAKEN) System.currentTimeMillis() else null
        )

        // If taken, decrement stock
        if (status == EIntakeStatus.TAKEN) {
            medicineRepository.decrementStock(log.medicineId, log.dosageAmount)
        }

        // Cancel any pending follow-up alarms
        notificationScheduler.cancelAlarm(logId)

        return Result.success(Unit)
    }
}
```

---

## Testing Strategy

### Unit Tests

| Test Class | What It Tests |
|------------|---------------|
| `CalculateDaysUntilEmptyUseCaseTest` | Daily/weekly calculations, edge cases |
| `GenerateDailyLogsUseCaseTest` | Log generation, vacation mode, duplicates |
| `MarkIntakeUseCaseTest` | Status updates, stock decrement |
| `MedicineRepositoryTest` | CRUD operations |
| `BackupManagerTest` | JSON serialization/deserialization |

### Integration Tests

| Test Class | What It Tests |
|------------|---------------|
| `TrackMedDatabaseTest` | Database operations, migrations |
| `NotificationSchedulerTest` | Alarm scheduling accuracy |

### UI Tests

| Test Class | What It Tests |
|------------|---------------|
| `HomeScreenTest` | Daily view, swipe actions |
| `AddMedicineFlowTest` | Complete add flow |
| `SettingsScreenTest` | Settings persistence |

### Test Examples

```kotlin
// GenerateDailyLogsUseCaseTest.kt
class GenerateDailyLogsUseCaseTest {

    private lateinit var useCase: GenerateDailyLogsUseCase
    private lateinit var scheduleRepository: ScheduleRepository
    private lateinit var intakeLogRepository: IntakeLogRepository
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setup() {
        scheduleRepository = mockk()
        intakeLogRepository = mockk()
        settingsRepository = mockk()

        useCase = GenerateDailyLogsUseCase(
            scheduleRepository,
            intakeLogRepository,
            mockk(), // medicineRepository
            settingsRepository
        )
    }

    @Test
    fun `invoke with daily schedule creates log for today`() = runTest {
        // Arrange
        val today = LocalDate.of(2025, 12, 26)
        val schedule = Schedule(
            id = 1,
            medicineId = 1,
            frequencyType = EFrequencyType.DAILY,
            timeWindow = ETimeWindow.MORNING,
            dosageAmount = 2
        )
        val settings = UserSettings()

        coEvery { settingsRepository.getSettingsOnce() } returns settings
        coEvery { scheduleRepository.getActiveSchedulesForDayOnce(any()) } returns listOf(schedule)
        coEvery { intakeLogRepository.getLogForScheduleAndDate(any(), any()) } returns null
        coEvery { intakeLogRepository.insertAll(any()) } just Runs

        // Act
        useCase(today)

        // Assert
        coVerify {
            intakeLogRepository.insertAll(match { logs ->
                logs.size == 1 &&
                logs[0].scheduleId == 1L &&
                logs[0].scheduledDate == "2025-12-26" &&
                logs[0].status == EIntakeStatus.PENDING
            })
        }
    }

    @Test
    fun `invoke with vacation mode does not create logs`() = runTest {
        // Arrange
        val today = LocalDate.of(2025, 12, 26)
        val settings = UserSettings(vacationModeActive = true)

        coEvery { settingsRepository.getSettingsOnce() } returns settings

        // Act
        useCase(today)

        // Assert
        coVerify(exactly = 0) { intakeLogRepository.insertAll(any()) }
    }

    @Test
    fun `invoke skips existing logs`() = runTest {
        // Arrange
        val today = LocalDate.of(2025, 12, 26)
        val schedule = Schedule(
            id = 1,
            medicineId = 1,
            frequencyType = EFrequencyType.DAILY,
            timeWindow = ETimeWindow.MORNING,
            dosageAmount = 2
        )
        val existingLog = IntakeLog(
            id = 1,
            scheduleId = 1,
            medicineId = 1,
            scheduledDate = "2025-12-26",
            scheduledTimeHour = 7,
            scheduledTimeMinute = 0,
            timeWindow = ETimeWindow.MORNING,
            dosageAmount = 2
        )
        val settings = UserSettings()

        coEvery { settingsRepository.getSettingsOnce() } returns settings
        coEvery { scheduleRepository.getActiveSchedulesForDayOnce(any()) } returns listOf(schedule)
        coEvery { intakeLogRepository.getLogForScheduleAndDate(1, "2025-12-26") } returns existingLog
        coEvery { intakeLogRepository.insertAll(any()) } just Runs

        // Act
        useCase(today)

        // Assert
        coVerify {
            intakeLogRepository.insertAll(match { it.isEmpty() })
        }
    }
}
```

---

## Open Source & Google Play

### Open Source Setup

**Required Files:**

1. **LICENSE** (Apache 2.0)
```
Apache License
Version 2.0, January 2004
...
```

2. **README.md**
```markdown
# TrackMed

A simple, privacy-focused medicine tracking app for Android.

## Features
- Track medicines and vitamins
- Flexible scheduling (daily, weekly, custom times)
- Smart notifications with snooze
- Inventory tracking with low-stock alerts
- Home screen widget
- Local-only storage (no cloud, no account)
- Export/import backup

## Download
[<img src="..." alt="Get it on Google Play" height="80">]()

## Building
1. Clone the repository
2. Open in Android Studio
3. Build and run

## Contributing
...

## License
Apache 2.0
```

3. **CHANGELOG.md**
```markdown
# Changelog

## [1.0.0] - 2025-XX-XX
### Added
- Initial release
- Medicine management
- Scheduling (daily/weekly)
- Notifications with follow-ups
- Home screen widget
- Backup/restore
```

4. **.github/ISSUE_TEMPLATE/** - Bug report and feature request templates

### Google Play Setup

**Requirements:**

1. **App Signing**
   - Create keystore for release signing
   - Configure in `local.properties` (gitignored):
   ```properties
   RELEASE_STORE_FILE=path/to/keystore.jks
   RELEASE_STORE_PASSWORD=xxx
   RELEASE_KEY_ALIAS=xxx
   RELEASE_KEY_PASSWORD=xxx
   ```

2. **ProGuard Rules** (`proguard-rules.pro`)
```proguard
# Keep Room entities
-keep class com.trackmed.data.local.entity.** { *; }

# Keep Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class com.trackmed.data.backup.** {
    *** Companion;
}

-keepclasseswithmembers class com.trackmed.data.backup.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Glance
-keep class androidx.glance.** { *; }
```

3. **Privacy Policy**
   - Required for apps with personal data
   - Host on GitHub Pages or similar
   - Content: "TrackMed stores all data locally on your device. No data is collected, transmitted, or shared."

4. **Store Listing**
   - Screenshots (phone + tablet if supported)
   - Feature graphic
   - Short description (80 chars)
   - Full description
   - Category: Health & Fitness or Medical

---

## Future Enhancements (Out of Scope for v1)

| Feature | Priority | Notes |
|---------|----------|-------|
| Cloud sync | Low | Privacy concern, complexity |
| Multiple profiles | Medium | Family sharing |
| Barcode scanning | Low | Add medicine by scanning |
| Pharmacy integration | Low | Auto-reorder |
| Interaction warnings | Medium | Drug interaction database |
| Statistics/Reports | Medium | Adherence tracking |
| Wear OS companion | Low | Watch notifications |
| Localization | High | i18n structure ready |

---

## Summary

This document provides a complete implementation blueprint for TrackMed v1.0. The architecture prioritizes:

1. **Simplicity** - Single user, local storage, no cloud
2. **Reliability** - Exact alarms, WorkManager for background tasks
3. **Privacy** - All data on device, no telemetry
4. **Maintainability** - Clean Architecture, MVVM, comprehensive testing

**Key Success Metrics:**
1. Notifications fire reliably at scheduled times
2. Widget updates instantly when intake is marked
3. Stock tracking is accurate
4. Backup/restore works without data loss
5. No crashes in normal usage

**Dependencies Updated:** December 2025
- Compose BOM 2025.12.00
- Room 2.8.4
- Hilt 2.57.1
- Navigation 3 1.0.0
- Glance 1.1.1
- KSP (not kapt)

**Sources:**
- [Compose December '25 Release](https://android-developers.googleblog.com/2025/12/whats-new-in-jetpack-compose-december.html)
- [Room Releases](https://developer.android.com/jetpack/androidx/releases/room)
- [Hilt Releases](https://github.com/google/dagger/releases)
- [Navigation 3 Stable](https://android-developers.googleblog.com/2025/11/jetpack-navigation-3-is-stable.html)
- [Glance Releases](https://developer.android.com/jetpack/androidx/releases/glance)
- [KSP Releases](https://github.com/google/ksp/releases)
