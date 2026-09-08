package com.example.wellnesspulse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTrackerScreen(
    healthDao: HealthDao,
    healthConnectClient: HealthConnectClient,
    onBackClick: () -> Unit
) {
    var selectedWorkout by remember { mutableStateOf<WorkoutType?>(null) }

    if (selectedWorkout == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Select Workout Type", fontWeight = FontWeight.Bold, color = StravaOrange) },
                    navigationIcon = {
                        TextButton(onClick = onBackClick) {
                            Text("Back", color = StravaOrange, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        ) { innerPadding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(WorkoutType.entries) { workout ->
                    Card(
                        onClick = { selectedWorkout = workout },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = workout.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    } else {
        ActiveTrackerScreen(
            workoutType = selectedWorkout!!,
            healthDao = healthDao,
            healthConnectClient = healthConnectClient,
            onBackToSelection = { selectedWorkout = null }
        )
    }
}