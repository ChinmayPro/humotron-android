package com.humotron.app.data.local.entity.band

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "band_total_activity",
    indices = [Index(value = ["hardwareId", "measuredAt"], unique = true)],
)
data class BandTotalActivityData(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hardwareId: String,
    val measuredAt: Long,
    val date: String,
    val goal: String,
    val distance: Double,
    val calories: Double,
    val activeMinutes: Int,
    val step: Int,
    val exerciseMinutes: Int,
    val sync: Boolean = false,
)
