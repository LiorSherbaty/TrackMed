# TrackMed

Your personal medication, vitamin, and supplement tracking companion for Android.

Never miss a dose again. TrackMed helps you stay on top of your health routine with smart reminders, flexible scheduling, and stock management.

## Download

*Coming soon to Google Play Store*

## Features

### For Daily Use
- **Smart Reminders** - Get notified at the right time with Take, Skip, and Snooze options
- **Home Screen Widget** - See pending medications at a glance
- **Progress Tracking** - Visual progress indicator shows your daily completion
- **Vacation Mode** - Pause all notifications when you need a break

### Medication Management
- **Flexible Schedules** - Daily, specific days of the week, every X days, or as-needed
- **Time Windows** - Morning, Noon, Afternoon, Evening, Night with customizable times
- **Multiple Schedules** - Add multiple schedules per medication (e.g., morning and evening doses)
- **Stock Tracking** - Monitor your supply and get low-stock alerts before you run out

### Privacy First
- **100% Offline** - All data stays on your device
- **No Account Required** - Start tracking immediately
- **No Data Collection** - We don't collect any personal information
- **Backup & Restore** - Export your data when you need it

## Requirements

- Android 8.0 (API 26) or higher
- Notification permission for reminders
- Alarm permission for precise timing (optional but recommended)

## Building from Source

### Prerequisites

- Android Studio Ladybug (2024.2.1) or later
- JDK 17
- Android SDK 35

### Build Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/trackmed.git
   cd trackmed
   ```

2. Open in Android Studio

3. Sync Gradle files

4. Run on device or emulator (API 26+)

### Build Variants

- **debug** - Development build with debugging enabled
- **release** - Optimized build for distribution

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 2.1 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| Database | Room with KSP |
| DI | Hilt |
| Background Work | WorkManager + AlarmManager |
| Widget | Jetpack Glance |

## Project Structure

```
app/src/main/java/com/trackmed/
├── data/
│   ├── local/          # Room database, DAOs, entities
│   └── repository/     # Data access layer
├── domain/
│   ├── model/          # Business models
│   └── usecase/        # Business logic
├── di/                 # Dependency injection
├── notification/       # Alarm & notification system
├── widget/             # Home screen widget
└── ui/
    ├── theme/          # Material 3 theming
    ├── navigation/     # App navigation
    ├── components/     # Reusable UI components
    └── screens/        # Feature screens
```

## Permissions

| Permission | Purpose |
|------------|---------|
| `POST_NOTIFICATIONS` | Send medication reminders |
| `SCHEDULE_EXACT_ALARM` | Precise reminder timing |
| `RECEIVE_BOOT_COMPLETED` | Restore alarms after device restart |
| `VIBRATE` | Vibrate for notifications |
| `WAKE_LOCK` | Ensure alarms trigger reliably |

## Contributing

Contributions are welcome! Please read the [Developer Guide](DEVELOPER.md) for architecture details and coding standards.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Material Design Icons](https://fonts.google.com/icons)
- [Jetpack Libraries](https://developer.android.com/jetpack) by Google
- The open source Android community

## Support

If you encounter any issues or have feature requests, please [open an issue](https://github.com/yourusername/trackmed/issues).
