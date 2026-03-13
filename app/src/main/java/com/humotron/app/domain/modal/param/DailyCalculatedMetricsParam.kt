package com.humotron.app.domain.modal.param

data class DailyCalculatedMetricsParam(
    val offset: String,
    val metricId: String,
    val startDate: String,
    val endDate: String
)
