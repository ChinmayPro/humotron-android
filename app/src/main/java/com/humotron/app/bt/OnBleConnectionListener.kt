package com.humotron.app.bt

interface OnBleConnectionListener {

    fun onBleState(state: Int)

    fun onBleReady()
}