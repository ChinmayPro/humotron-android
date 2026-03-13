package com.humotron.app.domain.modal.response

data class DailyCalculatedMetricsResponse(
    val status: String,
    val message: String,
    val data: List<DailyMetricItem>
)

data class DailyMetricItem(
    val key: String,
    val value: List<MetricDetail>
)

data class MetricDetail(
    val _id: String,
    val metricName: String,
    val metricUnit: String,
    val metricUserFacingName: String,
    val deviceId: String,
    val status: String,
    val metricWhat: String,
    val metricWhy: String,
    val observationLens: String,
    val metricRecommendedName: String,
    val metricDescription: String,
    val metricValue: MetricValue,
    val metricDuration: String,
    val calculationPeriod: Int,
    val recommendations: List<Any>,
    val metricReadingSubText: String,
    val fontColor: String,
    val boxColor: String,
    val insightCount: Int,
    val supplementCount: Int,
    val recipeCount: Int,
    val metricReading: String,
    val zones: List<Zone>
)

data class MetricValue(
    val fieldLabel: String,
    val value: String,
    val timestamp: String
)

data class Zone(
    val type: String,
    val zone: Int,
    val targetTime: Int,
    val range: String,
    val timeSpent: Double,
    val goalCompletion: Double
)
