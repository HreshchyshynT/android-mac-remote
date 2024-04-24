package com.threshchyshyn.androidmacremote.discover.model

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScannedBleDevice(
    val device: BluetoothDevice,
    val rssi: Int,
    val metadata: List<String>,
) : Parcelable {
    @IgnoredOnParcel
    @SuppressLint("MissingPermission")
    val name: String = device.name ?: "Unknown"

    @IgnoredOnParcel
    val address: String = device.address ?: "Unknown"
}

@SuppressLint("MissingPermission")
fun ScanResult.toScannedBleDevice(): ScannedBleDevice {
    return ScannedBleDevice(
        device = device,
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
        "Service UUIDs: ${serviceUuids?.joinToString()}",
        "Manufacturer Specific Data: $manufacturerSpecificData",
        "Service Data: $serviceData",
        "Tx Power Level: $txPowerLevel",
        "Device Name: $deviceName",
        "Bytes: ${bytes.contentToString()}"
    )
}

fun ScanResult.toMetadataList(): List<String> {
    return listOf(
        "Device: ${device?.toMetadataList()}",
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
