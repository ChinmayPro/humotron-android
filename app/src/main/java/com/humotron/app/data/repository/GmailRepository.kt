package com.humotron.app.data.repository

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.MessagePart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log



@Singleton
class GmailRepository @Inject constructor() {

    companion object {
        private const val TAG = "GmailRepository"
        private val SCOPES = listOf("https://www.googleapis.com/auth/gmail.readonly")
    }

    suspend fun searchPdfAttachments(
        context: Context,
        accountName: String,
        keywords: List<String>,
        labels: List<String>,
        dateRange: String,
        hasAttachments: Boolean
    ): List<ClinicalDocumentInfo> = withContext(Dispatchers.IO) {
        try {
            val credential = GoogleAccountCredential.usingOAuth2(context, SCOPES)
            credential.selectedAccountName = accountName

            val service = Gmail.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName("Humotron").build()

            val query = buildQuery(keywords, labels, dateRange, hasAttachments)
            Log.d(TAG, "Gmail Query: $query")

            val messagesResponse = service.users().messages().list("me")
                .setQ(query)
                .execute()

            val pdfAttachments = mutableListOf<ClinicalDocumentInfo>()

            messagesResponse.messages?.forEach { messageSummary ->
                val fullMessage = service.users().messages().get("me", messageSummary.id).execute()
                if (hasAttachments) {
                    extractPdfAttachments(fullMessage, pdfAttachments)
                }
            }

            Log.d(TAG, "Found ${pdfAttachments.size} PDF attachments")
            pdfAttachments.forEach { 
                val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(it.timestamp))
                Log.d(TAG, "Retrieved PDF: ${it.fileName} (Size: ${it.size} bytes, Received: $dateStr)")
            }

            pdfAttachments
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Gmail data: ${e.message}", e)
            emptyList()
        }
    }

    private fun buildQuery(
        keywords: List<String>,
        labels: List<String>,
        dateRange: String,
        hasAttachments: Boolean
    ): String {
        return buildString {
            // Attachment status
            if (hasAttachments) {
                append("(has:attachment) ")
            } else {
                append("(!has:attachment) ")
            }

            // Subject Search (e.g. subject:Blood OR subject:Report)
            if (keywords.isNotEmpty()) {
                val subjectQuery = keywords.joinToString(" OR ") { "subject:$it" }
                append("($subjectQuery) ")
            }

            // Body Search (using Keywords and Labels as general terms)
            val bodyWords = (keywords + labels).distinct()
            if (bodyWords.isNotEmpty()) {
                append("${bodyWords.joinToString(" ")} ")
            }

            // Date Range logic
            val calendarAfter = Calendar.getInstance()
            val calendarBefore = Calendar.getInstance()
            calendarBefore.add(Calendar.DAY_OF_YEAR, 1) // "before" is exclusive, so tomorrow includes today

            var useAfter = true
            var useBefore = true

            when (dateRange) {
                "Past 2 Years" -> {
                    calendarAfter.add(Calendar.YEAR, -2)
                }
                "Past 2 - 5 Years" -> {
                    calendarAfter.add(Calendar.YEAR, -5)
                    calendarBefore.add(Calendar.YEAR, -2)
                }
                "Past 5 - 10 Years" -> {
                    calendarAfter.add(Calendar.YEAR, -10)
                    calendarBefore.add(Calendar.YEAR, -5)
                }
                "Past 10 & More Years" -> {
                    calendarBefore.add(Calendar.YEAR, -10)
                    useAfter = false
                }
                else -> {
                    // Fallback to simple "after 2 years" if string doesn't match
                    calendarAfter.add(Calendar.YEAR, -2)
                    useBefore = true
                }
            }

            val sdf = java.text.SimpleDateFormat("yyyy/MM/dd", Locale.US)
            if (useAfter) {
                append("after:${sdf.format(calendarAfter.time)} ")
            }
            if (useBefore) {
                append("before:${sdf.format(calendarBefore.time)} ")
            }
        }.trim()
    }

    private fun extractPdfAttachments(message: com.google.api.services.gmail.model.Message, list: MutableList<ClinicalDocumentInfo>) {
        val parts = message.payload.parts ?: return
        findPdfParts(message.id, message.internalDate, parts, list)
    }

    private fun findPdfParts(messageId: String, timestamp: Long, parts: List<MessagePart>, list: MutableList<ClinicalDocumentInfo>) {
        for (part in parts) {
            if (part.mimeType == "application/pdf" && part.body.attachmentId != null) {
                list.add(
                    ClinicalDocumentInfo(
                        messageId = messageId,
                        attachmentId = part.body.attachmentId,
                        fileName = part.filename ?: "unknown.pdf",
                        mimeType = part.mimeType,
                        size = part.body.size?.toLong() ?: 0L,
                        timestamp = timestamp
                    )
                )
            }
            // Recursive check for nested multi-part
            part.parts?.let { findPdfParts(messageId, timestamp, it, list) }
        }
    }

    suspend fun downloadAttachment(
        context: Context,
        accountName: String,
        messageId: String,
        attachmentId: String,
        fileName: String
    ): java.io.File? = withContext(Dispatchers.IO) {
        try {
            val credential = GoogleAccountCredential.usingOAuth2(context, SCOPES)
            credential.selectedAccountName = accountName

            val service = Gmail.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName("Humotron").build()

            val attachment = service.users().messages().attachments().get("me", messageId, attachmentId).execute()
            val data = com.google.api.client.util.Base64.decodeBase64(attachment.data)
            
            val tempFile = java.io.File(context.cacheDir, fileName)
            tempFile.outputStream().use { it.write(data) }
            
            Log.d(TAG, "Attachment downloaded: ${tempFile.absolutePath}")
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading attachment: ${e.message}", e)
            null
        }
    }
}
