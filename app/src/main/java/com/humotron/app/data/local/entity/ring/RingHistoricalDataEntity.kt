package com.humotron.app.data.local.entity.ring

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ring_historical_data",
    indices = [Index(value = ["hardwareId", "ts", "sourceType"], unique = true)],
)
data class RingHistoricalDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hardwareId: String,
    val ts: Long,
    val sourceType: Int,
    val uuid: Int,
    val heartRate: Int? = null,
    val hrv: Int? = null,
    val spo2: Int? = null,
    val rr: Int? = null,
    val skinTemperature: Double? = null,
    val totalSteps: Int? = null,
    val motion: Int? = null,
    val batteryLevel: Int? = null,
    val workout: Int? = null,
    val ibi: Int? = null,
    val cc: Double? = null,
    val t90: Int? = null,
    val sync: Boolean = false,
)
