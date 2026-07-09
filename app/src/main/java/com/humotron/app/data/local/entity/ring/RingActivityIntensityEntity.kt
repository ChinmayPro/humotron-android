package com.humotron.app.data.local.entity.ring

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ring_activity_intensity",
    indices = [Index(value = ["hardwareId", "ts"], unique = true)],
)
data class RingActivityIntensityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hardwareId: String,
    val ts: Long,
    val intensity: Int,
    val steps: Int,
    val sync: Boolean = false,
)
