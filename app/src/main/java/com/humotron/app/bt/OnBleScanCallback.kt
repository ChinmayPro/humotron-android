package com.humotron.app.bt

interface OnBleScanCallback {

    fun onScanning(result: BleDevice)

    fun onScanFinished()
}