package com.humotron.app.data.local.entity.bpmachine

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "ecg_data",
    indices = [Index(value = ["hardwareId", "measureTime"], unique = true)]
)
data class EcgData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val hardwareId: String,

    val measureTime: Long,

    val date: String,

    val fileVersion: Int,

    val fileType: Int,

    val recordingTime: Int,

    val result: Int,

    val hr: Int,

    val qrs: Int,

    val pvcs: Int,

    val qtc: Int,

    val connectCable: Boolean,
    val fileName: String,

    @Embedded(prefix = "diagnosis_")
    val diagnosis: EcgDiagnosisEntity,

    val sync: Boolean = false,
)