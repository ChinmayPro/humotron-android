package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class WearableProviderResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: Data? = null
) : Parcelable {

    @Parcelize
    data class Data(
        @SerializedName("devices")
        val devices: List<WearableDevice>? = null
    ) : Parcelable

    @Parcelize
    data class WearableDevice(
        @SerializedName("user_id")
        val userId: String? = null,
        @SerializedName("provider")
        val provider: String? = null,
        @SerializedName("provider_user_id")
        val providerUserId: String? = null,
        @SerializedName("provider_username")
        val providerUsername: String? = null,
        @SerializedName("scope")
        val scope: String? = null,
        @SerializedName("id")
        val id: String? = null,
        @SerializedName("status")
        val status: String? = null,
        @SerializedName("last_synced_at")
        val lastSyncedAt: String? = null,
        @SerializedName("created_at")
        val createdAt: String? = null,
        @SerializedName("updated_at")
        val updatedAt: String? = null,
        @SerializedName("max_historical_days")
        val maxHistoricalDays: Int? = null,
        @SerializedName("rest_pull")
        val restPull: Boolean? = null,
        @SerializedName("webhook_stream")
        val webhookStream: Boolean? = null,
        @SerializedName("webhook_ping")
        val webhookPing: Boolean? = null,
        @SerializedName("webhook_callback")
        val webhookCallback: Boolean? = null,
        @SerializedName("live_sync_mode")
        val liveSyncMode: String? = null,
        @SerializedName("linked_user_ids")
        val linkedUserIds: List<String>? = null,
        @SerializedName("historicalSync")
        val historicalSync: Boolean? = null
    ) : Parcelable
}
