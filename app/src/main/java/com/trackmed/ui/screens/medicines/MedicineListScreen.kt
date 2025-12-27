package com.trackmed.ui.screens.medicines

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trackmed.R
import com.trackmed.domain.model.MedicineWithSchedules
import com.trackmed.ui.theme.StockGood
import com.trackmed.ui.theme.StockLow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineListScreen(
    onAddMedicine: () -> Unit,
    onEditMedicine: (Long) -> Unit,
    viewModel: MedicineListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.medicines_title)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMedicine) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_medicine))
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
        } else if (uiState.medicines.isEmpty()) {
            EmptyMedicineList(
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
                items(uiState.medicines, key = { it.medicine.id }) { medicineWithSchedules ->
                    MedicineCard(
                        medicineWithSchedules = medicineWithSchedules,
                        daysUntilEmpty = uiState.daysUntilEmpty[medicineWithSchedules.medicine.id],
                        onClick = { onEditMedicine(medicineWithSchedules.medicine.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicineCard(
    medicineWithSchedules: MedicineWithSchedules,
    daysUntilEmpty: Int?,
    onClick: () -> Unit
) {
    val medicine = medicineWithSchedules.medicine
    val isLowStock = medicine.currentStock <= medicine.restockThreshold

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = medicine.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (medicine.isPaused) {
                        Spacer(modifier = Modifier.width(8.dp))
                        SuggestionChip(
                            onClick = {},
                            label = { Text(stringResource(R.string.paused)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.stock_count, medicine.currentStock),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isLowStock) StockLow else StockGood
                    )

                    if (isLowStock) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = StockLow
                        )
                    }

                    daysUntilEmpty?.let { days ->
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.days_until_empty, days),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (medicineWithSchedules.schedules.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.schedules_count,
                            medicineWithSchedules.schedules.size
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMedicineList(
    modifier: Modifier = Modifier,
    onAddMedicine: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.no_medicines_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.no_medicines_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddMedicine) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.add_medicine))
        }
    }
}
