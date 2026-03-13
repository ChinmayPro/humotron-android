package com.humotron.app.domain.modal.response


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RingReadingData(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?,
) : Parcelable {
    @Parcelize
    data class Data(
        @SerializedName("device")
        val device: Device?,
        @SerializedName("exerciseIntensityMetric")
        val exerciseIntensityMetric: ExerciseIntensityMetric?,
        @SerializedName("hardwareData")
        val hardwareData: List<HardwareData?>?,
        @SerializedName("physicalRecoveryMetric")
        val physicalRecoveryMetric: PhysicalRecoveryMetric?,
        @SerializedName("sleepDurationMetric")
        val sleepDurationMetric: SleepDurationMetric?,
        @SerializedName("stressScoreMetric")
        val stressScoreMetric: StressScoreMetric?,
    ) : Parcelable {
        @Parcelize
        data class Device(
            @SerializedName("dataSync")
            val dataSync: String?,
            @SerializedName("deviceFacingName")
            val deviceFacingName: String?,
            @SerializedName("deviceIcon")
            val deviceIcon: String?,
            @SerializedName("deviceImage")
            val deviceImage: List<String?>?,
            @SerializedName("deviceName")
            val deviceName: String?,
            @SerializedName("deviceType")
            val deviceType: String?,
            @SerializedName("_id")
            val id: String?,
            @SerializedName("isReading")
            val isReading: String?,
            @SerializedName("minReading")
            val minReading: MinReading?,
            @SerializedName("status")
            val status: String?,
        ) : Parcelable {
            @Parcelize
            data class MinReading(
                @SerializedName("unit")
                val unit: String?,
                @SerializedName("value")
                val value: Int?,
            ) : Parcelable
        }

        @Parcelize
        data class HardwareData(
            @SerializedName("type")
            val type: String?,
            @SerializedName("value")
            val value: Double?,
        ) : Parcelable
    }
}
