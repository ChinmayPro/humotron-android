package com.humotron.app.data.repository

data class ClinicalDocumentInfo(
    val messageId: String = "",
    val attachmentId: String = "",
    val fileName: String,
    val mimeType: String,
    val size: Long,
    val timestamp: Long,
    val uri: String? = null
)
