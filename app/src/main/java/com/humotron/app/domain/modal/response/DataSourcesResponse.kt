package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DataSourcesResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: Data?
) : Parcelable {

    @Parcelize
    data class Data(
        @SerializedName("sections")
        val sections: List<Section>?
    ) : Parcelable

    @Parcelize
    data class Section(
        @SerializedName("section")
        val section: String?,
        @SerializedName("title")
        val title: String?,
        @SerializedName("subTitle")
        val subTitle: String?,
        @SerializedName("sources")
        val sources: List<Source>?
    ) : Parcelable

    @Parcelize
    data class Source(
        @SerializedName("sourceKey")
        val sourceKey: String?,
        @SerializedName("section")
        val section: String?,
        @SerializedName("name")
        val name: String?,
        @SerializedName("icon")
        val icon: String?,
        @SerializedName("accentColor")
        val accentColor: String?,
        @SerializedName("description")
        val description: String?,
        @SerializedName("refId")
        val refId: String?,
        @SerializedName("isConnected")
        val isConnected: Boolean?,
        @SerializedName("status")
        val status: String?,
        @SerializedName("meta")
        val meta: String?,
        @SerializedName("lastSyncedAt")
        val lastSyncedAt: String?,
        @SerializedName("includeInAnalysis")
        val includeInAnalysis: Boolean?,
        @SerializedName("isPaused")
        val isPaused: Boolean?,
        @SerializedName("usage")
        val usage: Usage?,
        @SerializedName("topicCount")
        val topicCount: Int?,
        @SerializedName("excludedTopicCount")
        val excludedTopicCount: Int?
    ) : Parcelable

    @Parcelize
    data class Usage(
        @SerializedName("aiChat")
        val aiChat: Boolean?,
        @SerializedName("aiInsights")
        val aiInsights: Boolean?,
        @SerializedName("productSuggestions")
        val productSuggestions: Boolean?,
        @SerializedName("deepDives")
        val deepDives: Boolean?
    ) : Parcelable
}
