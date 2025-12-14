package com.circadia.healthsync.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response body from the sync API endpoint.
 */
data class SyncResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("recordCount")
    val recordCount: Int,

    @SerializedName("timestamp")
    val timestamp: String
)

