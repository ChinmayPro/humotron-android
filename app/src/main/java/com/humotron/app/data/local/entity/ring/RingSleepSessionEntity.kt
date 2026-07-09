package com.humotron.app.data.local.entity.ring

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ring_sleep_session",
    indices = [Index(value = ["hardwareId", "startTs", "endTs"], unique = true)],
)
data class RingSleepSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hardwareId: String,
    val startTs: Long,
    val endTs: Long,
    val duration: Long,
    val efficiency: Double,
    val avgHr: Double,
    val hrv: Double,
    val rr: Double,
    val spo2: Double? = null,
    val restHr: Double? = null,
    val t90: Double? = null,
    val sleepLatencyStartTs: Long? = null,
    val sleepLatencyEndTs: Long? = null,
    val isNap: Boolean,
    val stagesJson: String? = null,
    val sync: Boolean = false,
)
