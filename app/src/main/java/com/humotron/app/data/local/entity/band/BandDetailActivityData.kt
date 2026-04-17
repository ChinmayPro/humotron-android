package com.humotron.app.data.local.entity.band

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "band_detail_activity",
    indices = [Index(value = ["hardwareId", "measuredAt"], unique = true)],
)
data class BandDetailActivityData(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hardwareId: String,
    val measuredAt: Long,
    val date: String,
    val arraySteps: List<Int>,
    val step: Int,
    val distance: Double,
    val calories: Double,
    val sync: Boolean = false,
)
