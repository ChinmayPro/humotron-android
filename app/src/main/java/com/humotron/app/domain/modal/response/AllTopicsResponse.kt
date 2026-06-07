package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AllTopicsResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: AllTopicsData?
) : Parcelable

@Parcelize
data class AllTopicsData(
    @SerializedName("totalRecords")
    val totalRecords: Int?,
    @SerializedName("topics")
    val topics: List<SearchTopicItem>?
) : Parcelable
