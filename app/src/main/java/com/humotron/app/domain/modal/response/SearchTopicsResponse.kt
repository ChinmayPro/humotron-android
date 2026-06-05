package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchTopicsResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: SearchTopicsData?
) : Parcelable

@Parcelize
data class SearchTopicsData(
    @SerializedName("pagination")
    val pagination: SupportPagination?,
    @SerializedName("subcategories")
    val subcategories: List<SearchSubcategoryItem>?,
    @SerializedName("topics")
    val topics: List<SearchTopicItem>?,
    @SerializedName("categories")
    val categories: List<SearchCategoryItem>?,
    @SerializedName("counts")
    val counts: SearchCounts?,
    @SerializedName("filters")
    val filters: SearchFilters?
) : Parcelable

@Parcelize
data class SupportPagination(
    @SerializedName("limit")
    val limit: Int?,
    @SerializedName("pages")
    val pages: Int?,
    @SerializedName("total")
    val total: Int?,
    @SerializedName("page")
    val page: Int?
) : Parcelable

@Parcelize
data class SearchSubcategoryItem(
    @SerializedName("category_label")
    val categoryLabel: String?,
    @SerializedName("count")
    val count: Int?,
    @SerializedName("subcategory_label")
    val subcategoryLabel: String?,
    @SerializedName("subcategory_key")
    val subcategoryKey: String?,
    @SerializedName("subcategory_icon")
    val subcategoryIcon: String?,
    @SerializedName("category_key")
    val categoryKey: String?
) : Parcelable

@Parcelize
data class SearchTopicItem(
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
    @SerializedName("tags")
    val tags: List<String>?,
    @SerializedName("slug")
    val slug: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("time_to_read")
    val timeToRead: Int?,
    @SerializedName("view_count")
    val viewCount: Int?,
    @SerializedName("category_label")
    val categoryLabel: String?,
    @SerializedName("short_answer")
    val shortAnswer: String?
) : Parcelable

@Parcelize
data class SearchCategoryItem(
    @SerializedName("category_icon")
    val categoryIcon: String?,
    @SerializedName("category_label")
    val categoryLabel: String?,
    @SerializedName("category_key")
    val categoryKey: String?,
    @SerializedName("count")
    val count: Int?
) : Parcelable

@Parcelize
data class SearchCounts(
    @SerializedName("subcategories")
    val subcategories: Int?,
    @SerializedName("categories")
    val categories: Int?,
    @SerializedName("topics")
    val topics: Int?
) : Parcelable

@Parcelize
data class SearchFilters(
    @SerializedName("region")
    val region: String?,
    @SerializedName("subcategory_key")
    val subcategoryKey: String?,
    @SerializedName("device_scope")
    val deviceScope: String?,
    @SerializedName("q")
    val q: String?,
    @SerializedName("search_mode")
    val searchMode: String?,
    @SerializedName("article_type")
    val articleType: String?,
    @SerializedName("category_key")
    val categoryKey: String?
) : Parcelable
