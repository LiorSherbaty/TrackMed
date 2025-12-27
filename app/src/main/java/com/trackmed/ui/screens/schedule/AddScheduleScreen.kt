package com.trackmed.ui.screens.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trackmed.R
import com.trackmed.data.local.entity.EFrequencyType
import com.trackmed.data.local.entity.ETimeWindow
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleScreen(
    medicineId: Long,
    onNavigateBack: () -> Unit,
    viewModel: AddScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(medicineId) {
        viewModel.setMedicineId(medicineId)
    }

    LaunchedEffect(uiState.navigateBack) {
        if (uiState.navigateBack) {
            onNavigateBack()
        }
    }

    // Success dialog
    if (uiState.showSaveSuccessDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialog() },
            title = { Text(stringResource(R.string.success_schedule_saved)) },
            text = { Text(stringResource(R.string.schedule_add_another_prompt)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onAddAnotherSchedule() }) {
                    Text(stringResource(R.string.schedule_add_another))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDone() }) {
                    Text(stringResource(R.string.action_done))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.schedule_add)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Frequency selection
            Text(
                text = stringResource(R.string.schedule_frequency),
                style = MaterialTheme.typography.titleMedium
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.frequencyType == EFrequencyType.DAILY,
                        onClick = { viewModel.updateFrequencyType(EFrequencyType.DAILY) },
                        label = { Text(stringResource(R.string.frequency_daily)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.frequencyType == EFrequencyType.SPECIFIC_DAYS,
                        onClick = { viewModel.updateFrequencyType(EFrequencyType.SPECIFIC_DAYS) },
                        label = { Text(stringResource(R.string.frequency_specific_days)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.frequencyType == EFrequencyType.INTERVAL,
                        onClick = { viewModel.updateFrequencyType(EFrequencyType.INTERVAL) },
                        label = { Text(stringResource(R.string.frequency_interval)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.frequencyType == EFrequencyType.AS_NEEDED,
                        onClick = { viewModel.updateFrequencyType(EFrequencyType.AS_NEEDED) },
                        label = { Text(stringResource(R.string.frequency_as_needed)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Specific days selection
            if (uiState.frequencyType == EFrequencyType.SPECIFIC_DAYS) {
                Text(
                    text = stringResource(R.string.schedule_days),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DayOfWeek.entries.forEach { day ->
                        FilterChip(
                            selected = uiState.selectedDays.contains(day.value),
                            onClick = { viewModel.toggleDay(day.value) },
                            label = {
                                Text(day.getDisplayName(TextStyle.NARROW, Locale.getDefault()))
                            }
                        )
                    }
                }
            }

            // Interval selection
            if (uiState.frequencyType == EFrequencyType.INTERVAL) {
                Text(
                    text = stringResource(R.string.schedule_every_x_days),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledIconButton(
                        onClick = { viewModel.updateIntervalDays(uiState.intervalDays - 1) },
                        enabled = uiState.intervalDays > 1
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }

                    Text(
                        text = "${uiState.intervalDays}",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    FilledIconButton(
                        onClick = { viewModel.updateIntervalDays(uiState.intervalDays + 1) }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }

                    Text(
                        text = stringResource(R.string.schedule_days_label),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            HorizontalDivider()

            // Time window selection (not shown for AS_NEEDED)
            if (uiState.frequencyType != EFrequencyType.AS_NEEDED) {
                Text(
                    text = stringResource(R.string.schedule_time_window),
                    style = MaterialTheme.typography.titleMedium
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.timeWindow == ETimeWindow.MORNING,
                            onClick = { viewModel.updateTimeWindow(ETimeWindow.MORNING) },
                            label = { Text(stringResource(R.string.time_window_morning)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uiState.timeWindow == ETimeWindow.NOON,
                            onClick = { viewModel.updateTimeWindow(ETimeWindow.NOON) },
                            label = { Text(stringResource(R.string.time_window_noon)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uiState.timeWindow == ETimeWindow.AFTERNOON,
                            onClick = { viewModel.updateTimeWindow(ETimeWindow.AFTERNOON) },
                            label = { Text(stringResource(R.string.time_window_afternoon)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.timeWindow == ETimeWindow.EVENING,
                            onClick = { viewModel.updateTimeWindow(ETimeWindow.EVENING) },
                            label = { Text(stringResource(R.string.time_window_evening)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uiState.timeWindow == ETimeWindow.NIGHT,
                            onClick = { viewModel.updateTimeWindow(ETimeWindow.NIGHT) },
                            label = { Text(stringResource(R.string.time_window_night)) },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // Custom time picker
                var showTimePicker by remember { mutableStateOf(false) }
                val timePickerState = rememberTimePickerState(
                    initialHour = uiState.customTimeHour,
                    initialMinute = uiState.customTimeMinute
                )

                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Specific time: ${String.format("%02d:%02d", uiState.customTimeHour, uiState.customTimeMinute)}"
                    )
                }

                if (showTimePicker) {
                    AlertDialog(
                        onDismissRequest = { showTimePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.updateCustomTime(
                                        timePickerState.hour,
                                        timePickerState.minute
                                    )
                                    showTimePicker = false
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTimePicker = false }) {
                                Text(stringResource(R.string.action_cancel))
                            }
                        },
                        text = {
                            TimePicker(state = timePickerState)
                        }
                    )
                }

                HorizontalDivider()
            }

            // Dosage
            Text(
                text = stringResource(R.string.schedule_dosage),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledIconButton(
                    onClick = {
                        if (uiState.dosageAmount > 1) {
                            viewModel.updateDosageAmount(uiState.dosageAmount - 1)
                        }
                    },
                    enabled = uiState.dosageAmount > 1
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }

                Text(
                    text = "${uiState.dosageAmount}",
                    style = MaterialTheme.typography.headlineMedium
                )

                FilledIconButton(
                    onClick = { viewModel.updateDosageAmount(uiState.dosageAmount + 1) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }

                Text(
                    text = if (uiState.dosageAmount == 1) "pill" else "pills",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving &&
                    (uiState.frequencyType != EFrequencyType.SPECIFIC_DAYS || uiState.selectedDays.isNotEmpty())
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.action_save))
                }
            }
        }
    }
}
