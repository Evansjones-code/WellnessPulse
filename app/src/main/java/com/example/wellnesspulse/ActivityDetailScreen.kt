package com.example.wellnesspulse

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    date: String,
    healthDao: HealthDao,
    onBackClick: () -> Unit
) {
    val log by healthDao.getLogByDate(date).collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Details ($date)", fontWeight = FontWeight.Bold, color = StravaOrange) },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("Back", color = StravaOrange, fontWeight = FontWeight.Bold)
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
            if (log == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Loading details...", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            } else {
                val detailLog = log!!
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Date: ${detailLog.date}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = StravaOrange
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        DetailRow(label = "Steps Recorded", value = "${detailLog.steps}")
                        DetailRow(label = "Sleep Duration", value = "%.1f hours".format(detailLog.sleepHours))
                        DetailRow(label = "Walking Distance", value = "%.1f km".format(detailLog.walkingKm))
                        DetailRow(label = "Running Distance", value = "%.1f km".format(detailLog.runningKm))
                        DetailRow(label = "Hiking Distance", value = "%.1f km".format(detailLog.hikingKm))
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
    }
}