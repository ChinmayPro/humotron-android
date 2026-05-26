package com.humotron.app.bt.weight

import com.qn.device.constant.QNAreaType
import com.qn.device.constant.UserGoal
import com.qn.device.constant.UserShape
import com.qn.device.out.QNBleDevice
import com.qn.device.out.QNScaleData
import java.util.Date

object WeightScaleSdkDefaults {
    const val APP_ID = "123456789"
    const val ENCRYPT_FILE_PATH = "file:///android_asset/123456789.qn"
    const val DEFAULT_SCAN_TIMEOUT_MILLIS = 15_000L
    const val DEFAULT_CONNECT_TIMEOUT_MILLIS = 20_000L
    const val MAX_RECONNECT_ATTEMPTS = 3
    const val BASE_RECONNECT_DELAY_MILLIS = 2_000L
}

data class WeightScaleUserProfile(
    val userId: String,
    val heightCm: Int,
    val heightUnit: String? = null,
    val gender: String,
    val birthday: Date,
    val athleteType: Int = 0,
    val userShape: UserShape = UserShape.SHAPE_NORMAL,
    val userGoal: UserGoal = UserGoal.GOAL_STAY_HEALTH,
    val clothesWeightKg: Double = 0.0,
    val userIndex: Int = 0,
    val secret: Int = 0,
    val areaType: QNAreaType = QNAreaType.AreaTypeAsia,
    val measureFat: Boolean = true,
    val adjustBodyAge: Boolean = false,
) {
    val normalizedGender: String
        get() = gender.trim().lowercase()
}

data class WeightScaleDeviceSummary(
    val mac: String,
    val name: String,
    val bluetoothName: String?,
    val rssi: Int,
    val modeId: String?,
    val deviceType: Int,
    val supportsEightElectrodes: Boolean,
    val supportsWifi: Boolean,
    val supportsUserBinding: Boolean,
)

data class WeightScaleMetric(
    val type: Int,
    val name: String?,
    val value: Double,
    val valueType: Int,
)

data class WeightScaleMeasurement(
    val mac: String,
    val measuredAt: Date?,
    val weightKg: Double,
    val heightCm: Double,
    val resistance50: Int,
    val resistance500: Int,
    val hmac: String?,
    val bodyMetrics: List<WeightScaleMetric>,
    val rawScaleData: QNScaleData,
)

sealed class WeightScaleSdkState {
    data object Uninitialized : WeightScaleSdkState()
    data object Initializing : WeightScaleSdkState()
    data object Ready : WeightScaleSdkState()
    data class Failed(val code: Int, val message: String) : WeightScaleSdkState()
}

sealed class WeightScaleScanState {
    data object Idle : WeightScaleScanState()
    data object Starting : WeightScaleScanState()
    data object Scanning : WeightScaleScanState()
    data object Stopping : WeightScaleScanState()
    data class Failed(val code: Int, val message: String) : WeightScaleScanState()
}

sealed class WeightScaleConnectionState {
    data object Idle : WeightScaleConnectionState()
    data class Connecting(val device: WeightScaleDeviceSummary) : WeightScaleConnectionState()
    data class Connected(val device: WeightScaleDeviceSummary) : WeightScaleConnectionState()
    data class Ready(val device: WeightScaleDeviceSummary) : WeightScaleConnectionState()
    data class Disconnecting(val device: WeightScaleDeviceSummary) : WeightScaleConnectionState()
    data class Disconnected(val device: WeightScaleDeviceSummary?) : WeightScaleConnectionState()
    data class Reconnecting(val device: WeightScaleDeviceSummary, val attempt: Int) :
        WeightScaleConnectionState()

    data class Failed(
        val device: WeightScaleDeviceSummary?,
        val code: Int,
        val message: String,
    ) : WeightScaleConnectionState()
}

sealed class WeightScaleMeasurementState {
    data object Idle : WeightScaleMeasurementState()
    data class Measuring(val device: WeightScaleDeviceSummary) : WeightScaleMeasurementState()
    data class UnsteadyWeight(val device: WeightScaleDeviceSummary, val weightKg: Double) :
        WeightScaleMeasurementState()

    data class Completed(
        val device: WeightScaleDeviceSummary,
        val measurement: WeightScaleMeasurement,
    ) : WeightScaleMeasurementState()

    data class StoredDataReceived(val device: WeightScaleDeviceSummary, val count: Int) :
        WeightScaleMeasurementState()
}

sealed class WeightScaleEvent {
    data class BluetoothStateChanged(val stateName: String) : WeightScaleEvent()
    data class BatteryUpdated(
        val device: WeightScaleDeviceSummary,
        val level: Int,
        val isCharging: Boolean,
    ) : WeightScaleEvent()

    data class ScaleStatusChanged(val device: WeightScaleDeviceSummary, val status: Int) :
        WeightScaleEvent()

    data class ScaleEventChanged(val device: WeightScaleDeviceSummary, val event: Int) :
        WeightScaleEvent()

    data class FirmwareVersionReceived(val device: WeightScaleDeviceSummary, val version: Int) :
        WeightScaleEvent()

    data class SerialNumberReceived(val device: WeightScaleDeviceSummary, val serialNumber: String) :
        WeightScaleEvent()

    data class ResultMessage(val code: Int, val message: String) : WeightScaleEvent()
}

sealed class WeightScaleError {
    data class Initialization(val code: Int, val message: String) : WeightScaleError()
    data class Scan(val code: Int, val message: String) : WeightScaleError()
    data class Connection(
        val device: WeightScaleDeviceSummary?,
        val code: Int,
        val message: String,
    ) : WeightScaleError()

    data class UserValidation(val code: Int, val message: String) : WeightScaleError()
}

internal fun QNBleDevice.toSummary(): WeightScaleDeviceSummary {
    return WeightScaleDeviceSummary(
        mac = getMac().orEmpty(),
        name = getName().orEmpty(),
        bluetoothName = getBluetoothName(),
        rssi = getRssi(),
        modeId = getModeId(),
        deviceType = getDeviceType(),
        supportsEightElectrodes = isSupportEightElectrodes(),
        supportsWifi = isSupportWifi(),
        supportsUserBinding = isUserScale() || isOneToOne(),
    )
}

internal fun QNScaleData.toMeasurement(device: QNBleDevice): WeightScaleMeasurement {
    return WeightScaleMeasurement(
        mac = device.getMac().orEmpty(),
        measuredAt = measureTime,
        weightKg = weight,
        heightCm = height,
        resistance50 = resistance50,
        resistance500 = resistance500,
        hmac = hmac,
        bodyMetrics = allItem.orEmpty().map { item ->
            WeightScaleMetric(
                type = item.type,
                name = item.name,
                value = item.value,
                valueType = item.valueType,
            )
        },
        rawScaleData = this,
    )
}
