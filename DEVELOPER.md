# TrackMed Developer Guide

This document provides architectural details, design decisions, and guidance for developers contributing to TrackMed.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Project Structure](#project-structure)
- [Data Flow](#data-flow)
- [Key Components](#key-components)
- [Adding New Features](#adding-new-features)
- [Common Tasks](#common-tasks)
- [Testing](#testing)
- [Code Style](#code-style)

## Architecture Overview

TrackMed follows **MVVM + Clean Architecture** with the following layers:

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                    │
│  Screens → ViewModels → UI State                        │
├─────────────────────────────────────────────────────────┤
│                    Domain Layer                          │
│  Use Cases → Domain Models                               │
├─────────────────────────────────────────────────────────┤
│                    Data Layer                            │
│  Repositories → DAOs → Room Database                     │
└─────────────────────────────────────────────────────────┘
```

### Why This Architecture?

1. **Separation of Concerns**: Each layer has a single responsibility
2. **Testability**: Use cases and repositories can be unit tested in isolation
3. **Maintainability**: Changes in one layer don't cascade to others
4. **Scalability**: Easy to add features without touching unrelated code

## Project Structure

```
app/src/main/java/com/trackmed/
│
├── data/                           # Data Layer
│   ├── local/
│   │   ├── dao/                    # Room Data Access Objects
│   │   │   ├── MedicineDao.kt      # CRUD for medicines
│   │   │   ├── ScheduleDao.kt      # CRUD for schedules
│   │   │   ├── IntakeLogDao.kt     # Daily intake tracking
│   │   │   └── UserSettingsDao.kt  # App settings
│   │   ├── entity/                 # Database entities
│   │   │   ├── Medicine.kt
│   │   │   ├── Schedule.kt
│   │   │   ├── IntakeLog.kt
│   │   │   ├── UserSettings.kt
│   │   │   └── E*.kt               # Enums (EFrequencyType, ETimeWindow, etc.)
│   │   ├── Converters.kt           # Room type converters
│   │   └── TrackMedDatabase.kt     # Database singleton
│   └── repository/                 # Repository implementations
│       ├── MedicineRepository.kt
│       ├── ScheduleRepository.kt
│       ├── IntakeLogRepository.kt
│       └── SettingsRepository.kt
│
├── domain/                         # Domain Layer
│   ├── model/                      # Business models (UI-friendly)
│   │   ├── MedicineWithSchedules.kt
│   │   └── DailyIntakeItem.kt
│   └── usecase/                    # Business logic
│       ├── GenerateDailyLogsUseCase.kt  # Creates daily intake logs
│       └── GetDailyIntakesUseCase.kt    # Fetches today's intakes
│
├── di/                             # Dependency Injection
│   ├── AppModule.kt                # Application-wide dependencies
│   └── DatabaseModule.kt           # Database dependencies
│
├── notification/                   # Notification System
│   ├── NotificationHelper.kt       # Creates notifications
│   ├── NotificationScheduler.kt    # Schedules alarms
│   ├── AlarmReceiver.kt            # Handles alarm triggers
│   ├── FollowUpWorker.kt           # Follow-up reminders
│   ├── BootReceiver.kt             # Reschedules after reboot
│   └── DailySchedulerWorker.kt     # Daily log generation
│
├── widget/                         # Home Screen Widget
│   ├── TrackMedWidget.kt           # Widget UI (Glance)
│   └── TrackMedWidgetReceiver.kt   # Widget updates
│
└── ui/                             # UI Layer
    ├── theme/                      # Material 3 theme
    │   ├── Color.kt
    │   ├── Theme.kt
    │   └── Type.kt
    ├── navigation/                 # Jetpack Navigation
    │   ├── NavGraph.kt
    │   └── Routes.kt
    ├── components/                 # Reusable composables
    │   └── TimeWindowIcon.kt
    ├── screens/                    # Feature screens
    │   ├── home/
    │   │   ├── HomeScreen.kt
    │   │   └── HomeViewModel.kt
    │   ├── medicine/
    │   │   ├── MedicineListScreen.kt
    │   │   ├── MedicineListViewModel.kt
    │   │   ├── AddEditMedicineScreen.kt
    │   │   └── AddEditMedicineViewModel.kt
    │   ├── schedule/
    │   │   ├── AddScheduleScreen.kt
    │   │   └── AddScheduleViewModel.kt
    │   ├── settings/
    │   │   ├── SettingsScreen.kt
    │   │   └── SettingsViewModel.kt
    │   └── onboarding/
    │       └── OnboardingScreen.kt
    └── MainActivity.kt
```

## Data Flow

### Creating a Medicine with Schedule

```
User Input → AddEditMedicineScreen
                    ↓
            AddEditMedicineViewModel.save()
                    ↓
            MedicineRepository.insert()
                    ↓
            MedicineDao.insert() → Room Database
                    ↓
            Navigate to AddScheduleScreen
                    ↓
            AddScheduleViewModel.save()
                    ↓
            ScheduleRepository.insert()
                    ↓
            GenerateDailyLogsUseCase.invoke()
                    ↓
            IntakeLogDao.insert() → Creates intake logs
                    ↓
            NotificationScheduler.scheduleAllForDate()
                    ↓
            AlarmManager → Schedules exact alarms
```

### Daily Intake Flow

```
App Launch / DailySchedulerWorker
            ↓
    GenerateDailyLogsUseCase.invoke()
            ↓
    For each active medicine + schedule:
        - Check if log exists for today
        - If not, create IntakeLog with PENDING status
            ↓
    HomeViewModel.loadTodayIntakes()
            ↓
    GetDailyIntakesUseCase.invoke()
            ↓
    Combines Medicine + Schedule + IntakeLog
            ↓
    HomeScreen displays grouped by time window
```

### Notification Flow

```
Scheduled Time Arrives
            ↓
    AlarmReceiver.onReceive()
            ↓
    NotificationHelper.showReminder()
            ↓
    User Action (Take/Skip/Snooze)
            ↓
    NotificationActionReceiver.onReceive()
            ↓
    Update IntakeLog status
    Decrement stock (if taken)
    Update widget
```

## Key Components

### Entities and Their Relationships

```
Medicine (1) ←→ (N) Schedule
    │                   │
    │                   ↓
    └──────→ (N) IntakeLog
```

- **Medicine**: Core entity (name, notes, stock, threshold)
- **Schedule**: Defines when to take (frequency, time window, dosage)
- **IntakeLog**: Daily record (status, timestamp, follow-up count)

### Time Windows

Time windows are defined in `ETimeWindow` enum:

| Window | Default Time |
|--------|--------------|
| MORNING | 08:00 |
| NOON | 12:00 |
| AFTERNOON | 15:00 |
| EVENING | 18:00 |
| NIGHT | 21:00 |

Users can customize these times in Settings.

### Frequency Types

Defined in `EFrequencyType` enum:

| Type | Description |
|------|-------------|
| DAILY | Every day |
| SPECIFIC_DAYS | Selected days of week |
| INTERVAL | Every X days |
| AS_NEEDED | No automatic scheduling |

### Intake Status

Defined in `EIntakeStatus` enum:

| Status | Meaning |
|--------|---------|
| PENDING | Not yet taken |
| TAKEN | Marked as taken |
| SKIPPED | User skipped |
| MISSED | Auto-marked after timeout |

## Adding New Features

### Adding a New Screen

1. **Create the Screen composable** in `ui/screens/yourfeature/`:
   ```kotlin
   @Composable
   fun YourFeatureScreen(
       onNavigateBack: () -> Unit,
       viewModel: YourFeatureViewModel = hiltViewModel()
   ) {
       val uiState by viewModel.uiState.collectAsState()
       // UI implementation
   }
   ```

2. **Create the ViewModel**:
   ```kotlin
   @HiltViewModel
   class YourFeatureViewModel @Inject constructor(
       private val repository: YourRepository
   ) : ViewModel() {
       private val _uiState = MutableStateFlow(YourUiState())
       val uiState: StateFlow<YourUiState> = _uiState.asStateFlow()
   }
   ```

3. **Add route** in `navigation/Routes.kt`:
   ```kotlin
   object YourFeature : Screen("your_feature")
   ```

4. **Add to NavGraph** in `navigation/NavGraph.kt`:
   ```kotlin
   composable(Routes.YourFeature.route) {
       YourFeatureScreen(onNavigateBack = { navController.popBackStack() })
   }
   ```

### Adding a New Database Entity

1. **Create the entity** in `data/local/entity/`:
   ```kotlin
   @Entity(tableName = "your_table")
   data class YourEntity(
       @PrimaryKey(autoGenerate = true) val id: Long = 0,
       val field: String
   )
   ```

2. **Create the DAO** in `data/local/dao/`:
   ```kotlin
   @Dao
   interface YourDao {
       @Query("SELECT * FROM your_table")
       fun getAll(): Flow<List<YourEntity>>

       @Insert
       suspend fun insert(entity: YourEntity): Long
   }
   ```

3. **Add to Database** in `TrackMedDatabase.kt`:
   ```kotlin
   @Database(
       entities = [..., YourEntity::class],
       version = X  // Increment version
   )
   abstract class TrackMedDatabase : RoomDatabase() {
       abstract fun yourDao(): YourDao
   }
   ```

4. **Create Repository** in `data/repository/`:
   ```kotlin
   class YourRepository @Inject constructor(
       private val yourDao: YourDao
   ) {
       fun getAll(): Flow<List<YourEntity>> = yourDao.getAll()
   }
   ```

5. **Provide in DI module** in `di/DatabaseModule.kt`:
   ```kotlin
   @Provides
   fun provideYourDao(database: TrackMedDatabase) = database.yourDao()
   ```

### Adding a New Use Case

Use cases encapsulate business logic that may involve multiple repositories:

```kotlin
class YourUseCase @Inject constructor(
    private val medicineRepository: MedicineRepository,
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(param: String): Result {
        // Business logic here
    }
}
```

## Common Tasks

### Where to Add UI Strings

Add all user-facing strings to `res/values/strings.xml`:

```xml
<string name="your_string_key">Your text here</string>
```

Use in Compose:
```kotlin
Text(stringResource(R.string.your_string_key))
```

### Where to Add Icons

1. Use Material Icons when possible:
   ```kotlin
   Icon(Icons.Default.YourIcon, contentDescription = "...")
   ```

2. For custom icons, add to `res/drawable/` as vector drawables

### Where to Handle Notifications

- **Creating notifications**: `NotificationHelper.kt`
- **Scheduling alarms**: `NotificationScheduler.kt`
- **Handling alarm triggers**: `AlarmReceiver.kt`
- **Notification actions**: `NotificationActionReceiver.kt`

### Where to Update Widget

The widget updates automatically when:
- User takes/skips a medication
- App launches
- Daily logs are generated

To manually trigger update:
```kotlin
TrackMedWidget().update(context, glanceId)
```

## Testing

### Unit Tests Location

```
app/src/test/java/com/trackmed/
├── domain/usecase/           # Use case tests
├── data/repository/          # Repository tests
└── ui/viewmodel/             # ViewModel tests
```

### Test Naming Convention

```kotlin
@Test
fun `methodName_condition_expectedResult`() {
    // Arrange
    // Act
    // Assert
}
```

Example:
```kotlin
@Test
fun `generateLogs_withDailySchedule_createsLogForToday`() {
    // ...
}
```

## Code Style

### Kotlin Conventions

- Use `data class` for models and UI state
- Use `sealed class` or `sealed interface` for restricted hierarchies
- Prefer `Flow` over `LiveData` for reactive streams
- Use `suspend` functions for one-shot operations

### Compose Conventions

- Extract reusable composables to `ui/components/`
- Use `remember` and `derivedStateOf` to optimize recomposition
- Keep composables stateless when possible (state hoisting)
- Use `LaunchedEffect` for side effects

### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Interface | `I` prefix | `IRepository` |
| Enum | `E` prefix | `ETimeWindow` |
| UI State | `*UiState` suffix | `HomeUiState` |
| ViewModel | `*ViewModel` suffix | `HomeViewModel` |
| Use Case | `*UseCase` suffix | `GetDailyIntakesUseCase` |

### File Organization

- One public class per file
- Related private classes can be in the same file
- Extensions in separate `*Extensions.kt` files

## Need Help?

- Check existing code for patterns
- Look at similar features for guidance
- Open an issue for architecture questions
