package com.humotron.app.data.local.entity.band

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "band_spo2",
    indices = [Index(value = ["hardwareId", "measuredAt"], unique = true)],
)
data class BandSpO2Data(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hardwareId: String,
    val measuredAt: Long,
    val date: String,
    val automaticSpo2Data: Int,
    val sync: Boolean = false,
)