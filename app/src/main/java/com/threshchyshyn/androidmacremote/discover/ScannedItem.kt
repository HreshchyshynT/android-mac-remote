package com.threshchyshyn.androidmacremote.discover

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.threshchyshyn.androidmacremote.discover.model.ScannedBleDevice

@Composable
internal fun ScannedItem(
    scannedBleDevice: ScannedBleDevice,
    modifier: Modifier = Modifier,
) {
    val isExpanded = remember(scannedBleDevice.address) {
        mutableStateOf(false)
    }
    Column(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = scannedBleDevice.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = scannedBleDevice.address,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                text = "RSSI: ${scannedBleDevice.rssi}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Row(Modifier.fillMaxWidth()) {
            HorizontalDivider()
            Icon(
                Icons.Outlined.ArrowDropDown,
                "toggle metadata visibility",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        rotationZ = 90f
                    }
            )
        }
    }
}
