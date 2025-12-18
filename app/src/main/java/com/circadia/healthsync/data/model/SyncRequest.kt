package com.circadia.healthsync.data.model

import com.google.gson.annotations.SerializedName

/**
 * Request body for the sync API endpoint.
 * Contains records to upsert and deleted record IDs.
 */
data class SyncRequest(
    @SerializedName("syncType")
    val syncType: String,  // "full" or "incremental"

    @SerializedName("records")
    val records: List<StepRecord>,

    @SerializedName("deletedRecordIds")
    val deletedRecordIds: List<String> = emptyList()
)

