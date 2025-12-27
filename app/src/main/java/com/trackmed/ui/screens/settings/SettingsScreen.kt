package com.trackmed.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trackmed.BuildConfig
import com.trackmed.R
import com.trackmed.data.local.entity.EThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Appearance Section
            SettingsSection(title = stringResource(R.string.settings_appearance)) {
                SettingsThemeSelector(
                    title = stringResource(R.string.settings_theme),
                    subtitle = stringResource(R.string.settings_theme_desc),
                    selectedTheme = uiState.themeMode,
                    onThemeSelected = { viewModel.setThemeMode(it) }
                )
            }

            HorizontalDivider()

            // Notifications Section
            SettingsSection(title = stringResource(R.string.settings_notifications)) {
                SettingsSwitchItem(
                    title = stringResource(R.string.enable_notifications),
                    subtitle = stringResource(R.string.enable_notifications_desc),
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                )

                SettingsDropdownItem(
                    title = stringResource(R.string.follow_up_interval),
                    subtitle = stringResource(R.string.follow_up_interval_desc),
                    selectedValue = "${uiState.followUpIntervalMinutes} min",
                    options = listOf(15, 30, 45, 60),
                    onOptionSelected = { viewModel.setFollowUpInterval(it) },
                    optionLabel = { "$it min" }
                )

                SettingsSliderItem(
                    title = stringResource(R.string.max_follow_ups),
                    subtitle = stringResource(R.string.max_follow_ups_desc),
                    value = uiState.maxFollowUps.toFloat(),
                    onValueChange = { viewModel.setMaxFollowUps(it.toInt()) },
                    valueRange = 1f..5f,
                    steps = 3
                )
            }

            HorizontalDivider()

            // Vacation Mode Section
            SettingsSection(title = stringResource(R.string.settings_vacation)) {
                SettingsSwitchItem(
                    title = stringResource(R.string.vacation_mode),
                    subtitle = stringResource(R.string.vacation_mode_desc),
                    checked = uiState.vacationModeActive,
                    onCheckedChange = { viewModel.setVacationMode(it) }
                )
            }

            HorizontalDivider()

            // Data Section
            SettingsSection(title = stringResource(R.string.settings_data)) {
                SettingsButtonItem(
                    title = stringResource(R.string.export_backup),
                    subtitle = stringResource(R.string.export_backup_desc),
                    onClick = { viewModel.exportBackup(context) }
                )

                SettingsButtonItem(
                    title = stringResource(R.string.import_backup),
                    subtitle = stringResource(R.string.import_backup_desc),
                    onClick = { viewModel.importBackup(context) }
                )
            }

            HorizontalDivider()

            // About Section
            SettingsSection(title = stringResource(R.string.settings_about)) {
                SettingsInfoItem(
                    title = stringResource(R.string.version),
                    value = BuildConfig.VERSION_NAME
                )
            }

            // Message snackbar
            uiState.message?.let { message ->
                LaunchedEffect(message) {
                    // Show snackbar
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun <T> SettingsDropdownItem(
    title: String,
    subtitle: String,
    selectedValue: String,
    options: List<T>,
    onOptionSelected: (T) -> Unit,
    optionLabel: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(selectedValue)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(optionLabel(option)) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSliderItem(
    title: String,
    subtitle: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value.toInt().toString(),
                style = MaterialTheme.typography.titleMedium
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}

@Composable
private fun SettingsButtonItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedButton(onClick = onClick) {
            Text(title)
        }
    }
}

@Composable
private fun SettingsInfoItem(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsThemeSelector(
    title: String,
    subtitle: String,
    selectedTheme: EThemeMode,
    onThemeSelected: (EThemeMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            EThemeMode.entries.forEachIndexed { index, themeMode ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = EThemeMode.entries.size
                    ),
                    onClick = { onThemeSelected(themeMode) },
                    selected = selectedTheme == themeMode
                ) {
                    Text(
                        text = when (themeMode) {
                            EThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                            EThemeMode.LIGHT -> stringResource(R.string.theme_light)
                            EThemeMode.DARK -> stringResource(R.string.theme_dark)
                        }
                    )
                }
            }
        }
    }
}
