package com.threshchyshyn.androidmacremote.discover

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.threshchyshyn.androidmacremote.destinations.TogglePlaybackScreenDestination
import com.threshchyshyn.androidmacremote.remote.TogglePlaybackScreenArgs
import kotlinx.coroutines.launch
import timber.log.Timber

@Destination(start = true)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun DiscoverBLEScreen(
    bluetoothAdapter: BluetoothAdapter,
    navigator: DestinationsNavigator,
) {
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
            listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            onPermissionsResult = { Timber.d("Location permission callback: $it") },
        )

        val bluetoothPermission = rememberMultiplePermissionsState(
            listOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
            onPermissionsResult = { Timber.d("Bluetooth permission callback: $it") },
        )

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
            val scanner = remember { ComposableBLEScanner(bluetoothAdapter.bluetoothLeScanner) }

            LaunchedEffect(Unit) { scanner.scan() }

            ScannerTitle(
                scanner = scanner,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(scanner.scanResults) { scannedBleDevice ->
                    ScannedItem(
                        scannedBleDevice,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable {
                                val args = TogglePlaybackScreenArgs(scannedBleDevice)
                                navigator.navigate(TogglePlaybackScreenDestination(args))
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun ScannerTitle(scanner: ComposableBLEScanner, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth()) {
        Text(
            when (scanner.scanState) {
                ScanState.Idle -> "Scanner is idle"
                ScanState.Scanning -> "Scanning for devices"
                ScanState.Completed -> "Scanning completed"
            },
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Center),
        )

        var iconRotation by remember { mutableFloatStateOf(0f) }
        val coroutineScope = rememberCoroutineScope()
        val scanState by rememberUpdatedState(scanner.scanState)
        Icon(
            Icons.Default.Refresh,
            contentDescription = "Refresh",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable {
                    Timber.d("scanState: $scanState")
                    if (scanState != ScanState.Scanning) {
                        coroutineScope.launch { scanner.scan() }
                    }
                }
                .graphicsLayer { rotationZ = iconRotation },
        )
        LaunchedEffect(scanState.isScanning) {
            Timber.d("launch effect scanState: $scanState")
            val isScanning = scanner.scanState.isScanning
            animate(
                iconRotation,
                targetValue = 360f,
                animationSpec = when (isScanning) {
                    true -> infiniteRepeatable(animation = tween(durationMillis = 750, easing = LinearEasing))
                    false -> tween(durationMillis = 300)
                }
            ) { value, _ ->
                // when animation ends value will be 360f so we need to reset it to 0f to be able to animate again
                iconRotation = value % 360f
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

private val Intent.isBluetoothEnabled
    get() = getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF) == BluetoothAdapter.STATE_ON


