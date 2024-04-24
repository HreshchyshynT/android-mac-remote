package com.threshchyshyn.androidmacremote.discover

import android.annotation.SuppressLint
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.threshchyshyn.androidmacremote.discover.model.ScannedBleDevice
import com.threshchyshyn.androidmacremote.discover.model.toScannedBleDevice
import kotlinx.coroutines.delay
import timber.log.Timber


internal enum class ScanState {
    Scanning,
    Idle,
    Completed,
    ;

    val isScanning: Boolean
        get() = this == Scanning
}


@SuppressLint("MissingPermission")
internal class ComposableBLEScanner(private val scanner: BluetoothLeScanner) {
    var scanResults by mutableStateOf(emptyList<ScannedBleDevice>())
        private set

    var scanState by mutableStateOf(ScanState.Idle)
        private set


    suspend fun scan() {
        if (scanState.isScanning) {
            Timber.d("scan already in progress")
            return
        }
        scanResults = emptyList()
        val callback = object : ScanCallback() {
            private val resultsAddresses = mutableSetOf<String>()

            override fun onScanFailed(errorCode: Int) {
                Timber.e("onScanFailed: $errorCode")
            }

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                Timber.d("onScanResult: $callbackType - $result")
                if (result == null || resultsAddresses.contains(result.device.address)) return
                resultsAddresses.add(result.device.address)
                scanResults = scanResults + result.toScannedBleDevice()
            }
        }

        scanner.startScan(
            listOf(
                ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid.fromString("12345678-1234-1234-1234-1234567890AB"))
                    .build()
            ),
            ScanSettings.Builder()
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build(),
            callback,
        )
        scanState = ScanState.Scanning
        Timber.d("scan started")
        delay(10_000)
        Timber.d("scan completed")
        scanState = ScanState.Completed
    }
}
