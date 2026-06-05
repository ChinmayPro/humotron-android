package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SupportTopicDetailResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: SupportTopicDetailData?
) : Parcelable

@Parcelize
data class SupportTopicDetailData(
    @SerializedName("subcategory")
    val subcategory: TopicSubcategory?,
    @SerializedName("topic")
    val topic: SupportTopicDetail?,
    @SerializedName("category")
    val category: TopicCategory?,
    @SerializedName("related_topics")
    val relatedTopics: List<SearchTopicItem>?
) : Parcelable

@Parcelize
data class TopicSubcategory(
    @SerializedName("key")
    val key: String?,
    @SerializedName("label")
    val label: String?,
    @SerializedName("icon")
    val icon: String?
) : Parcelable

@Parcelize
data class TopicCategory(
    @SerializedName("key")
    val key: String?,
    @SerializedName("label")
    val label: String?,
    @SerializedName("icon")
    val icon: String?
) : Parcelable

@Parcelize
data class SupportTopicDetail(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("category_key")
    val categoryKey: String?,
    @SerializedName("slug")
    val slug: String?,
    @SerializedName("helpful_yes")
    val helpfulYes: Int?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("region_scope")
    val regionScope: List<String>?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("tags")
    val tags: List<String>?,
    @SerializedName("time_to_read")
    val timeToRead: Int?,
    @SerializedName("device_scope")
    val deviceScope: List<String>?,
    @SerializedName("category_label")
    val categoryLabel: String?,
    @SerializedName("helpful_no")
    val helpfulNo: Int?,
    @SerializedName("subcategory_key")
    val subcategoryKey: String?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("visibility")
    val visibility: String?,
    @SerializedName("search_keywords")
    val searchKeywords: List<String>?,
    @SerializedName("short_answer")
    val shortAnswer: String?,
    @SerializedName("priority")
    val priority: Int?,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("subtitle")
    val subtitle: String?,
    @SerializedName("topic_id")
    val topicId: String?,
    @SerializedName("contact_reason_code")
    val contactReasonCode: String?,
    @SerializedName("view_count")
    val viewCount: Int?,
    @SerializedName("subcategory_label")
    val subcategoryLabel: String?,
    @SerializedName("category_id")
    val categoryId: String?,
    @SerializedName("steps")
    val steps: List<String>?,
    @SerializedName("related_topic_ids")
    val relatedTopicIds: List<String>?,
    @SerializedName("article_type")
    val articleType: String?
) : Parcelable
