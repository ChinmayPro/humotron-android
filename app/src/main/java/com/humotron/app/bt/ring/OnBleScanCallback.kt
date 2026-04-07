package com.humotron.app.bt.ring

interface OnBleScanCallback {

    fun onScanning(result: RingBleDevice)

    fun onScanFinished()
}