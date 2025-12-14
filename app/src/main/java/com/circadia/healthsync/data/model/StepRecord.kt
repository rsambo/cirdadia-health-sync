package com.circadia.healthsync.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a single step record to be sent to the API.
 * Matches the API schema: { type: "steps", date: "2025-12-14", count: 8500 }
 */
data class StepRecord(
    @SerializedName("type")
    val type: String = "steps",

    @SerializedName("date")
    val date: String,  // Format: "YYYY-MM-DD"

    @SerializedName("count")
    val count: Long
)

