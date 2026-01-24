package com.learning.companionshimejis.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.learning.companionshimejis.persistence.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val scale by PreferencesManager.getPetScale(context).collectAsState(initial = 1.0f)
    val opacity by PreferencesManager.getPetOpacity(context).collectAsState(initial = 1.0f)
    val speed by PreferencesManager.getAnimationSpeed(context).collectAsState(initial = 1.0f)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
                text = "Customize Experience",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
        )

        // Scale Slider
        Text(
                text = "Pet Size: ${(scale * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium
        )
        Slider(
                value = scale,
                onValueChange = { newScale ->
                    scope.launch(Dispatchers.IO) {
                        PreferencesManager.setPetScale(context, newScale)
                    }
                },
                valueRange = 0.5f..2.0f,
                steps = 14 // Steps of 0.1
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Opacity Slider
        Text(
                text = "Opacity: ${(opacity * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium
        )
        Slider(
                value = opacity,
                onValueChange = { newOpacity ->
                    scope.launch(Dispatchers.IO) {
                        PreferencesManager.setPetOpacity(context, newOpacity)
                    }
                },
                valueRange = 0.2f..1.0f,
                steps = 7 // Steps of 0.1
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Speed Slider
        Text(
                text = "Animation Speed: ${String.format("%.1fx", speed)}",
                style = MaterialTheme.typography.titleMedium
        )
        Slider(
                value = speed,
                onValueChange = { newSpeed ->
                    scope.launch(Dispatchers.IO) {
                        PreferencesManager.setAnimationSpeed(context, newSpeed)
                    }
                },
                valueRange = 0.5f..3.0f,
                steps = 4 // 0.5, 1.0, 1.5, 2.0, 2.5, 3.0
        )
    }
}
