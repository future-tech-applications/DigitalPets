package com.learning.companionshimejis

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.learning.companionshimejis.persistence.PreferencesManager
import com.learning.companionshimejis.data.PetRepository
import com.learning.companionshimejis.service.MainService
import com.learning.companionshimejis.ui.SettingsScreen
import com.learning.companionshimejis.ui.theme.CompanionShimejisTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { CompanionShimejisTheme { MainScreen() } }
    }
} // test changes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(initialScreen: String = "Home") {
    val context = LocalContext.current
    var hasPermission by rememberSaveable { mutableStateOf(false) }
    var currentScreen by rememberSaveable { mutableStateOf(initialScreen) }

    // Lifecycle observer to check permission onResume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
            topBar = {
                if (hasPermission) {
                    TopAppBar(
                            title = { Text(text = "Companion Shimejis") },
                            navigationIcon = {
                                if (currentScreen == "Settings") {
                                    IconButton(onClick = { currentScreen = "Home" }) {
                                        Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Back"
                                        )
                                    }
                                }
                            },
                            actions = {
                                if (currentScreen == "Home") {
                                    IconButton(onClick = { currentScreen = "Settings" }) {
                                        Icon(
                                                imageVector = Icons.Filled.Settings,
                                                contentDescription = "Settings"
                                        )
                                    }
                                }
                            }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (hasPermission) {
                when (currentScreen) {
                    "Home" -> HomeScreen()
                    "Settings" -> SettingsScreen()
                }
            } else {
                PermissionRequestScreen()
            }
        }
    }
}

@Composable
fun PermissionRequestScreen() {
    val context = LocalContext.current

    Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Permission Required", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
                text =
                        "To display pets over other apps, we need the 'Draw over other apps' permission.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
                onClick = {
                    val intent =
                            Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                            )
                    context.startActivity(intent)
                }
        ) { Text("Grant Permission") }
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isEnabled by PreferencesManager.isServiceEnabled(context).collectAsState(initial = false)
    val selectedPets by
            PreferencesManager.getSelectedPets(context).collectAsState(initial = emptySet())

    Column(modifier = Modifier.fillMaxSize()) {
        // Toggle Header
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                    text = "Start Pets Floating",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
            )
            Switch(
                    checked = isEnabled,
                    onCheckedChange = { checked ->
                        scope.launch(Dispatchers.IO) {
                            PreferencesManager.setServiceEnabled(context, checked)
                        }

                        if (checked) {
                            // Start Service
                            val intent = Intent(context, MainService::class.java)
                            intent.putStringArrayListExtra(
                                    MainService.EXTRA_PET_IDS,
                                    ArrayList(selectedPets.toList())
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(intent)
                            } else {
                                context.startService(intent)
                            }
                        } else {
                            // Stop Service
                            val intent = Intent(context, MainService::class.java)
                            intent.action = MainService.ACTION_STOP
                            context.startService(intent)
                        }
                    }
            )
        }

        // List
        Text(
                text = "Select Pets",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn {
            items(PetRepository.predefinedPets) { pet ->
                val isSelected = selectedPets.contains(pet.id)
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                val newSet = selectedPets.toMutableSet()
                                if (checked) {
                                    newSet.add(pet.id)
                                } else {
                                    newSet.remove(pet.id)
                                }

                                scope.launch(Dispatchers.IO) {
                                    PreferencesManager.setSelectedPets(context, newSet)
                                }

                                // If service is running, update it
                                if (isEnabled) {
                                    val intent = Intent(context, MainService::class.java)
                                    intent.putStringArrayListExtra(
                                            MainService.EXTRA_PET_IDS,
                                            ArrayList(newSet.toList())
                                    )
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        context.startForegroundService(intent)
                                    } else {
                                        context.startService(intent)
                                    }
                                }
                            }
                    )
                    Image(
                            painter = painterResource(id = pet.resId),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                    )
                    Text(
                            text = pet.name,
                            modifier = Modifier.padding(start = 16.dp),
                            style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

