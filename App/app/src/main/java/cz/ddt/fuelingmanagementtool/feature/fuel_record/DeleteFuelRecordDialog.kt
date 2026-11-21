package cz.ddt.fuelingmanagementtool.feature.fuel_record

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import java.time.LocalDate

@Composable
internal fun DeleteFuelRecordDialog(
    date: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Smazat tankování?") },
        text = {
            Text("Opravdu chceš odstranit tankování ze dne $date? Tuto akci nelze vrátit zpět.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Smazat")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušit")
            }
        }
    )
}