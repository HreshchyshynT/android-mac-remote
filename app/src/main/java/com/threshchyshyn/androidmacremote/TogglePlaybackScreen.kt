package com.threshchyshyn.androidmacremote

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Build
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.threshchyshyn.androidmacremote.discover.model.ScannedBleDevice
import timber.log.Timber
import java.util.UUID

private enum class ConnectionState {
    Connected,
    Disconnected,
}

private const val CHARACTERISTIC_UUID = "abcd1234-5678-1234-5678-1234567890ab"
private const val SERVICE_UUID = "12345678-1234-1234-1234-1234567890ab"

@SuppressLint("MissingPermission")
private class MyGattCallback : BluetoothGattCallback() {
    private val _isServiceDiscovered = mutableStateOf(false)
    val isServiceDiscovered get() = _isServiceDiscovered.value

    private val _isWriteSuccess = mutableStateOf(false)
    val isWriteSuccess get() = _isWriteSuccess.value

    private val _connectionState = mutableStateOf(ConnectionState.Disconnected)
    val connectionState get() = _connectionState.value

    private var savedGatt: BluetoothGatt? = null


    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        Timber.d("onConnectionStateChange: status=$status, newState=$newState, gatt=$gatt")
        when (newState) {
            BluetoothGatt.STATE_CONNECTED -> {
                _connectionState.value = ConnectionState.Connected
                savedGatt = gatt
                gatt?.discoverServices()
            }

            BluetoothGatt.STATE_DISCONNECTED -> {
                _connectionState.value = ConnectionState.Disconnected
                savedGatt = null
            }
        }
        Timber.d("gatt in the end: $gatt")
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        Timber.d("onServicesDiscovered: status=$status")
        _isServiceDiscovered.value = status == BluetoothGatt.GATT_SUCCESS
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int,
    ) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        Timber.d("onCharacteristicWrite: status=$status, characteristic uuid=${characteristic?.uuid}")
        if (characteristic != null && characteristic.uuid.toString() == CHARACTERISTIC_UUID) {
            _isWriteSuccess.value = status == BluetoothGatt.GATT_SUCCESS
        }
    }

    fun togglePlaying() {
        Timber.d("saved GATT: $savedGatt")
        if (savedGatt == null) Timber.e("Gatt is null when trying to toggle playing")
        val gatt = savedGatt ?: return
        val service = gatt.getService(UUID.fromString(SERVICE_UUID))
        if (service == null) {
            Timber.e("Service not found")
            return
        }
        val characteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
        if (characteristic == null) {
            Timber.e("Characteristic not found")
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeCharacteristic(
                characteristic,
                byteArrayOf(0x01),
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
            )
        } else {
            characteristic.value = byteArrayOf(0x01)
            gatt.writeCharacteristic(characteristic)
        }
        Timber.d("characteristic written")
    }

    fun disconnect() {
        savedGatt?.disconnect()
    }
}

@SuppressLint("MissingPermission")
@Composable
internal fun TogglePlaybackScreen(
    scannedBleDevice: ScannedBleDevice,
) {
    val context = LocalContext.current
    val gattCallback = remember { MyGattCallback() }
    DisposableEffect(Unit) {
        scannedBleDevice.device.connectGatt(
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
            text = "Connect to Device: ${scannedBleDevice.name}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Text(
            text = "MAC Address: ${scannedBleDevice.address}",
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
