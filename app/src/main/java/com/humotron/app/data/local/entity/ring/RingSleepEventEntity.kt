package com.humotron.app.data.local.entity.ring

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ring_sleep_event",
    indices = [Index(value = ["hardwareId", "sleepTs", "type"], unique = true)],
)
data class RingSleepEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hardwareId: String,
    val sleepTs: Long,
    val type: Int,
    val bedRestDuration: Int,
    val awakeningOrder: Int,
    val sync: Boolean = false,
)
