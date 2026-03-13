package com.humotron.app.domain.modal

import lib.linktop.nexring.api.SleepStage
import lib.linktop.nexring.api.SleepState

class Histogram(val avg: Number, val list: List<Array<Number>>)

class SleepNap(
    val start: Float = 0f,
    val end: Float = 0f,
    val duration: Long = 0L,
)


data class SleepSession(
    val start: Float,
    val end: Float,
    val duration: Long,
    val efficiency: Double,
    val sleepStages: ArrayList<SleepStage>,
    val sleepStates: Array<SleepState>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SleepSession

        if (start != other.start) return false
        if (end != other.end) return false
        if (duration != other.duration) return false
        if (efficiency != other.efficiency) return false
        if (sleepStages != other.sleepStages) return false
        if (!sleepStates.contentEquals(other.sleepStates)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + end.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + efficiency.hashCode()
        result = 31 * result + sleepStages.hashCode()
        result = 31 * result + sleepStates.contentHashCode()
        return result
    }
}