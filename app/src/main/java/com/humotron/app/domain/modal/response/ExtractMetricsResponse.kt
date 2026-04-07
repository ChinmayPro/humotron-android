package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExtractMetricsResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: MetricsData
) : Parcelable

@Parcelize
data class MetricsData(
    @SerializedName("pdfData")
    val pdfData: List<PdfReportData>,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("uploadType")
    val uploadType: String,
    @SerializedName("pdfCount")
    val pdfCount: Int,
    @SerializedName("_id")
    val id: String
) : Parcelable

@Parcelize
data class PdfReportData(
    @SerializedName("originalName")
    val originalName: String,
    @SerializedName("fileName")
    val fileName: String,
    @SerializedName("pdfId")
    val pdfId: String,
    @SerializedName("isMetricGenerated")
    val isMetricGenerated: Boolean,
    @SerializedName("uploadType")
    val uploadType: String,
    @SerializedName("uploadedAt")
    val uploadedAt: Long,
    @SerializedName("dateSince")
    val dateSince: Int,
    @SerializedName("patientName")
    val patientName: String? = null,
    @SerializedName("labNo")
    val labNo: String? = null,
    @SerializedName("labName")
    val labName: String? = null,
    @SerializedName("date")
    val date: String? = null
) : Parcelable
