package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SupportHomeResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: SupportHomeData?
) : Parcelable

@Parcelize
data class SupportHomeData(
    @SerializedName("connected_devices")
    val connectedDevices: List<SupportConnectedDevice>?,
    @SerializedName("recent_tickets")
    val recentTickets: List<SupportTicket>?,
    @SerializedName("popular_search_keywords")
    val popularSearchKeywords: List<PopularSearchKeyword>?,
    @SerializedName("categories")
    val categories: List<SupportCategory>?,
    @SerializedName("popular_articles")
    val popularArticles: List<PopularArticle>?
) : Parcelable

@Parcelize
data class SupportConnectedDevice(
    @SerializedName("hardware_id")
    val hardwareId: String?,
    @SerializedName("device_label")
    val deviceLabel: String?,
    @SerializedName("device_scope_key")
    val deviceScopeKey: String?,
    @SerializedName("device_id")
    val deviceId: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("device_url")
    val deviceUrl: String?,
    @SerializedName("uuid")
    val uuid: String?,
    @SerializedName("connected_at")
    val connectedAt: String?,
    @SerializedName("hardware_type")
    val hardwareType: String?,
    @SerializedName("serial_number")
    val serialNumber: String?
) : Parcelable

@Parcelize
data class SupportTicket(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("current_status")
    val currentStatus: String?,
    @SerializedName("priority_level")
    val priorityLevel: String?,
    @SerializedName("ticket_number")
    val ticketNumber: String?,
    @SerializedName("subject")
    val subject: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("createdAt")
    val createdAt: String?
) : Parcelable

@Parcelize
data class PopularSearchKeyword(
    @SerializedName("count")
    val count: Int?,
    @SerializedName("keyword")
    val keyword: String?
) : Parcelable

@Parcelize
data class SupportCategory(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("key")
    val key: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("display_order")
    val displayOrder: Int?,
    @SerializedName("icon")
    val icon: String?,
    @SerializedName("label")
    val label: String?,
    @SerializedName("subcategories")
    val subcategories: List<SupportSubcategory>?
) : Parcelable

@Parcelize
data class SupportSubcategory(
    @SerializedName("visibility")
    val visibility: String?,
    @SerializedName("is_active")
    val isActive: Boolean?,
    @SerializedName("key")
    val key: String?,
    @SerializedName("label")
    val label: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("icon")
    val icon: String?,
    @SerializedName("display_order")
    val displayOrder: Int?
) : Parcelable

@Parcelize
data class PopularArticle(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("subcategory_label")
    val subcategoryLabel: String?,
    @SerializedName("subtitle")
    val subtitle: String?,
    @SerializedName("category_key")
    val categoryKey: String?,
    @SerializedName("topic_id")
    val topicId: String?,
    @SerializedName("article_type")
    val articleType: String?,
    @SerializedName("contact_reason_code")
    val contactReasonCode: String?,
    @SerializedName("device_scope")
    val deviceScope: List<String>?,
    @SerializedName("priority")
    val priority: Int?,
    @SerializedName("subcategory_key")
    val subcategoryKey: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("slug")
    val slug: String?,
    @SerializedName("view_count")
    val viewCount: Int?,
    @SerializedName("time_to_read")
    val timeToRead: Int?,
    @SerializedName("category_label")
    val categoryLabel: String?
) : Parcelable
