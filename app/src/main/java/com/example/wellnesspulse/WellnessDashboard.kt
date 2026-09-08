package com.example.wellnesspulse

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

val StravaOrange = Color(0xFFFC4C02)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessDashboard(
    healthConnectClient: HealthConnectClient,
    healthDao: HealthDao,
    permissions: Set<String>,
    onRequestPermissions: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var stepCount by remember { mutableStateOf(0L) }
    var sleepDurationHours by remember { mutableStateOf(0.0) }
    var walkingKm by remember { mutableStateOf(0.0) }
    var runningKm by remember { mutableStateOf(0.0) }
    var hikingKm by remember { mutableStateOf(0.0) }
    var statusMessage by remember { mutableStateOf("Ready to sync activities") }

    val savedLogs by healthDao.getAllLogs().collectAsState(initial = emptyList())
    val customStepGoal = 10000L

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "WellnessPulse",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = StravaOrange
                    )
                },
                actions = {
                    TextButton(onClick = onNavigateToProfile) {
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = StravaOrange
                        )
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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics Grid Cards Row 1 (Steps & Sleep)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "STEPS", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "$stepCount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "SLEEP", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "%.1fh".format(sleepDurationHours), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Metrics Grid Cards Row 2 (Walk, Run, Hike)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "WALK", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(text = "%.1f km".format(walkingKm), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "RUN", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(text = "%.1f km".format(runningKm), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "HIKE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(text = "%.1f km".format(hikingKm), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Daily Goal Progress Card
            val progress = (stepCount.toFloat() / customStepGoal).coerceIn(0f, 1f)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Daily Step Goal ($customStepGoal)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text(text = "${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = StravaOrange)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = StravaOrange,
                        trackColor = Color.LightGray.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Weekly Trends Interactive Bar Chart Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "7-Day Step Trend",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = StravaOrange
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val recentLogs = savedLogs.takeLast(7)
                    if (recentLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Sync activities to populate trends", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    } else {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        ) {
                            val maxStepsVal = recentLogs.maxOfOrNull { it.steps }?.coerceAtLeast(1000L) ?: customStepGoal
                            val barWidth = size.width / (7 * 2)
                            val spacing = barWidth

                            recentLogs.forEachIndexed { index, log ->
                                val barHeight = (log.steps.toFloat() / maxStepsVal) * size.height
                                val x = index * (barWidth + spacing) + spacing / 2
                                val y = size.height - barHeight

                                drawRect(
                                    color = StravaOrange,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = StravaOrange),
                onClick = {
                    coroutineScope.launch {
                        try {
                            val granted = healthConnectClient.permissionController.getGrantedPermissions()
                            if (granted.containsAll(permissions)) {
                                val endTime = Instant.now()
                                val startTime = endTime.minus(1, ChronoUnit.DAYS)
                                val timeFilter = TimeRangeFilter.between(startTime, endTime)

                                // Read Steps
                                val stepResponse = healthConnectClient.readRecords(
                                    ReadRecordsRequest(recordType = StepsRecord::class, timeRangeFilter = timeFilter)
                                )
                                stepCount = stepResponse.records.sumOf { it.count }

                                // Read Sleep Sessions
                                val sleepResponse = healthConnectClient.readRecords(
                                    ReadRecordsRequest(recordType = SleepSessionRecord::class, timeRangeFilter = timeFilter)
                                )
                                val totalMinutes = sleepResponse.records.sumOf { session ->
                                    Duration.between(session.startTime, session.endTime).toMinutes()
                                }
                                sleepDurationHours = totalMinutes / 60.0

                                // Read Exercise Sessions
                                val exerciseResponse = healthConnectClient.readRecords(
                                    ReadRecordsRequest(recordType = ExerciseSessionRecord::class, timeRangeFilter = timeFilter)
                                )

                                var walkDist = 0.0
                                var runDist = 0.0
                                var hikeDist = 0.0

                                exerciseResponse.records.forEach { session ->
                                    when (session.exerciseType) {
                                        ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> walkDist += 2.0
                                        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> runDist += 5.0
                                        ExerciseSessionRecord.EXERCISE_TYPE_HIKING -> hikeDist += 4.0
                                    }
                                }

                                walkingKm = walkDist
                                runningKm = runDist
                                hikingKm = hikeDist

                                // Save to Local Room Database
                                val today = LocalDate.now().toString()
                                healthDao.insertLog(
                                    HealthLogEntity(
                                        date = today,
                                        steps = stepCount,
                                        sleepHours = sleepDurationHours,
                                        walkingKm = walkingKm,
                                        runningKm = runningKm,
                                        hikingKm = hikingKm
                                    )
                                )

                                statusMessage = "Synced activities successfully"
                            } else {
                                statusMessage = "Permissions required"
                                onRequestPermissions()
                            }
                        } catch (e: Exception) {
                            statusMessage = "Error: ${e.localizedMessage}"
                        }
                    }
                }
            ) {
                Text(text = "Sync & Save Activity", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(text = "Activity Feed", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(savedLogs) { log ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDetail(log.date) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = log.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "Wellness Log", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${log.steps} steps | %.1fh sleep".format(log.sleepHours),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = StravaOrange
                                )
                                Text(
                                    text = "W: %.1fkm | R: %.1fkm | H: %.1fkm".format(log.walkingKm, log.runningKm, log.hikingKm),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}