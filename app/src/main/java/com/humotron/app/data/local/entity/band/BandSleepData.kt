package com.humotron.app.data.local.entity.band

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "band_sleep",
    indices = [Index(value = ["hardwareId", "measuredAt"], unique = true)],
)
data class BandSleepData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hardwareId: String,
    val measuredAt: Long,
    val date: String,
    val arraySleepQuality: List<Int>,
    val totalSleepTime: Int,
    val sleepUnitLength: Int,
    val startTimeSleepData: String,
    val sync: Boolean = false,
)
