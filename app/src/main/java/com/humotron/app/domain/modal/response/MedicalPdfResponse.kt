package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MedicalPdfResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: MedicalPdfData
) : Parcelable

@Parcelize
data class MedicalPdfData(
    @SerializedName("pdfData")
    val pdfData: List<MedicalPdf>,
    @SerializedName("userId")
    val userId: String? = null,
    @SerializedName("pdfCount")
    val pdfCount: Int = 0,
    @SerializedName("uploadType")
    val uploadType: String,
    @SerializedName("_id")
    val id: String
) : Parcelable

@Parcelize
data class MedicalPdf(
    @SerializedName("pdfId")
    val id: String,
    @SerializedName("originalName")
    val fileName: String,
    @SerializedName("fileName")
    val serverFileName: String? = null,
    @SerializedName("uploadedAt")
    val uploadedAt: Long = 0,
    @SerializedName("dateSince")
    val dateSince: Int = 0,
    @SerializedName("pdf")
    val details: MedicalPdfDetails? = null,
    @SerializedName("isMetricGenerated")
    val isMetricGenerated: Boolean = false,
    @SerializedName("uploadType")
    val uploadType: String? = null,
    @SerializedName("isSynced")
    val isSynced: Boolean = true,
    @SerializedName("metrics")
    val metrics: List<MedicalPdfMetric>? = null
) : Parcelable

@Parcelize
data class MedicalPdfMetric(
    @SerializedName("metricName")
    val metricName: String?,
    @SerializedName("metricDate")
    val metricDate: String?,
    @SerializedName("metricReading")
    val metricReading: String?,
    @SerializedName("metricUnit")
    val metricUnit: String?
) : Parcelable

@Parcelize
data class MedicalPdfDetails(
    @SerializedName("patientName")
    val patientName: String? = null,
    @SerializedName("labName")
    val labName: String? = null,
    @SerializedName("labNo")
    val labNo: String? = null,
    @SerializedName("date")
    val date: String? = null
) : Parcelable

fun MedicalPdf.toPdfReportData(): PdfReportData {
    return PdfReportData(
        originalName = this.fileName ?: "",
        fileName = this.serverFileName ?: this.fileName ?: "",
        pdfId = this.id ?: "",
        isMetricGenerated = this.isMetricGenerated,
        uploadType = this.uploadType ?: "",
        uploadedAt = this.uploadedAt,
        dateSince = this.dateSince,
        patientName = this.details?.patientName,
        labNo = this.details?.labNo,
        labName = this.details?.labName,
        date = this.details?.date
    )
}
