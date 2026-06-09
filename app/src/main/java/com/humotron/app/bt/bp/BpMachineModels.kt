package com.humotron.app.bt.bp

import android.bluetooth.BluetoothDevice
import com.lepu.blepro.ext.airbp.Battery
import com.lepu.blepro.ext.airbp.DeviceInfo as AirBpDeviceInfo
import com.lepu.blepro.ext.airbp.RtResult as AirBpRtResult
import com.lepu.blepro.ext.bp2.Bp2Config
import com.lepu.blepro.ext.bp2.Bp2File
import com.lepu.blepro.ext.bp2.DeviceInfo as Bp2DeviceInfo
import com.lepu.blepro.ext.bp2.RtData as Bp2RtData
import com.lepu.blepro.ext.bp2w.Bp2wConfig
import com.lepu.blepro.ext.bp2w.Bp2wFile
import com.lepu.blepro.ext.bp2w.DeviceInfo as Bp2wDeviceInfo
import com.lepu.blepro.ext.bp2w.RtData as Bp2wRtData
import com.lepu.blepro.ext.bp3.BleFile
import com.lepu.blepro.ext.bp3.Bp3Config
import com.lepu.blepro.ext.bp3.DeviceInfo as Bp3DeviceInfo
import com.lepu.blepro.ext.bp3.ListCrc
import com.lepu.blepro.ext.bp3.RtData as Bp3RtData
import com.lepu.blepro.ext.bp3.UserList
import com.lepu.blepro.objs.Bluetooth

object BpMachineDefaults {
    val DEFAULT_SCAN_MODELS = intArrayOf(
        Bluetooth.MODEL_AIRBP,
        Bluetooth.MODEL_BP2,
        Bluetooth.MODEL_BP2A,
        Bluetooth.MODEL_BP2T,
        Bluetooth.MODEL_BP2W,
        Bluetooth.MODEL_BP3A,
        Bluetooth.MODEL_BP3B,
        Bluetooth.MODEL_BP3C,
        Bluetooth.MODEL_BP3D,
        Bluetooth.MODEL_BP3E,
        Bluetooth.MODEL_BP3F,
        Bluetooth.MODEL_BP3G,
        Bluetooth.MODEL_BP3H,
        Bluetooth.MODEL_BP3K,
        Bluetooth.MODEL_BP3L,
        Bluetooth.MODEL_BP3Z,
    )

    const val MAX_RECONNECT_ATTEMPTS = 3
    const val BASE_RECONNECT_DELAY_MS = 2_000L
    const val MAX_RECONNECT_DELAY_MS = 10_000L
}

data class BpDiscoveredDevice(
    val model: Int,
    val name: String,
    val macAddress: String,
    val sdkBluetooth: Bluetooth,
    val bluetoothDevice: BluetoothDevice?,
) {
    val deviceKey: String = buildString {
        append(model)
        append('_')
        append(macAddress.uppercase())
    }
}

sealed class BpSdkState {
    data object Uninitialized : BpSdkState()
    data object Initializing : BpSdkState()
    data object Ready : BpSdkState()
    data class Failed(val code: Int, val message: String) : BpSdkState()
}

sealed class BpScanState {
    data object Idle : BpScanState()
    data object Starting : BpScanState()
    data object Scanning : BpScanState()
    data object Stopping : BpScanState()
    data class Failed(val code: Int, val message: String) : BpScanState()
}

sealed class BpConnectionState {
    data object Idle : BpConnectionState()
    data class Connecting(val device: BpDiscoveredDevice) : BpConnectionState()
    data class Connected(val device: BpDiscoveredDevice) : BpConnectionState()
    data class Disconnecting(
        val device: BpDiscoveredDevice,
        val autoReconnect: Boolean,
    ) : BpConnectionState()

    data class Disconnected(
        val device: BpDiscoveredDevice?,
        val reason: Int? = null,
    ) : BpConnectionState()

    data class Reconnecting(
        val device: BpDiscoveredDevice,
        val attempt: Int,
        val delayMs: Long,
    ) : BpConnectionState()

    data class Failed(
        val device: BpDiscoveredDevice?,
        val code: Int,
        val message: String,
    ) : BpConnectionState()
}

sealed class BpError {
    data class Initialization(val code: Int, val message: String) : BpError()
    data class Scan(val code: Int, val message: String) : BpError()
    data class Connection(
        val device: BpDiscoveredDevice?,
        val code: Int,
        val message: String,
    ) : BpError()

    data class Command(
        val model: Int?,
        val code: Int,
        val message: String,
    ) : BpError()

    data class Unknown(val message: String, val throwable: Throwable? = null) : BpError()
}

