package com.circadia.healthsync.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a lap within an exercise session.
 */
data class ExerciseLap(
    @SerializedName("startTime")
    val startTime: String,  // ISO 8601 format

    @SerializedName("endTime")
    val endTime: String  // ISO 8601 format
)

