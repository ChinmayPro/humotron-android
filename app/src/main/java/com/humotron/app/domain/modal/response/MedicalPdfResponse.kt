package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class MedicalPdfResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: MedicalPdfData
)

data class MedicalPdfData(
    @SerializedName("pdfData")
    val pdfData: List<MedicalPdf>,
    @SerializedName("userId")
    val userId: String? = null,
    @SerializedName("pdfCount")
    val pdfCount: Int = 0
)

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
)

data class MedicalPdfMetric(
    @SerializedName("metricName")
    val metricName: String?,
    @SerializedName("metricDate")
    val metricDate: String?,
    @SerializedName("metricReading")
    val metricReading: String?,
    @SerializedName("metricUnit")
    val metricUnit: String?
)

data class MedicalPdfDetails(
    @SerializedName("patientName")
    val patientName: String? = null,
    @SerializedName("labName")
    val labName: String? = null,
    @SerializedName("labNo")
    val labNo: String? = null,
    @SerializedName("date")
    val date: String? = null
)

fun MedicalPdf.toPdfReportData(): com.humotron.app.domain.modal.response.PdfReportData {
    return com.humotron.app.domain.modal.response.PdfReportData(
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
