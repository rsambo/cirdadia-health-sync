package com.circadia.healthsync.data.model

import com.google.gson.annotations.SerializedName

/**
 * Aggregated metrics data for an exercise session.
 * Values are nullable - null means the metric was not available.
 */
data class ExerciseSessionData(
    @SerializedName("energyBurned")
    val energyBurned: Double?,  // kcal

    @SerializedName("totalDistance")
    val totalDistance: Double?,  // meters

    @SerializedName("steps")
    val steps: Long?,  // count

    @SerializedName("elevationGain")
    val elevationGain: Double?,  // meters

    @SerializedName("avgHeartRate")
    val avgHeartRate: Double?,  // bpm

    @SerializedName("laps")
    val laps: List<ExerciseLap>?  // null if no laps recorded
)

