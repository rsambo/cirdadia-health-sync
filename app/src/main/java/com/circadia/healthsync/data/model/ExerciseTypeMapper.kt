package com.circadia.healthsync.data.model

import androidx.health.connect.client.records.ExerciseSessionRecord as HCExerciseSessionRecord

/**
 * Maps Health Connect exercise type integers to string values for API payload.
 * Note: Only includes exercise types available in the Health Connect SDK.
 */
object ExerciseTypeMapper {

    fun fromHealthConnect(exerciseType: Int): String {
        return when (exerciseType) {
            HCExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> "RUNNING"
            HCExerciseSessionRecord.EXERCISE_TYPE_WALKING -> "WALKING"
            HCExerciseSessionRecord.EXERCISE_TYPE_HIKING -> "HIKING"
            HCExerciseSessionRecord.EXERCISE_TYPE_BIKING -> "BIKING"
            HCExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL -> "SWIMMING_POOL"
            HCExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER -> "SWIMMING_OPEN_WATER"
            HCExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING -> "STRENGTH_TRAINING"
            HCExerciseSessionRecord.EXERCISE_TYPE_WEIGHTLIFTING -> "WEIGHTLIFTING"
            HCExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING -> "HIIT"
            HCExerciseSessionRecord.EXERCISE_TYPE_ELLIPTICAL -> "ELLIPTICAL"
            HCExerciseSessionRecord.EXERCISE_TYPE_ROWING_MACHINE -> "ROWING_MACHINE"
            HCExerciseSessionRecord.EXERCISE_TYPE_STAIR_CLIMBING -> "STAIR_CLIMBING"
            HCExerciseSessionRecord.EXERCISE_TYPE_RUNNING_TREADMILL -> "TREADMILL"
            HCExerciseSessionRecord.EXERCISE_TYPE_YOGA -> "YOGA"
            HCExerciseSessionRecord.EXERCISE_TYPE_PILATES -> "PILATES"
            HCExerciseSessionRecord.EXERCISE_TYPE_SKIING -> "SKIING"
            HCExerciseSessionRecord.EXERCISE_TYPE_TENNIS -> "TENNIS"
            HCExerciseSessionRecord.EXERCISE_TYPE_GOLF -> "GOLF"
            HCExerciseSessionRecord.EXERCISE_TYPE_ROCK_CLIMBING -> "ROCK_CLIMBING"
            HCExerciseSessionRecord.EXERCISE_TYPE_OTHER_WORKOUT -> "WORKOUT"
            else -> "OTHER"
        }
    }
}

