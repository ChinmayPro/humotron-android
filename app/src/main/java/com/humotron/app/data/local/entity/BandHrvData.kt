package com.humotron.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "band_hrv",
    indices = [Index(value = ["hardwareId", "measuredAt"], unique = true)],
)
data class BandHrvData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hardwareId: String,
    val measuredAt: Long,
    val date: String,
    val highBP: Int,
    val lowBP: Int,
    val heartRate: Int,
    val stress: Int,
    val hrv: Int,
    val vascularAging: Int,
    val sync: Boolean = false,
)

