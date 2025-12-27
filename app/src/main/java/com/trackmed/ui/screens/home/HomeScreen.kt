package com.trackmed.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trackmed.R
import com.trackmed.data.local.entity.EIntakeStatus
import com.trackmed.data.local.entity.ETimeWindow
import com.trackmed.domain.model.DailyIntakeItem
import com.trackmed.ui.components.TimeWindowIcon
import com.trackmed.ui.theme.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddMedicine: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.home_title),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = uiState.currentDate.format(
                                DateTimeFormatter.ofPattern("EEEE, MMMM d")
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Vacation mode toggle could go here
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMedicine) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.medicines_add))
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.totalCount == 0) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onAddMedicine = onAddMedicine
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Progress header
                item {
                    ProgressHeader(
                        completed = uiState.completedCount,
                        total = uiState.totalCount
                    )
                }

                // Morning section
                if (uiState.morningIntakes.isNotEmpty()) {
                    item {
                        TimeWindowHeader(timeWindow = ETimeWindow.MORNING)
                    }
                    items(uiState.morningIntakes, key = { it.logId }) { intake ->
                        IntakeCard(
                            intake = intake,
                            onTake = { viewModel.markAsTaken(intake.logId) },
                            onSkip = { viewModel.markAsSkipped(intake.logId) },
                            onReset = { viewModel.resetIntake(intake.logId) }
                        )
                    }
                }

                // Noon section
                if (uiState.noonIntakes.isNotEmpty()) {
                    item {
                        TimeWindowHeader(timeWindow = ETimeWindow.NOON)
                    }
                    items(uiState.noonIntakes, key = { it.logId }) { intake ->
                        IntakeCard(
                            intake = intake,
                            onTake = { viewModel.markAsTaken(intake.logId) },
                            onSkip = { viewModel.markAsSkipped(intake.logId) },
                            onReset = { viewModel.resetIntake(intake.logId) }
                        )
                    }
                }

                // Afternoon section
                if (uiState.afternoonIntakes.isNotEmpty()) {
                    item {
                        TimeWindowHeader(timeWindow = ETimeWindow.AFTERNOON)
                    }
                    items(uiState.afternoonIntakes, key = { it.logId }) { intake ->
                        IntakeCard(
                            intake = intake,
                            onTake = { viewModel.markAsTaken(intake.logId) },
                            onSkip = { viewModel.markAsSkipped(intake.logId) },
                            onReset = { viewModel.resetIntake(intake.logId) }
                        )
                    }
                }

                // Evening section
                if (uiState.eveningIntakes.isNotEmpty()) {
                    item {
                        TimeWindowHeader(timeWindow = ETimeWindow.EVENING)
                    }
                    items(uiState.eveningIntakes, key = { it.logId }) { intake ->
                        IntakeCard(
                            intake = intake,
                            onTake = { viewModel.markAsTaken(intake.logId) },
                            onSkip = { viewModel.markAsSkipped(intake.logId) },
                            onReset = { viewModel.resetIntake(intake.logId) }
                        )
                    }
                }

                // Night section
                if (uiState.nightIntakes.isNotEmpty()) {
                    item {
                        TimeWindowHeader(timeWindow = ETimeWindow.NIGHT)
                    }
                    items(uiState.nightIntakes, key = { it.logId }) { intake ->
                        IntakeCard(
                            intake = intake,
                            onTake = { viewModel.markAsTaken(intake.logId) },
                            onSkip = { viewModel.markAsSkipped(intake.logId) },
                            onReset = { viewModel.resetIntake(intake.logId) }
                        )
                    }
                }

                // Bottom spacing for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun ProgressHeader(completed: Int, total: Int) {
    val progress = if (total > 0) completed.toFloat() / total else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_progress),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$completed / $total",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
private fun TimeWindowHeader(timeWindow: ETimeWindow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeWindowIcon(
            timeWindow = timeWindow,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = when (timeWindow) {
                ETimeWindow.MORNING -> stringResource(R.string.time_window_morning)
                ETimeWindow.NOON -> stringResource(R.string.time_window_noon)
                ETimeWindow.AFTERNOON -> stringResource(R.string.time_window_afternoon)
                ETimeWindow.EVENING -> stringResource(R.string.time_window_evening)
                ETimeWindow.NIGHT -> stringResource(R.string.time_window_night)
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun IntakeCard(
    intake: DailyIntakeItem,
    onTake: () -> Unit,
    onSkip: () -> Unit,
    onReset: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (intake.status) {
            EIntakeStatus.TAKEN -> StatusTaken.copy(alpha = 0.1f)
            EIntakeStatus.SKIPPED -> StatusSkipped.copy(alpha = 0.1f)
            EIntakeStatus.MISSED -> StatusMissed.copy(alpha = 0.1f)
            EIntakeStatus.PENDING -> MaterialTheme.colorScheme.surface
        },
        label = "background"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            StatusIndicator(status = intake.status)

            Spacer(modifier = Modifier.width(16.dp))

            // Medicine info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = intake.medicineName,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (intake.status == EIntakeStatus.TAKEN) {
                        TextDecoration.LineThrough
                    } else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        R.string.dosage_pills,
                        intake.dosageAmount,
                        stringResource(if (intake.dosageAmount > 1) R.string.pill_plural else R.string.pill_singular)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                intake.notes?.let { notes ->
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (intake.isLowStock) {
                    Text(
                        text = "${intake.currentStock} left (low stock)",
                        style = MaterialTheme.typography.bodySmall,
                        color = StockLow
                    )
                }
            }

            // Actions
            when (intake.status) {
                EIntakeStatus.PENDING -> {
                    Row {
                        IconButton(onClick = onSkip) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.action_skip),
                                tint = StatusSkipped
                            )
                        }
                        IconButton(onClick = onTake) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.action_take),
                                tint = StatusTaken
                            )
                        }
                    }
                }
                EIntakeStatus.TAKEN, EIntakeStatus.SKIPPED -> {
                    IconButton(onClick = onReset) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.action_reset),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun StatusIndicator(status: EIntakeStatus) {
    val color = when (status) {
        EIntakeStatus.TAKEN -> StatusTaken
        EIntakeStatus.SKIPPED -> StatusSkipped
        EIntakeStatus.MISSED -> StatusMissed
        EIntakeStatus.PENDING -> StatusPending
    }

    Box(
        modifier = Modifier
            .size(12.dp)
            .background(color = color, shape = MaterialTheme.shapes.small)
    )
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    onAddMedicine: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.home_empty),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.home_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddMedicine) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.medicines_add))
        }
    }
}
