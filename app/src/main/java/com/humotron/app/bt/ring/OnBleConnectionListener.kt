package com.humotron.app.bt.ring

interface OnBleConnectionListener {

    fun onBleState(state: Int)

    fun onBleReady()

    fun onBleAdapterStateChanged(isEnabled: Boolean)
}