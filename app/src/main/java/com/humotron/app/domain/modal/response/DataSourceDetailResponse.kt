package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DataSourceDetailResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: Data?
) : Parcelable {

    @Parcelize
    data class Data(
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
        @SerializedName("includeInAnalysis")
        val includeInAnalysis: Boolean?,
        @SerializedName("isPaused")
        val isPaused: Boolean?,
        @SerializedName("usage")
        val usage: DataSourcesResponse.Usage?,
        @SerializedName("usageToggles")
        val usageToggles: List<UsageToggle>?,
        @SerializedName("lastPausedAt")
        val lastPausedAt: String?,
        @SerializedName("lastPurgedAt")
        val lastPurgedAt: String?,
        @SerializedName("topicToggles")
        val topicToggles: List<TopicToggle>?,
        @SerializedName("excludedTopics")
        val excludedTopics: List<String>?,
        @SerializedName("refId")
        val refId: String?,
        @SerializedName("isConnected")
        val isConnected: Boolean?,
        @SerializedName("lastSyncedAt")
        val lastSyncedAt: String?,
        @SerializedName("status")
        val status: String?
    ) : Parcelable

    @Parcelize
    data class UsageToggle(
        @SerializedName("key")
        val key: String?,
        @SerializedName("label")
        val label: String?,
        @SerializedName("description")
        val description: String?,
        @SerializedName("isEnabled")
        val isEnabled: Boolean?
    ) : Parcelable

    @Parcelize
    data class TopicToggle(
        @SerializedName("key")
        val key: String?,
        @SerializedName("label")
        val label: String?,
        @SerializedName("isEnabled")
        val isEnabled: Boolean?
    ) : Parcelable
}
