package com.humotron.app.bt.ring

import android.bluetooth.BluetoothDevice

data class RingBleDevice(
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

val RingBleDevice.isCharging: Boolean
    get() = batteryState == 1
