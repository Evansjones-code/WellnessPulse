package com.example.wellnesspulse

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.metadata.Metadata
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveTrackerScreen(
    workoutType: WorkoutType,
    healthDao: HealthDao,
    healthConnectClient: HealthConnectClient,
    onBackToSelection: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var isTracking by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var distanceKm by remember { mutableStateOf(0.0) }

    LaunchedEffect(isTracking) {
        if (isTracking) {
            while (true) {
                kotlinx.coroutines.delay(1000L)
                elapsedSeconds++
                distanceKm += 0.002 // Simulated live GPS increment
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workoutType.title, fontWeight = FontWeight.Bold, color = StravaOrange) },
                navigationIcon = {
                    TextButton(onClick = onBackToSelection) {
                        Text("Change", color = StravaOrange, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "SESSION TIMER", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        text = "%02d:%02d:%02d".format(elapsedSeconds / 3600, (elapsedSeconds % 3600) / 60, elapsedSeconds % 60),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = StravaOrange
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "DISTANCE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = "%.2f km".format(distanceKm), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "EST. PACE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = "5'12\" /km", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isTracking) {
                        isTracking = false
                        scope.launch {
                            val todayDate = LocalDate.now().toString()

                            val existingLog = healthDao.getLogByDate(todayDate).first() ?: HealthLogEntity(
                                date = todayDate,
                                steps = 0L,
                                sleepHours = 0.0,
                                walkingKm = 0.0,
                                runningKm = 0.0,
                                hikingKm = 0.0,
                                swimmingKm = 0.0,
                                bikingKm = 0.0,
                                ellipticalKm = 0.0,
                                strengthTrainingMinutes = 0
                            )

                            val updatedLog = when (workoutType) {
                                WorkoutType.POOL_SWIM -> existingLog.copy(swimmingKm = existingLog.swimmingKm + distanceKm)
                                WorkoutType.TREADMILL -> existingLog.copy(runningKm = existingLog.runningKm + distanceKm)
                                WorkoutType.BIKE -> existingLog.copy(bikingKm = existingLog.bikingKm + distanceKm)
                                WorkoutType.ELLIPTICAL -> existingLog.copy(ellipticalKm = existingLog.ellipticalKm + distanceKm)
                                WorkoutType.CIRCUIT_TRAINING, WorkoutType.WEIGHT_MACHINES ->
                                    existingLog.copy(strengthTrainingMinutes = existingLog.strengthTrainingMinutes + (elapsedSeconds / 60))
                                WorkoutType.OTHER -> existingLog.copy(walkingKm = existingLog.walkingKm + distanceKm)
                            }

                            healthDao.insertLog(updatedLog)

                            try {
                                val endTime = Instant.now()
                                val startTime = endTime.minusSeconds(elapsedSeconds.toLong())
                                val exerciseRecord = ExerciseSessionRecord(
                                    startTime = startTime,
                                    startZoneOffset = ZoneOffset.systemDefault().rules.getOffset(startTime),
                                    endTime = endTime,
                                    endZoneOffset = ZoneOffset.systemDefault().rules.getOffset(endTime),
                                    metadata = Metadata.manualEntry(),
                                    exerciseType = workoutType.healthConnectValue,
                                    title = workoutType.title
                                )
                                healthConnectClient.insertRecords(listOf(exerciseRecord))
                            } catch (e: Exception) {
                                // Catch permission or insertion issues safely
                            }
                        }
                        onBackToSelection()
                    } else {
                        isTracking = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isTracking) Color.Red else StravaOrange),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(text = if (isTracking) "Finish & Stop Workout" else "Start Live Tracking", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    val shareText = "I just completed a %.2f km ${workoutType.title} workout on WellnessPulse!".format(distanceKm)
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(text = "Share Milestone Summary", color = StravaOrange, fontWeight = FontWeight.Bold)
            }
        }
    }
}