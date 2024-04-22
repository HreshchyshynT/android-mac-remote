package com.threshchyshyn.androidmacremote.discover

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp


internal class DiscoverBLEState(
    val bluetoothAdapter: BluetoothAdapter,
)

@Composable
internal fun DiscoverBLEScreen(
    bluetoothAdapter: BluetoothAdapter,
) {
    val state = remember { DiscoverBLEState(bluetoothAdapter) }
    val isBluetoothEnabled = isBluetoothEnabled(bluetoothAdapter.isEnabled)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            "Is bluetooth enabled: $isBluetoothEnabled",
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun isBluetoothEnabled(initialState: Boolean): Boolean {
    val isEnabledState = remember { mutableStateOf(initialState) }

    val context = LocalContext.current
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null || !intent.hasExtra(BluetoothAdapter.EXTRA_STATE)) return
                isEnabledState.value = intent.isBluetoothEnabled
            }
        }
        context.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    return isEnabledState.value
}

private val Intent.isBluetoothEnabled
    get() = getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF) == BluetoothAdapter.STATE_ON


