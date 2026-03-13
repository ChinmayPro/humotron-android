package com.humotron.app.domain.modal.response


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddDeviceDataResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
) : Parcelable {
    @Parcelize
    data class Data(
        @SerializedName("hardwareSpecificDetail")
        val hardwareSpecificDetail: HardwareSpecificDetail?
    ) : Parcelable {
        @Parcelize
        data class HardwareSpecificDetail(
            @SerializedName("createdAt")
            val createdAt: String?,
            @SerializedName("data")
            val `data`: Data?,
            @SerializedName("hardwareId")
            val hardwareId: String?,
            @SerializedName("_id")
            val id: String?,
            @SerializedName("isDeleted")
            val isDeleted: Boolean?,
            @SerializedName("pdfId")
            val pdfId: String?,
            @SerializedName("recordId")
            val recordId: String?,
            @SerializedName("recordTimestamp")
            val recordTimestamp: Int?,
            @SerializedName("updatedAt")
            val updatedAt: String?,
            @SerializedName("userId")
            val userId: String?
        ) : Parcelable {
            @Parcelize
            data class Data(
                @SerializedName("HRV")
                val hrv: List<HRV>?,
                @SerializedName("HeartRate")
                val heartRate: List<HeartRate>?,
                @SerializedName("STEPS")
                val steps: List<STEPS>?,
                @SerializedName("Sleep")
                val sleep: List<Sleep>?,
                @SerializedName("Temperature")
                val temperature: List<Temperature>?
            ) : Parcelable {
                @Parcelize
                data class HRV(
                    @SerializedName("date")
                    val date: String?,
                    @SerializedName("type")
                    val type: String?,
                    @SerializedName("value")
                    val value: Int?
                ) : Parcelable

                @Parcelize
                data class HeartRate(
                    @SerializedName("date")
                    val date: String?,
                    @SerializedName("type")
                    val type: String?,
                    @SerializedName("value")
                    val value: Int?
                ) : Parcelable

                @Parcelize
                data class STEPS(
                    @SerializedName("date")
                    val date: String?,
                    @SerializedName("type")
                    val type: String?,
                    @SerializedName("value")
                    val value: Int?
                ) : Parcelable

                @Parcelize
                data class Sleep(
                    @SerializedName("Avg. br")
                    val avgBr: Double?,
                    @SerializedName("Avg. deepDuration")
                    val avgDeepDuration: Int?,
                    @SerializedName("Avg. effieiency")
                    val avgEffieiency: Float?,
                    @SerializedName("Avg. hr")
                    val avgHr: Double?,
                    @SerializedName("Avg. hrDip")
                    val avgHrDip: Double?,
                    @SerializedName("Avg. hrv")
                    val avgHrv: Double?,
                    @SerializedName("Avg. isNap")
                    val avgIsNap: Boolean?,
                    @SerializedName("Avg. qulalityDuration")
                    val avgQulalityDuration: Int?,
                    @SerializedName("Avg. sleepEnd")
                    val avgSleepEnd: Int?,
                    @SerializedName("Avg. sleepStart")
                    val avgSleepStart: Int?,
                    @SerializedName("spo2")
                    val spo2: Int?,
                    @SerializedName("time")
                    val time: String?,
                    @SerializedName("type")
                    val type: String?
                ) : Parcelable

                @Parcelize
                data class Temperature(
                    @SerializedName("date")
                    val date: String?,
                    @SerializedName("type")
                    val type: String?,
                    @SerializedName("value")
                    val value: Double?
                ) : Parcelable
            }
        }
    }
}