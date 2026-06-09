package com.humotron.app.bt.common

import android.bluetooth.BluetoothAdapter

enum class BluetoothState {
    ON,
    OFF,
    TURNING_ON,
    TURNING_OFF;

    val isEnabled: Boolean
        get() = this == ON

    companion object {
        fun fromAdapterState(state: Int): BluetoothState {
            return when (state) {
                BluetoothAdapter.STATE_ON -> ON
                BluetoothAdapter.STATE_OFF -> OFF
                BluetoothAdapter.STATE_TURNING_ON -> TURNING_ON
                BluetoothAdapter.STATE_TURNING_OFF -> TURNING_OFF
                else -> OFF
            }
        }
    }
}
