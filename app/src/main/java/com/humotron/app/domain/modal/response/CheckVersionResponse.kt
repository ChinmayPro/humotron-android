package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class CheckVersionBaseResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: CheckVersionResponse?
)

data class CheckVersionResponse(
    @SerializedName("updateStatus")
    val updateStatus: String?,
    @SerializedName("latestVersion")
    val latestVersion: String?,
    @SerializedName("latestBuildNumber")
    val latestBuildNumber: Int?,
    @SerializedName("minSupportedVersion")
    val minSupportedVersion: String?,
    @SerializedName("minSupportedBuildNumber")
    val minSupportedBuildNumber: Int?,
    @SerializedName("storeUrl")
    val storeUrl: String?,
    @SerializedName("releaseNotes")
    val releaseNotes: ReleaseNotes?,
    @SerializedName("maintenance")
    val maintenance: Maintenance?
) {
    data class ReleaseNotes(
        @SerializedName("title")
        val title: String?,
        @SerializedName("message")
        val message: String?
    )

    data class Maintenance(
        @SerializedName("isActive")
        val isActive: Boolean?,
        @SerializedName("title")
        val title: String?,
        @SerializedName("message")
        val message: String?,
        @SerializedName("endAt")
        val endAt: String?
    )
}
