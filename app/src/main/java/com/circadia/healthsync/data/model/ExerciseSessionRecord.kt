package com.circadia.healthsync.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents an exercise session record to be sent to the API.
 */
data class ExerciseSessionRecord(
    @SerializedName("id")
    override val id: String,  // Health Connect record ID

    @SerializedName("type")
    override val type: String = "exercise_session",

    @SerializedName("exerciseType")
    val exerciseType: String,  // e.g., "RUNNING", "WALKING", etc.

    @SerializedName("startTime")
    val startTime: String,  // ISO 8601 format

    @SerializedName("endTime")
    val endTime: String,  // ISO 8601 format

    @SerializedName("source")
    val source: String,  // Package name of app that created the record

    @SerializedName("title")
    val title: String?,  // Optional user title

    @SerializedName("notes")
    val notes: String?,  // Optional user notes

    @SerializedName("data")
    val data: ExerciseSessionData
) : SyncRecord

