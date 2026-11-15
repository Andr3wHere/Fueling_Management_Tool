
package cz.ddt.fuelingmanagementtool.feature.fuel_record

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.ddt.fuelingmanagementtool.ui.theme.FuelingManagementToolTheme
import androidx.compose.material.icons.filled.Delete

data class FuelRecordUi(
    val id: Int,
    val date: String,
    val liters: String,
    val price: String
)

@Composable
fun FuelRecordListScreen(
    records: List<FuelRecordUi> = emptyList(),
    onAddRecord: () -> Unit = {},
    onDeleteRecord: (Int) -> Unit = {}
) {
    var recordToDelete by remember {
        mutableStateOf<FuelRecordUi?>(null)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRecord,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Moje tankování",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Zatím žádná tankování",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(
                        items = records,
                        key = { it.id }
                    ) { record ->
                        FuelRecordCard(
                            record = record,
                            onDelete = {
                                recordToDelete = record
                            }
                        )
                    }
                }
            }
        }
    }

    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = {
                recordToDelete = null
            },
            title = {
                Text("Smazat tankování?")
            },
            text = {
                Text(
                    "Opravdu chceš odstranit tankování ze dne ${record.date}? Tuto akci nelze vrátit zpět."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteRecord(record.id)
                        recordToDelete = null
                    }
                ) {
                    Text("Smazat")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        recordToDelete = null
                    }
                ) {
                    Text("Zrušit")
                }
            }
        )
    }
}

@Composable
private fun FuelRecordCard(
    record: FuelRecordUi,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.date,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Smazat tankování",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = record.liters,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = record.price,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private val previewRecords = listOf(
    FuelRecordUi(1, "15. 10. 2026", "42,5 l", "1 520 Kč"),
    FuelRecordUi(2, "8. 10. 2026", "35,2 l", "1 267 Kč"),
    FuelRecordUi(3, "1. 10. 2026", "28,4 l", "1 022 Kč")
)

@Preview(
    name = "Historie tankování – světlý režim",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
private fun FuelRecordListLightPreview() {
    FuelingManagementToolTheme(
        darkTheme = false,
        dynamicColor = false
    ) {
        FuelRecordListScreen(records = previewRecords)
    }
}

@Preview(
    name = "Historie tankování – tmavý režim",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FuelRecordListDarkPreview() {
    FuelingManagementToolTheme(
        darkTheme = true,
        dynamicColor = false
    ) {
        FuelRecordListScreen(records = previewRecords)
    }
}

@Preview(
    name = "Prázdná historie tankování",
    showBackground = true
)
@Composable
private fun FuelRecordListEmptyPreview() {
    FuelingManagementToolTheme(dynamicColor = false) {
        FuelRecordListScreen()
    }
}