sealed class BpMachineEvent {
    data class ServiceInitialized(val success: Boolean) : BpMachineEvent()
    data class BluetoothStateChanged(val enabled: Boolean) : BpMachineEvent()
    data class ScanStarted(val models: IntArray) : BpMachineEvent()
    data object ScanStopped : BpMachineEvent()
    data class DeviceDiscovered(val device: BpDiscoveredDevice) : BpMachineEvent()
    data class ConnectionReady(val device: BpDiscoveredDevice) : BpMachineEvent()
    data class DisconnectReason(val device: BpDiscoveredDevice?, val reason: Int) : BpMachineEvent()
    data class EncryptionVerified(val model: Int, val verified: Boolean) : BpMachineEvent()
    data class RawResult(val code: Int, val message: String) : BpMachineEvent()

    sealed class AirBp : BpMachineEvent() {
        data class SetTime(val model: Int, val success: Boolean) : AirBp()
        data class Info(val model: Int, val data: AirBpDeviceInfo) : AirBp()
        data class BatteryInfo(val model: Int, val data: Battery) : AirBp()
        data class ConfigChanged(val model: Int, val enabled: Boolean) : AirBp()
        data class ConfigLoaded(val model: Int, val enabled: Boolean) : AirBp()
        data class Pressure(val model: Int, val pressure: Int) : AirBp()
        data class State(val model: Int, val state: Int) : AirBp()
        data class Result(val model: Int, val result: AirBpRtResult) : AirBp()
    }

    sealed class Bp2 : BpMachineEvent() {
        data class SyncTime(val model: Int, val success: Boolean) : Bp2()
        data class Info(val model: Int, val data: Bp2DeviceInfo) : Bp2()
        data class FileList(val model: Int, val data: ArrayList<String>) : Bp2()
        data class FileReadProgress(val model: Int, val filename: String, val progress: Int) : Bp2()
        data class FileReadError(val model: Int, val filename: String, val message: String) : Bp2()
        data class FileReadComplete(val model: Int, val data: Bp2File) : Bp2()
        data class RtData(val model: Int, val data: Bp2RtData) : Bp2()
        data class ConfigLoaded(val model: Int, val data: Bp2Config) : Bp2()
        data class ConfigChanged(val model: Int, val success: Boolean) : Bp2()
        data class FactoryReset(val model: Int, val success: Boolean) : Bp2()
    }

    sealed class Bp2W : BpMachineEvent() {
        data class SyncTime(val model: Int, val success: Boolean) : Bp2W()
        data class Info(val model: Int, val data: Bp2wDeviceInfo) : Bp2W()
        data class FileList(val model: Int, val data: ArrayList<String>) : Bp2W()
        data class FileReadProgress(val model: Int, val filename: String, val progress: Int) :
            Bp2W()

        data class FileReadError(val model: Int, val filename: String, val message: String) :
            Bp2W()

        data class FileReadComplete(val model: Int, val data: Bp2wFile) : Bp2W()
        data class RtData(val model: Int, val data: Bp2wRtData) : Bp2W()
        data class ConfigLoaded(val model: Int, val data: Bp2wConfig) : Bp2W()
        data class ConfigChanged(val model: Int, val success: Boolean) : Bp2W()
        data class FactoryReset(val model: Int, val success: Boolean) : Bp2W()
    }

    sealed class Bp3 : BpMachineEvent() {
        data class SetTime(val model: Int, val success: Boolean) : Bp3()
        data class Info(val model: Int, val data: Bp3DeviceInfo) : Bp3()
        data class FileList(val model: Int, val data: BleFile) : Bp3()
        data class FileListCrc(val model: Int, val data: ListCrc) : Bp3()
        data class FileReadProgress(val model: Int, val filename: String, val progress: Int) :
            Bp3()

        data class FileReadComplete(val model: Int, val data: BleFile) : Bp3()
        data class FileWriteProgress(val model: Int, val progress: Int) : Bp3()
        data class FileWriteComplete(val model: Int, val data: ListCrc) : Bp3()
        data class RtData(val model: Int, val data: Bp3RtData) : Bp3()
        data class ConfigLoaded(val model: Int, val data: Bp3Config) : Bp3()
        data class ConfigChanged(val model: Int, val success: Boolean) : Bp3()
        data class FactoryReset(val model: Int, val success: Boolean) : Bp3()
        data class UserListWritten(val model: Int, val data: UserList) : Bp3()
    }
}

internal fun Bluetooth.toBpDevice(): BpDiscoveredDevice {
    return BpDiscoveredDevice(
        model = model,
        name = name.orEmpty(),
        macAddress = macAddr.orEmpty(),
        sdkBluetooth = this,
        bluetoothDevice = device,
    )
}
