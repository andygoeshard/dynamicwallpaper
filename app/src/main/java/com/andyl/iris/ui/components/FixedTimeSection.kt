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

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pendingTime?.let { time ->
                onEvent(WallpaperEvent.SetFixedTimeWallpaper(context, time, it.toString()))
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.fixed_time_section_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.fixed_time_section_subtitle),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        state.fixedRules.forEach { (time, uri) ->
            TimeRuleItem(
                time = time,
                uri = uri,
                onDelete = { onEvent(WallpaperEvent.OnDeleteFixedTimeRule(time)) },
                onImageClick = {
                    pendingTime = time
                    launcher.launch(arrayOf("image/*"))
                },
                onTimeClick = {
                    // 1. Parseamos el tiempo actual para que el diálogo abra en esa hora
                    val parts = time.split(":")
                    val currentHour = parts[0].toInt()
                    val currentMin = parts[1].toInt()

                    TimePickerDialog(context, { _, hour, minute ->
                        val newTime = String.format("%02d:%02d", hour, minute)
                        if (newTime != time) {
                            // 2. Si cambió, creamos la nueva regla con el mismo URI y borramos la vieja
                            onEvent(WallpaperEvent.SetFixedTimeWallpaper(context, newTime, uri))
                            onEvent(WallpaperEvent.OnDeleteFixedTimeRule(time))
                        }
                    }, currentHour, currentMin, true).show()
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        Button(
            onClick = {
                val calendar = Calendar.getInstance()
                TimePickerDialog(context, { _, hour, minute ->
                    pendingTime = String.format("%02d:%02d", hour, minute)
                    launcher.launch(arrayOf("image/*"))
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                contentColor = MaterialTheme.colorScheme.primary
            ),
            elevation = null
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.acc_add_time_rule), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.btn_schedule_new_time), fontWeight = FontWeight.Bold)
        }
    }
}
