package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MyTicketsResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: MyTicketsData?
) : Parcelable

@Parcelize
data class MyTicketsData(
    @SerializedName("page")
    val page: Int?,
    @SerializedName("limit")
    val limit: Int?,
    @SerializedName("pages")
    val pages: Int?,
    @SerializedName("total")
    val total: Int?,
    @SerializedName("tickets")
    val tickets: List<TicketDetail>?
) : Parcelable

@Parcelize
data class TicketDetail(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("rating_comment")
    val ratingComment: String?,
    @SerializedName("source")
    val source: String?,
    @SerializedName("device_meta_snapshot")
    val deviceMetaSnapshot: DeviceMetaSnapshot?,
    @SerializedName("user_email")
    val userEmail: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("user_id")
    val userId: String?,
    @SerializedName("app_version")
    val appVersion: String?,
    @SerializedName("messages")
    val messages: List<TicketMessage>?,
    @SerializedName("region")
    val region: String?,
    @SerializedName("subject")
    val subject: String?,
    @SerializedName("category")
    val category: String?,
    @SerializedName("subcategory")
    val subcategory: String?,
    @SerializedName("current_screen")
    val currentScreen: Int?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("current_status")
    val currentStatus: String?,
    @SerializedName("assigned_agent_id")
    val assignedAgentId: String?,
    @SerializedName("resolved_at")
    val resolvedAt: String?,
    @SerializedName("os_platform")
    val osPlatform: String?,
    @SerializedName("attachments")
    val attachments: List<TicketAttachment>?,
    @SerializedName("contact_reason_code")
    val contactReasonCode: String?,
    @SerializedName("first_response_at")
    val firstResponseAt: String?,
    @SerializedName("last_activity_at")
    val lastActivityAt: String?,
    @SerializedName("priority_level")
    val priorityLevel: String?,
    @SerializedName("assigned_agent_name")
    val assignedAgentName: String?,
    @SerializedName("rating")
    val rating: String?,
    @SerializedName("user_name")
    val userName: String?,
    @SerializedName("ticket_number")
    val ticketNumber: String?
) : Parcelable

@Parcelize
data class DeviceMetaSnapshot(
    @SerializedName("deviceId")
    val deviceId: String?,
    @SerializedName("fw")
    val fw: String?,
    @SerializedName("mac")
    val mac: String?,
    @SerializedName("captured_at")
    val capturedAt: String?,
    @SerializedName("sn")
    val sn: String?,
    @SerializedName("lpm")
    val lpm: Boolean?,
    @SerializedName("desc")
    val desc: String?
) : Parcelable

@Parcelize
data class TicketAttachment(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("filename")
    val filename: String?,
    @SerializedName("url")
    val url: String?,
    @SerializedName("mime_type")
    val mimeType: String?,
    @SerializedName("size_bytes")
    val sizeBytes: Long?
) : Parcelable

@Parcelize
data class TicketMessage(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("sender_type")
    val senderType: String?,
    @SerializedName("sender_id")
    val senderId: String?,
    @SerializedName("sender_name")
    val senderName: String?,
    @SerializedName("body")
    val body: String?,
    @SerializedName("is_read")
    val isRead: Boolean?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("attachments")
    val attachments: List<TicketAttachment>?
) : Parcelable
