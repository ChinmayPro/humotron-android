package com.humotron.app.domain.modal.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class MetricType : Parcelable {
    data class Exercise(
        val metric: ExerciseIntensityMetric,
        val deviceId: String?,
        val dataSync: String?,
        val deviceName: String?,
    ) : MetricType()

    data class Sleep(
        val metric: SleepDurationMetric,
        val deviceId: String?,
        val dataSync: String?,
        val deviceName: String?,
    ) : MetricType()

    data class PhysicalRecovery(
        val metric: PhysicalRecoveryMetric,
        val deviceId: String?,
        val dataSync: String?,
        val deviceName: String?,
    ) : MetricType()

    data class Stress(
        val metric: StressScoreMetric,
        val deviceId: String?,
        val dataSync: String?,
        val deviceName: String?,
    ) : MetricType()
}
