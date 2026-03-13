package com.humotron.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "hr", indices = [Index(value = ["time"], unique = true)])
data class HrData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val time: Long,
    val timeStamp: String,
    val hr: Int,
    val sync: Boolean = false
)

@Entity(tableName = "hrv", indices = [Index(value = ["time"], unique = true)])
data class HrvData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val time: Long,
    val timeStamp: String,
    val hrv: Int,
    val sync: Boolean = false
)

@Entity(tableName = "step", indices = [Index(value = ["time"], unique = true)])
data class StepData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val time: Long,
    val timeStamp: String,
    val step: Int,
    val sync: Boolean = false
)

@Entity(tableName = "temp", indices = [Index(value = ["time"], unique = true)])
data class TempData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val time: Long,
    val timeStamp: String,
    val temp: Double,
    val sync: Boolean = false
)

data class HrMapper(
    val id: Long = 0,
    val hr: Int = 0,
    val date: String = "-",
    val time: String = "-"
)

data class HrvMapper(
    val id: Long = 0,
    val hrv: Int = 0,
    val date: String = "-",
    val time: String = "-",
    val milisecond: Long = 0
)


data class TempMapper(
    val id: Long = 0,
    val temp: Double = 0.0,
    val date: String = "-",
    val time: String = "-"
)

data class StepMapper(
    val id: Long = 0,
    val step: Int = 0,
    val date: String = "-",
    val time: String = "-"
)

data class SleepMapper(
    val id: Int = 0,
    val duration: Long = 0,
    val efficiency: Double = 0.0,
    val deepEfficiency: Float = 0f,
    val date: String = "-",
)


data class IntUploadMapper(
    val type: String,
    val value: Int,
    val date: String
)

data class DoubleUploadMapper(
    val type: String,
    val value: Double,
    val date: String
)

data class SleepUploadMapper(
    @SerializedName("spo2")
    val spo2: Int,
    @SerializedName("Avg. sleepStart")
    val sleepStart: Long,
    @SerializedName("Avg. hr")
    val hr: Double,
    @SerializedName("Avg. effieiency")
    val efficiency: Double,
    @SerializedName("Avg. qulalityDuration")
    val duration: Long,
    @SerializedName("time")
    val time: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("Avg. hrv")
    val hrv: Double,
    @SerializedName("Avg. br")
    val br: Double,
    @SerializedName("Avg. hrDip")
    val hrDip: Double,
    @SerializedName("Avg. sleepEnd")
    val sleepEnd: Long,
    @SerializedName("Avg. deepDuration")
    val deepDuration: Long,
    @SerializedName("Avg. isNap")
    val isNap: Boolean,

    )


data class UploadData(
    val hardwareId: String,
    val data: UploadDeviceData,
    val recordTimestamp: Long
)

data class UploadDeviceData(
    @SerializedName("HeartRate")
    val heartRate: List<IntUploadMapper>,
    @SerializedName("HRV")
    val heartRateVariability: List<IntUploadMapper>,
    @SerializedName("Temperature")
    val temperature: List<DoubleUploadMapper>,
    @SerializedName("STEPS")
    val steps: List<IntUploadMapper>,
    @SerializedName("Sleep")
    val sleep: List<SleepUploadMapper>
)


