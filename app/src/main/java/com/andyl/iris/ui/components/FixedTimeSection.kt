package com.andyl.iris.ui.components

import android.app.TimePickerDialog
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andyl.iris.R
import com.andyl.iris.ui.event.WallpaperEvent
import com.andyl.iris.ui.state.DynamicWallpaperUiState
import java.util.Calendar

@Composable
fun FixedTimeSection(
    state: DynamicWallpaperUiState,
    onEvent: (WallpaperEvent) -> Unit
) {
    val context = LocalContext.current
    var pendingTime by remember { mutableStateOf<String?>(null) }
    var pendingUri by remember { mutableStateOf<String?>(null) }
    var showTargetDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pendingUri = it.toString()
            showTargetDialog = true // Después de la imagen, preguntamos target
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = stringResource(R.string.fixed_time_section_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        Text(text = stringResource(R.string.fixed_time_section_subtitle), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))

        // Renderizamos las reglas
        state.fixedRules.forEach { (timeKey, uri) ->
            val parts = timeKey.split("-")
            val displayTime = parts[0]
            val target = if (parts.size > 1) parts[1].toInt() else 3

            TimeRuleItem(
                time = displayTime,
                uri = uri,
                target = target,
                onDelete = { onEvent(WallpaperEvent.OnDeleteFixedTimeRule(context, timeKey)) },
                onImageClick = {
                    pendingTime = displayTime
                    launcher.launch(arrayOf("image/*"))
                },
                onTimeClick = {
                    val tParts = displayTime.split(":")
                    TimePickerDialog(context, { _, hour, minute ->
                        val newTime = String.format("%02d:%02d", hour, minute)
                        if (newTime != displayTime) {
                            // Si cambia la hora, movemos la regla completa (con su target)
                            val newFullKey = if (target == 3) newTime else "$newTime-$target"
                            onEvent(WallpaperEvent.SetFixedTimeWallpaper(context, newFullKey, uri))
                            onEvent(WallpaperEvent.OnDeleteFixedTimeRule(context, timeKey))
                        }
                    }, tParts[0].toInt(), tParts[1].toInt(), true).show()
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Botón para nueva regla
        Button(
            onClick = {
                val calendar = Calendar.getInstance()
                TimePickerDialog(context, { _, hour, minute ->
                    pendingTime = String.format("%02d:%02d", hour, minute)
                    launcher.launch(arrayOf("image/*"))
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(0.15f), contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.btn_schedule_new_time), fontWeight = FontWeight.Bold)
        }
    }

    if (showTargetDialog) {
        WallpaperTargetDialog(
            onDismiss = { showTargetDialog = false },
            onConfirm = { target ->
                if (pendingTime != null && pendingUri != null) {
                    val finalKey = if (target == 3) pendingTime!! else "${pendingTime}-$target"
                    onEvent(WallpaperEvent.SetFixedTimeWallpaper(context, finalKey, pendingUri!!))
                }
                showTargetDialog = false
            }
        )
    }
}