package com.threshchyshyn.androidmacremote.discover.model

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult

data class ScannedBleDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val metadata: List<String>,
)

@SuppressLint("MissingPermission")
fun ScanResult.toScannedBleDevice(): ScannedBleDevice {
    return ScannedBleDevice(
        name = device?.name ?: "Unknown",
        address = device?.address ?: "Unknown",
        rssi = rssi,
        metadata = toMetadataList(),
    )
}

@SuppressLint("MissingPermission")
fun BluetoothDevice.toMetadataList(): List<String> {
    return listOf(
        "Address: $address",
        "Name: ${name ?: "N/A"}",
        "Bond State: $bondState",
        "Type: $type",
        "Bluetooth Class: ${bluetoothClass?.deviceClass ?: "N/A"}",
        "UUIDs: ${uuids?.joinToString() ?: "N/A"}"
    )
}

fun ScanRecord.toMetadataList(): List<String> {
    return listOf(
        "Advertise Flags: $advertiseFlags",
        "Service UUIDs: ${serviceUuids.joinToString()}",
        "Manufacturer Specific Data: $manufacturerSpecificData",
        "Service Data: $serviceData",
        "Tx Power Level: $txPowerLevel",
        "Device Name: $deviceName",
        "Bytes: ${bytes.contentToString()}"
    )
}

fun ScanResult.toMetadataList(): List<String> {
    return listOf(
        "Device: ${device.toMetadataList()}",
        "Scan Record: ${scanRecord?.toMetadataList()}",
        "RSSI: $rssi",
        "Timestamp Nanos: $timestampNanos",
        "Primary Phy: $primaryPhy",
        "Secondary Phy: $secondaryPhy",
        "Advertising Sid: $advertisingSid",
        "Tx Power: $txPower",
        "Periodic Advertising Interval: $periodicAdvertisingInterval"
    )
}
