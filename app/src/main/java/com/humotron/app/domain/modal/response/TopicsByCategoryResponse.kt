package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TopicsByCategoryResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: TopicsByCategoryData?
) : Parcelable

@Parcelize
data class TopicsByCategoryData(
    @SerializedName("top_articles")
    val topArticles: List<SearchTopicItem>?,
    @SerializedName("total")
    val total: Int?,
    @SerializedName("category")
    val category: SupportCategory?,
    @SerializedName("popular_topics")
    val popularTopics: List<PopularSubcategoryTopics>?
) : Parcelable

@Parcelize
data class PopularSubcategoryTopics(
    @SerializedName("subcategory_icon")
    val subcategoryIcon: String?,
    @SerializedName("topics")
    val topics: List<SearchTopicItem>?,
    @SerializedName("topic_count")
    val topicCount: Int?,
    @SerializedName("subcategory_description")
    val subcategoryDescription: String?,
    @SerializedName("subcategory_label")
    val subcategoryLabel: String?,
    @SerializedName("display_order")
    val displayOrder: Int?,
    @SerializedName("subcategory_key")
    val subcategoryKey: String?
) : Parcelable
