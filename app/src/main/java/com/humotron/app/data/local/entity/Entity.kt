package com.humotron.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import lib.linktop.nexring.api.SleepStage
import lib.linktop.nexring.api.SleepState

@Entity(
    tableName = "sleep_data", indices = [
        Index(value = ["startTs"], unique = true),
        Index(value = ["endTs"], unique = true)
    ]
)
data class SleepEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val btMac: String,
    val startTs: Long,
    val startTimeStamp: String,
    val endTs: Long,
    val endTimeStamp: String,
    val duration: Long,
    val efficiency: Double,
    val hr: Double,
    val hrDip: Double,
    val hrv: Double,
    val rr: Double,
    val spo2: Double?,
    val isNap: Boolean,
    val sleepStages: List<SleepStage>,
    val sleepStates: List<SleepState>,
    val sync: Boolean = false // for sync tracking
)
