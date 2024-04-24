package com.threshchyshyn.androidmacremote.remote

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import timber.log.Timber
import java.util.UUID

internal enum class ConnectionState {
    Connected,
    Disconnected,
}

private const val CHARACTERISTIC_UUID = "abcd1234-5678-1234-5678-1234567890ab"
private const val SERVICE_UUID = "12345678-1234-1234-1234-1234567890ab"


@SuppressLint("MissingPermission")
internal class TogglePlaybackGattCallback : BluetoothGattCallback() {
    private val _isServiceDiscovered = mutableStateOf(false)
    val isServiceDiscovered get() = _isServiceDiscovered.value

    private val _isWriteSuccess = mutableStateOf(false)
    val isWriteSuccess get() = _isWriteSuccess.value

    private val _connectionState = mutableStateOf(ConnectionState.Disconnected)
    val connectionState get() = _connectionState.value

    private var savedGatt: BluetoothGatt? = null


    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        Timber.d("onConnectionStateChange: status=$status, newState=$newState")
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
        Timber.d("disconnected")
    }
}
