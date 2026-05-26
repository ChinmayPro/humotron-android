package com.humotron.app.domain.modal.param

import android.content.Context
import com.google.gson.annotations.SerializedName
import com.humotron.app.bt.ring.RingBleDevice
import com.humotron.app.bt.weight.WeightScaleDeviceSummary
import com.humotron.app.domain.modal.response.UserHardware
import com.humotron.app.util.toRingColor
import com.jstyle.blesdk2208a.model.Device

import android.os.Parcelable
import com.humotron.app.domain.modal.response.AddHardwareResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeviceMetaDataParam(
    @SerializedName("deviceId")
    val deviceId: String,
    @SerializedName("data")
    val data: DeviceMetaData,
) : Parcelable {
    companion object {
        fun from(
            device: RingBleDevice,
            userHardware: UserHardware,
            deviceId: String?,
            context: Context,
        ): DeviceMetaDataParam {
            val colorName = context.toRingColor(device.color)
            val desc = "$colorName, size ${device.size}, Generation ${device.generation ?: ""}"
            return DeviceMetaDataParam(
                deviceId = deviceId ?: "",
                data = DeviceMetaData(
                    sn = userHardware.hardwareSerialNumber ?: device.sn ?: "",
                    mac = device.device.address,
                    desc = desc,
                    fw = "",
                    measureFreq = "60",
                    lowPowerMode = false
                )
            )
        }

        fun from(
            device: Device,
            userHardware: UserHardware,
            deviceId: String?,
        ): DeviceMetaDataParam {
            return DeviceMetaDataParam(
                deviceId = deviceId ?: "",
                data = DeviceMetaData(
                    sn = userHardware.hardwareSerialNumber ?: "",
                    mac = device.mac,
                    desc = device.name ?: "WristBand",
                    fw = "",
                    measureFreq = "60",
                    lowPowerMode = false
                )
            )
        }

        fun from(
            device: WeightScaleDeviceSummary,
            userHardware: UserHardware,
            deviceId: String?,
        ): DeviceMetaDataParam {
            return DeviceMetaDataParam(
                deviceId = deviceId ?: "",
                data = DeviceMetaData(
                    sn = userHardware.hardwareSerialNumber ?: "",
                    mac = device.mac,
                    desc = device.name ?: "WeightMachine",
                    fw = "",
                    measureFreq = "0",
                    lowPowerMode = false
                )
            )
        }
    }
}

@Parcelize
data class DeviceMetaData(
    @SerializedName("sn")
    val sn: String,
    @SerializedName("mac")
    val mac: String,
    @SerializedName("desc")
    val desc: String,
    @SerializedName("fw")
    val fw: String,
    @SerializedName("mf")
    val measureFreq: String,
    @SerializedName("lpm")
    val lowPowerMode: Boolean,
) : Parcelable
