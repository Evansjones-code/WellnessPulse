package com.example.wellnesspulse

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_logs")
data class HealthLogEntity(
    @PrimaryKey
    val date: String, // YYYY-MM-DD
    val steps: Long = 0L,
    val sleepHours: Double = 0.0,
    val walkingKm: Double = 0.0,
    val runningKm: Double = 0.0,
    val hikingKm: Double = 0.0,
    val swimmingKm: Double = 0.0,
    val bikingKm: Double = 0.0,
    val ellipticalKm: Double = 0.0,
    val strengthTrainingMinutes: Int = 0 // For circuit training and weight machines
)