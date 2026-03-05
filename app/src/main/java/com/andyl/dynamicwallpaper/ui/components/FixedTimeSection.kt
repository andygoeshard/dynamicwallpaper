package com.andyl.dynamicwallpaper.ui.components

import android.app.TimePickerDialog
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.andyl.dynamicwallpaper.ui.viewmodel.DynamicWallpaperViewModel
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar

@Composable
fun FixedTimeSection(
    viewModel: DynamicWallpaperViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Launcher para la imagen del horario fijo
    var pendingTime by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pendingTime?.let { time -> viewModel.setFixedTimeWallpaper(context, time, it.toString()) }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            "Interrupciones por Horario",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Estas fotos se pondrán a la hora exacta, ignorando el clima.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Lista de horarios ya seteados
        state.fixedRules.forEach { (time, uri) ->
            TimeRuleItem(time, uri) {
                // Opción para borrar si querés agregarla después
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedButton(
            onClick = {
                val calendar = Calendar.getInstance()
                TimePickerDialog(context, { _, hour, minute ->
                    pendingTime = String.format("%02d:%02d", hour, minute)
                    launcher.launch(arrayOf("image/*"))
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Programar nueva hora")
        }
    }
}

@Composable
fun TimeRuleItem(
    time: String,
    uri: String,
    onDelete: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Miniatura redonda
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(16.dp))

            Text(
                text = time,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )

            Spacer(Modifier.weight(1f))

        }
    }
}