package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AllMetricsResponse(

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: Data? = null

) : Parcelable {

    @Parcelize
    data class Data(

        @SerializedName("_id")
        val id: String? = null,

        @SerializedName("deviceId")
        val deviceId: String? = null,

        @SerializedName("userId")
        val userId: String? = null,

        @SerializedName("createdAt")
        val createdAt: String? = null,

        @SerializedName("device")
        val device: Device? = null,

        @SerializedName("generatedAt")
        val generatedAt: String? = null,

        @SerializedName("metrics")
        val metrics: List<Metric>? = null,

        @SerializedName("updatedAt")
        val updatedAt: String? = null,

        @SerializedName("wristbandMetrics")
        val wristbandMetrics: WristbandMetrics? = null

    ) : Parcelable {

        @Parcelize
        data class Device(

            @SerializedName("_id")
            val id: String? = null,

            @SerializedName("deviceName")
            val deviceName: String? = null,

            @SerializedName("deviceImage")
            val deviceImage: List<String>? = null,

            @SerializedName("deviceType")
            val deviceType: String? = null,

            @SerializedName("deviceModelId")
            val deviceModelId: String? = null,

            @SerializedName("deviceSubCategoryId")
            val deviceSubCategoryId: String? = null,

            @SerializedName("deviceFacingName")
            val deviceFacingName: String? = null,

            @SerializedName("deviceUrl")
            val deviceUrl: List<String>? = null,

            @SerializedName("deviceTextMessage")
            val deviceTextMessage: String? = null,

            @SerializedName("orderStatus")
            val orderStatus: String? = null,

            @SerializedName("dataSync")
            val dataSync: String? = null,

            @SerializedName("deviceModelName")
            val deviceModelName: String? = null,

            @SerializedName("deviceSubCategoryName")
            val deviceSubCategoryName: String? = null,

            @SerializedName("deviceCategoryName")
            val deviceCategoryName: String? = null,

            @SerializedName("metrics")
            val metrics: List<DeviceMetric>? = null

        ) : Parcelable {

            @Parcelize
            data class DeviceMetric(

                @SerializedName("key")
                val key: String? = null,

                @SerializedName("value")
                val value: String? = null,

                @SerializedName("unit")
                val unit: String? = null,

                @SerializedName("shortMetricName")
                val shortMetricName: String? = null

            ) : Parcelable
        }

        @Parcelize
        data class Metric(

            @SerializedName("_id")
            val id: String? = null,

            @SerializedName("metricName")
            val metricName: String? = null,

            @SerializedName("metricUnit")
            val metricUnit: String? = null,

            @SerializedName("metricUserFacingName")
            val metricUserFacingName: String? = null,

            @SerializedName("deviceId")
            val deviceId: String? = null,

            @SerializedName("status")
            val status: String? = null,

            @SerializedName("metricWhat")
            val metricWhat: String? = null,

            @SerializedName("metricWhy")
            val metricWhy: String? = null,

            @SerializedName("observationLens")
            val observationLens: String? = null,

            @SerializedName("timeFormat")
            val timeFormat: String? = null,

            @SerializedName("metricOrder")
            val metricOrder: Int? = null,

            @SerializedName("metricRecommendedName")
            val metricRecommendedName: String? = null,

            @SerializedName("metricDescription")
            val metricDescription: String? = null,

            @SerializedName("metricDuration")
            val metricDuration: String? = null,

            @SerializedName("metricReading")
            val metricReading: String? = null,

            @SerializedName("metricValue")
            val metricValue: MetricValue? = null,

            @SerializedName("recommendations")
            val recommendations: List<Recommendation>? = null,

            @SerializedName("metricReadingSubText")
            val metricReadingSubText: String? = null,

            @SerializedName("fontColor")
            val fontColor: String? = null,

            @SerializedName("boxColor")
            val boxColor: String? = null,

            @SerializedName("insightCount")
            val insightCount: Int? = null,

            @SerializedName("supplementCount")
            val supplementCount: Int? = null,

            @SerializedName("recipeCount")
            val recipeCount: Int? = null

        ) : Parcelable {

            @Parcelize
            data class MetricValue(

                @SerializedName("fieldLabel")
                val fieldLabel: String? = null,

                @SerializedName("value")
                val value: String? = null,

                @SerializedName("timestamp")
                val timestamp: String? = null

            ) : Parcelable

            @Parcelize
            data class Recommendation(

                @SerializedName("recommendationsShort")
                val recommendationsShort: String? = null,

                @SerializedName("recommendationsLong")
                val recommendationsLong: String? = null,

                @SerializedName("recommendationsTag")
                val recommendationsTag: String? = null,

                @SerializedName("isPreview")
                val isPreview: Boolean? = null

            ) : Parcelable
        }

        @Parcelize
        data class WristbandMetrics(
            @SerializedName("sleepDurationMetric")
            val sleepDurationMetric: SleepDurationMetric? = null,

            @SerializedName("stressScoreMetric")
            val stressScoreMetric: StressScoreMetric? = null,

            @SerializedName("exerciseIntensityMetric")
            val exerciseIntensityMetric: ExerciseIntensityMetric? = null,

            @SerializedName("physicalRecoveryMetric")
            val physicalRecoveryMetric: PhysicalRecoveryMetric? = null
        ) : Parcelable
    }
}
