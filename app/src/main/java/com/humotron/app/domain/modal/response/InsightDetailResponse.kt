package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class InsightDetailResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: InsightDetailData? = null
) : Parcelable

@Parcelize
data class InsightDetailData(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("categoryId")
    val categoryId: String? = null,
    @SerializedName("insightType")
    val insightType: String? = null,
    @SerializedName("metricId")
    val metricId: List<String>? = null,
    @SerializedName("patternDays")
    val patternDays: Int? = null,
    @SerializedName("userId")
    val userId: String? = null,
    @SerializedName("insightRange")
    val insightRange: String? = null,
    @SerializedName("startDate")
    val startDate: String? = null,
    @SerializedName("endDate")
    val endDate: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("metricName")
    val metricName: String? = null,
    @SerializedName("insightJson")
    val insightJson: InsightJson? = null
) : Parcelable

@Parcelize
data class InsightJson(
    @SerializedName("insight")
    val insight: InsightDetailInner? = null,
    @SerializedName("user_id")
    val userId: String? = null,
    @SerializedName("observational_lens")
    val observationalLens: String? = null,
    @SerializedName("metric")
    val metric: String? = null
) : Parcelable

@Parcelize
data class InsightDetailInner(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("short_title")
    val shortTitle: String? = null,
    @SerializedName("short_description")
    val shortDescription: String? = null,
    @SerializedName("summary")
    val summary: String? = null,
    @SerializedName("hypothesis")
    val hypothesis: InsightHypothesis? = null,
    @SerializedName("nugget_visualisation")
    val nuggetVisualisation: NuggetVisualisation? = null,
    @SerializedName("suggested_actions")
    val suggestedActions: List<SuggestedAction>? = null
) : Parcelable

@Parcelize
data class InsightHypothesis(
    @SerializedName("narrative")
    val narrative: String? = null,
    @SerializedName("reasons")
    val reasons: List<String>? = null
) : Parcelable

@Parcelize
data class NuggetVisualisation(
    @SerializedName("graph_title")
    val graphTitle: String? = null,
    @SerializedName("chart_type")
    val chartType: String? = null,
    @SerializedName("x_axis_label")
    val xAxisLabel: String? = null,
    @SerializedName("y_axis_label")
    val yAxisLabel: String? = null,
    @SerializedName("interval")
    val interval: String? = null,
    @SerializedName("x_values")
    val xValues: List<String>? = null,
    @SerializedName("y_values")
    val yValues: List<Float>? = null
) : Parcelable

@Parcelize
data class SuggestedAction(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("description")
    val description: String? = null
) : Parcelable
