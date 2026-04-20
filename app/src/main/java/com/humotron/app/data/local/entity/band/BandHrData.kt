package com.humotron.app.data.local.entity.band

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "band_hr",
    indices = [Index(value = ["hardwareId", "measuredAt"], unique = true)],
)
data class BandHrData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hardwareId: String,
    val measuredAt: Long,
    val date: String,
    val singleHR: Int,
    val sync: Boolean = false,
)