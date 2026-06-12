package com.humotron.app.data.local.entity.bpmachine

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "bp_data",
    indices = [Index(value = ["hardwareId", "measureTime"], unique = true)]
)
data class BpData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hardwareId: String,
    val measureTime: Long,
    val sys: Int,
    val dia: Int,
    val mean: Int,
    val pr: Int,
    val arrhythmia: Boolean,
    val fileName: String,
    val sync: Boolean = false,
)