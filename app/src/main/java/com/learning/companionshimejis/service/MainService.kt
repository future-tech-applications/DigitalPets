package com.learning.companionshimejis.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.learning.companionshimejis.animation.PetAnimationController
import com.learning.companionshimejis.animation.PetAnimationEngine
import com.learning.companionshimejis.manager.PetManager
import com.learning.companionshimejis.overlay.PetOptionsOverlayMenuManager
import com.learning.companionshimejis.overlay.PetWindowManager
import com.learning.companionshimejis.persistence.PetPreferencesObserver
import com.learning.companionshimejis.persistence.PetSessionManager
import com.learning.companionshimejis.persistence.PetState
import com.learning.companionshimejis.persistence.PreferencesManager
import com.learning.companionshimejis.physics.PetPhysicsController
import com.learning.companionshimejis.system.ScreenStateReceiver
import com.learning.companionshimejis.system.ServiceNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * #### <Service Lifecycle & Orchestration> Main Service which orchestrates all other components to
 * #### overlay and manage the characters/pets
 */
class MainService : Service() {

    // Components
    private lateinit var petWindowManager: PetWindowManager
    private lateinit var animationEngine: PetAnimationEngine
    private lateinit var petOptionsOverlayMenuManager: PetOptionsOverlayMenuManager
    private lateinit var physicsController: PetPhysicsController
    private lateinit var animationController: PetAnimationController
    private lateinit var sessionManager: PetSessionManager
    private lateinit var notificationHelper: ServiceNotificationHelper
    private lateinit var petManager: PetManager
    private lateinit var preferencesObserver: PetPreferencesObserver

    private var isDeviceScreenOn = true
    private var petAnimationSpeedMultiplier = 1.0f
    private var petCurrentScale = 1.0f
    private var petCurrentOpacity = 1.0f

    companion object {
        const val CHANNEL_ID = "overlay_channel"
        const val EXTRA_PET_IDS = "extra_pet_ids"
        const val ACTION_STOP = "stop_service"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /** ## System Part (Screen State) ## */
    /** Listens for screen state changes and starts/stops the service accordingly. */
    private val screenReceiver =
            ScreenStateReceiver(
                    onScreenOn = {
                        isDeviceScreenOn = true
                        startAnimation()
                    },
                    onScreenOff = {
                        isDeviceScreenOn = false
                        stopAnimation()
                    }
            )

    override fun onCreate() {
        super.onCreate()

        // Initialize Components
        petWindowManager = PetWindowManager(this)
        physicsController = PetPhysicsController(petWindowManager)
        animationController = PetAnimationController(this)
        sessionManager = PetSessionManager(this)
        notificationHelper = ServiceNotificationHelper(this)
        animationEngine = PetAnimationEngine {
            physicsController.updatePhysics(petManager.activePets, petAnimationSpeedMultiplier)
            animationController.updateAnimations(petManager.activePets, petAnimationSpeedMultiplier)
        }
        // Initialize Pet Options Menu Manager
        petOptionsOverlayMenuManager =
                PetOptionsOverlayMenuManager(
                        this,
                        petWindowManager,
                        object : PetOptionsOverlayMenuManager.Callback {
                            override fun onDismissPet(petState: PetState) {
                                petManager.removePet(petState)
                            }
                            override fun onSnooze() {
                                snoozePets()
                            }
                            override fun onOpenSettings() {
                                val intent =
                                        Intent(
                                                this@MainService,
                                                com.learning.companionshimejis.MainActivity::class
                                                        .java
                                        )
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                intent.putExtra("DESTINATION", "Settings")
                                startActivity(intent)
                            }
                            override fun onStopApp() {
                                val scope = CoroutineScope(Dispatchers.IO)
                                scope.launch {
                                    PreferencesManager.setServiceEnabled(this@MainService, false)
                                    stopSelf()
                                }
                            }
                        }
                )

        // Initialize Pet Manager with callbacks
        petManager =
                PetManager(
                        this,
                        petWindowManager,
                        petOptionsOverlayMenuManager,
                        onPetCountChanged = { isNotEmpty ->
                            if (isNotEmpty && isDeviceScreenOn) startAnimation()
                            else stopAnimation()
                        },
                        onPetRemoved = { petState ->
                            // Sync with Persistence
                            val scope = CoroutineScope(Dispatchers.IO)
                            scope.launch {
                                PreferencesManager.setPetPosition(
                                        this@MainService,
                                        petState.id,
                                        petState.x,
                                        petState.y
                                )

                                PreferencesManager.getSelectedPets(this@MainService).collect {
                                        currentSet ->
                                    val newSet = currentSet.toMutableSet()
                                    if (newSet.remove(petState.id)) {
                                        PreferencesManager.setSelectedPets(this@MainService, newSet)
                                    }
                                    cancel()
                                }
                            }
                        }
                )

        notificationHelper.createNotificationChannel()
        notificationHelper.startForeground()

        screenReceiver.register(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            runBlocking { sessionManager.saveAllPetPositions(petManager.activePets) }
            stopSelf()
            return START_NOT_STICKY
        }

        // Reset session if manual start, otherwise restore
        if (intent?.getStringArrayListExtra(EXTRA_PET_IDS) != null) {
            runBlocking { sessionManager.saveAllPetPositions(petManager.activePets) }
            petManager.removeAllPets()
        } else if (petManager.activePets.isNotEmpty()) {
            // Already running? Maybe saving is redundant here if we just continue,
            // but let's safe guard.
            runBlocking { sessionManager.saveAllPetPositions(petManager.activePets) }
        }

        sessionManager.restoreSession(
                intent,
                CoroutineScope(Dispatchers.Main),
                onPetRestored = { restored ->
                    petManager.addPet(
                            restored.pet,
                            restored.position,
                            petCurrentScale,
                            petCurrentOpacity
                    )
                },
                onSessionEmpty = { stopSelf() }
        )

        preferencesObserver = PetPreferencesObserver(this)

        // Observe Pet Settings Changes
        val scope = CoroutineScope(Dispatchers.Main)
        preferencesObserver.observeChanges(
                scope,
                object : PetPreferencesObserver.Callback {
                    override fun onScaleChanged(scale: Float) {
                        petCurrentScale = scale
                        petManager.updatePetsScale(scale)
                    }
                    override fun onOpacityChanged(opacity: Float) {
                        petCurrentOpacity = opacity
                        petManager.updatePetsOpacity(opacity)
                    }
                    override fun onSpeedChanged(speed: Float) {
                        petAnimationSpeedMultiplier = speed
                    }
                }
        )

        return START_STICKY
    }

    private fun startAnimation() {
        if (petManager.activePets.isNotEmpty() && isDeviceScreenOn) {
            animationEngine.start()
        }
    }

    private fun stopAnimation() {
        animationEngine.stop()
    }

    private fun snoozePets() {
        stopAnimation()
        // Hide all pets
        petManager.setPetsVisible(false)

        // Schedule restore
        android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed(
                        {
                            petManager.setPetsVisible(true)
                            startAnimation()
                        },
                        30 * 60 * 1000L
                ) // 30 minutes
    }

    override fun onDestroy() {
        screenReceiver.unregister(this)
        runBlocking { sessionManager.saveAllPetPositions(petManager.activePets) }
        //        isRunning = false
        //        animationHandler.removeCallbacks(animationRunnable)
        stopAnimation()
        petManager.removeAllPets()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        runBlocking { sessionManager.saveAllPetPositions(petManager.activePets) }
        // Removed stopSelf() to allow START_STICKY to restart service
        // stopSelf()
    }
}
