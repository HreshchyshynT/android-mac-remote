package com.threshchyshyn.androidmacremote.remote

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.threshchyshyn.androidmacremote.discover.model.ScannedBleDevice
import timber.log.Timber

data class TogglePlaybackScreenArgs(
    val scannedBleDevice: ScannedBleDevice,
)

@Destination(navArgsDelegate = TogglePlaybackScreenArgs::class)
@SuppressLint("MissingPermission")
@Composable
internal fun TogglePlaybackScreen(
    args: TogglePlaybackScreenArgs,
) {
    val context = LocalContext.current
    val gattCallback = remember { TogglePlaybackGattCallback() }
    DisposableEffect(Unit) {
        args.scannedBleDevice.device.connectGatt(
            context,
            true,
            gattCallback,
            BluetoothDevice.TRANSPORT_LE,
        )
        onDispose {
            Timber.d("on dispose")
            gattCallback.disconnect()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 8.dp, horizontal = 16.dp),
    ) {
        Text(
            text = "Connect to Device: ${args.scannedBleDevice.name}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Text(
            text = "MAC Address: ${args.scannedBleDevice.address}",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        )

        Text(
            text = when (gattCallback.connectionState) {
                ConnectionState.Connected -> "Connection state: Connected"
                ConnectionState.Disconnected -> "Connection state: Disconnected"
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        Text(
            text = when (gattCallback.isServiceDiscovered) {
                true -> "Service discovered"
                false -> "Service not discovered"
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        Text(
            text = when (gattCallback.isWriteSuccess) {
                true -> "Last write successful"
                false -> "Last write unsuccessful"
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        if (gattCallback.connectionState == ConnectionState.Connected) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Icon(
                    Icons.Filled.ThumbUp,
                    contentDescription = "toggle play button",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .border(2.dp, color = Color.Black, CircleShape)
                        .clickable { gattCallback.togglePlaying() }
                        .padding(16.dp)
                        .size(56.dp),
                )
            }
        }
    }
}
