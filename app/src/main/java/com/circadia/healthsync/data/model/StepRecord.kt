package com.circadia.healthsync.data.model

import com.google.gson.annotations.SerializedName

/**
 * Time range data for a step record.
 */
data class StepRecordData(
    @SerializedName("startTime")
    val startTime: String,  // ISO 8601 format: "2025-12-15T09:30:00Z"

    @SerializedName("endTime")
    val endTime: String  // ISO 8601 format: "2025-12-15T09:45:00Z"
)

/**
 * Represents a single step record to be sent to the API.
 * Matches the API schema:
 * {
 *   "id": "...",
 *   "type": "steps",
 *   "date": "2025-12-15",
 *   "count": 500,
 *   "data": {
 *     "startTime": "2025-12-15T09:30:00Z",
 *     "endTime": "2025-12-15T09:45:00Z"
 *   }
 * }
 */
data class StepRecord(
    @SerializedName("id")
    override val id: String,  // Health Connect record ID

    @SerializedName("type")
    override val type: String = "steps",

    @SerializedName("date")
    val date: String,  // Format: "YYYY-MM-DD"

    @SerializedName("count")
    val count: Long,

    @SerializedName("data")
    val data: StepRecordData
) : SyncRecord

