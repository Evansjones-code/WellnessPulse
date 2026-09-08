package com.example.wellnesspulse

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AthleteProfileScreen(
    settingsDataStore: SettingsDataStore,
    onNavigateToTracker: () -> Unit,
    onBackClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val isDarkMode by settingsDataStore.darkModeFlow.collectAsState(initial = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Athlete Profile", fontWeight = FontWeight.Bold, color = StravaOrange) },
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Evans Kotut Kiplagat",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = StravaOrange
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Data Operations & Systems Support Engineer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Dark Mode Theme", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { enabled ->
                            coroutineScope.launch {
                                settingsDataStore.setDarkMode(enabled)
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = StravaOrange)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToTracker,
                colors = ButtonDefaults.buttonColors(containerColor = StravaOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Open Live GPS Tracker", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}