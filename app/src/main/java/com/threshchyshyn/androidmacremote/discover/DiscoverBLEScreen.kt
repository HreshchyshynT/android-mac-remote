package com.threshchyshyn.androidmacremote.discover

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.threshchyshyn.androidmacremote.discover.model.ScannedBleDevice
import com.threshchyshyn.androidmacremote.discover.model.toMetadataList
import com.threshchyshyn.androidmacremote.discover.model.toScannedBleDevice
import timber.log.Timber


internal class DiscoverBLEState(
    val bluetoothAdapter: BluetoothAdapter,
)

@OptIn(ExperimentalPermissionsApi::class)
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
        val locationPermission = rememberMultiplePermissionsState(
            listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            Timber.d("Location permission callback: $it")
            if (it.any { (_, isGranted) -> isGranted }) {

            }
        }

        val bluetoothPermission = rememberMultiplePermissionsState(
            listOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        ) {
            Timber.d("Bluetooth permission callback: $it")
            if (it.any { (_, isGranted) -> isGranted }) {

            }
        }

        val isLocationGranted = locationPermission.allPermissionsGranted
        val isBluetoothPermissionGranted = bluetoothPermission.allPermissionsGranted
        Text(
            when (isLocationGranted) {
                true -> "Location permission granted"
                false -> "Location permission denied, click to grant permissions"
            },
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .then(when (isLocationGranted) {
                    true -> Modifier
                    false -> Modifier.clickable { locationPermission.launchMultiplePermissionRequest() }
                })
                .padding(vertical = 4.dp),
        )
        Text(
            when (isBluetoothPermissionGranted) {
                true -> "Bluetooth permission granted"
                false -> "Bluetooth permission denied, click to grant permissions"
            },
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .then(when (isBluetoothPermissionGranted) {
                    true -> Modifier
                    false -> Modifier.clickable { bluetoothPermission.launchMultiplePermissionRequest() }
                })
                .padding(vertical = 4.dp),
        )
        HorizontalDivider(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        if (isBluetoothEnabled && isLocationGranted && isBluetoothPermissionGranted) {
            Text(
                "Scanning for devices...",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            val scannedBleDevices = scanForDevices(bluetoothAdapter.bluetoothLeScanner)
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(scannedBleDevices) { scannedBleDevice ->
                    ScannedItem(
                        scannedBleDevice,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
        }
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

@SuppressLint("MissingPermission")
@Composable
private fun scanForDevices(scanner: BluetoothLeScanner): List<ScannedBleDevice> {
    val results = remember { mutableStateOf(emptyList<ScannedBleDevice>()) }
    DisposableEffect(Unit) {
        // Start scanning for devices
        val callback = object : ScanCallback() {
            override fun onScanFailed(errorCode: Int) {
                Timber.e("onScanFailed: $errorCode")
            }

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                Timber.d("onScanResult: ${result?.toMetadataList()}")
            }

            override fun onBatchScanResults(scanResults: MutableList<ScanResult>?) {
                Timber.d("onBatchScanResults: $scanResults")
                if (scanResults != null) {
                    results.value = scanResults.map(ScanResult::toScannedBleDevice)
                }
            }
        }
        scanner.startScan(callback)
        Timber.d("scan started")
        onDispose {
            scanner.stopScan(callback)
            // Stop scanning for devices
        }
    }
    return results.value
}

private val Intent.isBluetoothEnabled
    get() = getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF) == BluetoothAdapter.STATE_ON


