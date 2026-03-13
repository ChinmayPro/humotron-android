package com.humotron.app.domain.modal.response

data class WristBandSleepDurationResponse(
    val status: String,
    val message: String,
    val data: List<WristBandSleepDurationItem>
)

data class WristBandSleepDurationItem(
    val key: String,
    val value: List<WristBandMetricDetail>
)

data class WristBandMetricDetail(
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
    val metricValue: WristBandMetricValue,
    val metricDuration: String,
    val calculationPeriod: Int,
    val recommendations: List<Any>,
    val metricReadingSubText: String,
    val fontColor: String,
    val boxColor: String,
    val insightCount: Int,
    val supplementCount: Int,
    val recipeCount: Int,
    val metricReading: String
)

data class WristBandMetricValue(
    val fieldLabel: String,
    val value: String,
    val timestamp: String,
    val sleepStartTime: String?,
    val sleepEndTime: String?
)
