package com.andyl.iris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import com.andyl.iris.R

@Composable
fun DayImageCard(
    dayName: String,
    imageUri: String?,
    isToday: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(85.dp)
    ) {
        Box {
            Card(
                onClick = onClick,
                modifier = Modifier
                    .size(80.dp)
                    .then(
                        if (isToday) Modifier.border(
                            2.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            RoundedCornerShape(20.dp)
                        )
                        else Modifier
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                )
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUri)
                                .crossfade(200)
                                .size(160, 160)
                                .precision(Precision.EXACT)
                                .bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            if (imageUri != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                        .padding(4.dp)
                        .background(
                            Color.Gray.copy(alpha = 0.9f),
                            androidx.compose.foundation.shape.CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.btn_delete),
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isToday) stringResource(R.string.today) else dayName.take(3).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}