package com.trackmed.ui.screens.medicines

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trackmed.R
import com.trackmed.data.local.entity.EFrequencyType
import com.trackmed.data.local.entity.ETimeWindow
import com.trackmed.data.local.entity.Schedule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicineScreen(
    medicineId: Long?,
    onNavigateBack: () -> Unit,
    onAddSchedule: (Long) -> Unit,
    viewModel: AddEditMedicineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val schedules by viewModel.schedulesFlow.collectAsState()

    LaunchedEffect(medicineId) {
        if (medicineId != null) {
            viewModel.loadMedicine(medicineId)
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            if (uiState.isNewMedicine && uiState.savedMedicineId != null) {
                onAddSchedule(uiState.savedMedicineId!!)
            } else {
                onNavigateBack()
            }
        }
    }

    // Delete schedule confirmation dialog
    uiState.scheduleToDelete?.let { schedule ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteSchedule() },
            title = { Text(stringResource(R.string.delete_schedule_title)) },
            text = { Text(stringResource(R.string.delete_schedule_message)) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDeleteSchedule() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeleteSchedule() }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (medicineId == null) stringResource(R.string.add_medicine)
                        else stringResource(R.string.edit_medicine)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name field
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text(stringResource(R.string.medicine_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } }
            )

            // Notes field
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text(stringResource(R.string.notes_hint)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                supportingText = { Text(stringResource(R.string.notes_helper)) }
            )

            HorizontalDivider()

            // Current stock
            Text(
                text = stringResource(R.string.current_stock),
                style = MaterialTheme.typography.titleMedium
            )
            NumberPicker(
                value = uiState.currentStock,
                onValueChange = { viewModel.updateCurrentStock(it) },
                minValue = 0
            )

            // Restock threshold
            Text(
                text = stringResource(R.string.restock_threshold),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.restock_threshold_helper),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            NumberPicker(
                value = uiState.restockThreshold,
                onValueChange = { viewModel.updateRestockThreshold(it) },
                minValue = 0
            )

            // Pills per dose
            Text(
                text = stringResource(R.string.pills_per_dose),
                style = MaterialTheme.typography.titleMedium
            )
            NumberPicker(
                value = uiState.pillsPerDose,
                onValueChange = { viewModel.updatePillsPerDose(it) },
                minValue = 1
            )

            // Schedules section (only in edit mode)
            if (medicineId != null) {
                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.schedules_section_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(
                        onClick = { onAddSchedule(medicineId) }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.schedule_add))
                    }
                }

                if (schedules.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_schedules_yet),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        schedules.forEach { schedule ->
                            ScheduleItem(
                                schedule = schedule,
                                onDelete = { viewModel.requestDeleteSchedule(schedule) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.save))
                }
            }

            // Delete button (only for edit mode)
            if (medicineId != null) {
                OutlinedButton(
                    onClick = { viewModel.delete() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            }
        }
    }
}

@Composable
private fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int = 0,
    maxValue: Int = 999
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilledIconButton(
            onClick = { if (value > minValue) onValueChange(value - 1) },
            enabled = value > minValue
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease")
        }

        OutlinedTextField(
            value = value.toString(),
            onValueChange = { newValue ->
                newValue.toIntOrNull()?.let { num ->
                    if (num in minValue..maxValue) {
                        onValueChange(num)
                    }
                }
            },
            modifier = Modifier.width(100.dp),
            textStyle = MaterialTheme.typography.titleLarge,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        FilledIconButton(
            onClick = { if (value < maxValue) onValueChange(value + 1) },
            enabled = value < maxValue
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase")
        }
    }
}

@Composable
private fun ScheduleItem(
    schedule: Schedule,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getTimeWindowDisplayName(schedule.timeWindow),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = buildScheduleDescription(schedule),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun getTimeWindowDisplayName(timeWindow: ETimeWindow): String {
    return when (timeWindow) {
        ETimeWindow.MORNING -> stringResource(R.string.time_window_morning)
        ETimeWindow.NOON -> stringResource(R.string.time_window_noon)
        ETimeWindow.AFTERNOON -> stringResource(R.string.time_window_afternoon)
        ETimeWindow.EVENING -> stringResource(R.string.time_window_evening)
        ETimeWindow.NIGHT -> stringResource(R.string.time_window_night)
    }
}

@Composable
private fun buildScheduleDescription(schedule: Schedule): String {
    val frequencyText = when (schedule.frequencyType) {
        EFrequencyType.DAILY -> stringResource(R.string.frequency_daily)
        EFrequencyType.SPECIFIC_DAYS -> {
            val days = schedule.daysOfWeek?.split(",")?.size ?: 0
            stringResource(R.string.schedule_days_count, days)
        }
        EFrequencyType.INTERVAL -> {
            stringResource(R.string.schedule_interval_description, schedule.intervalDays ?: 1)
        }
        EFrequencyType.AS_NEEDED -> stringResource(R.string.frequency_as_needed)
    }

    val dosageText = stringResource(R.string.schedule_dosage_description, schedule.dosageAmount)

    return "$frequencyText - $dosageText"
}
