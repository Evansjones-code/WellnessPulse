package com.example.wellnesspulse

import androidx.health.connect.client.records.ExerciseSessionRecord

enum class WorkoutType(val title: String, val healthConnectValue: Int) {
    POOL_SWIM("Pool Swim", ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL),
    TREADMILL("Treadmill", ExerciseSessionRecord.EXERCISE_TYPE_RUNNING_TREADMILL),
    BIKE("Bike", ExerciseSessionRecord.EXERCISE_TYPE_BIKING),
    ELLIPTICAL("Elliptical Trainer", ExerciseSessionRecord.EXERCISE_TYPE_ELLIPTICAL),
    CIRCUIT_TRAINING("Circuit Training", ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING),
    WEIGHT_MACHINES("Weight Machines", ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING),
    OTHER("Other Workouts", ExerciseSessionRecord.EXERCISE_TYPE_OTHER_WORKOUT)
}