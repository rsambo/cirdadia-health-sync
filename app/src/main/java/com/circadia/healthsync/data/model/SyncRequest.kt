package com.circadia.healthsync.data.model

import com.google.gson.annotations.SerializedName

/**
 * Request body for the sync API endpoint.
 * Contains a list of step records to send.
 */
data class SyncRequest(
    @SerializedName("records")
    val records: List<StepRecord>
)

