package com.example.wellnesspulse

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class HealthSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val healthClient = HealthConnectClient.getOrCreate(applicationContext)
            val dao = HealthDatabase.getDatabase(applicationContext).healthDao()

            val endTime = Instant.now()
            val startTime = endTime.minus(1, ChronoUnit.DAYS)
            val timeFilter = TimeRangeFilter.between(startTime, endTime)

            val stepResponse = healthClient.readRecords(
                ReadRecordsRequest(recordType = StepsRecord::class, timeRangeFilter = timeFilter)
            )
            val totalSteps = stepResponse.records.sumOf { it.count }

            val sleepResponse = healthClient.readRecords(
                ReadRecordsRequest(recordType = SleepSessionRecord::class, timeRangeFilter = timeFilter)
            )
            val totalSleepMins = sleepResponse.records.sumOf { session ->
                java.time.Duration.between(session.startTime, session.endTime).toMinutes()
            }
            val sleepHours = totalSleepMins / 60.0

            val exerciseResponse = healthClient.readRecords(
                ReadRecordsRequest(recordType = ExerciseSessionRecord::class, timeRangeFilter = timeFilter)
            )

            var walk = 0.0
            var run = 0.0
            var hike = 0.0

            exerciseResponse.records.forEach { session ->
                when (session.exerciseType) {
                    ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> walk += 2.0
                    ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> run += 5.0
                    ExerciseSessionRecord.EXERCISE_TYPE_HIKING -> hike += 4.0
                }
            }

            dao.insertLog(
                HealthLogEntity(
                    date = LocalDate.now().toString(),
                    steps = totalSteps,
                    sleepHours = sleepHours,
                    walkingKm = walk,
                    runningKm = run,
                    hikingKm = hike
                )
            )

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}