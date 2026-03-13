package com.humotron.app.bt

import android.bluetooth.BluetoothDevice

data class BleDevice(
    val device: BluetoothDevice,
    val cid: String? = null,
    val color: Int,
    val size: Int,
    val batteryState: Int? = null,
    val batteryLevel: Int? = null,
    /*val chipMode: Int = 0,*/
    val generation: Int? = null,
    val sn: String? = null,
    var rssi: Int,
)

val BleDevice.isCharging: Boolean
    get() = batteryState == 1
