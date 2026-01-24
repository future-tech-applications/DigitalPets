package com.learning.companionshimejis.persistence

import android.content.Context
import android.content.Intent
import com.learning.companionshimejis.data.PetRepository
import com.learning.companionshimejis.data.model.Pet
import com.learning.companionshimejis.service.MainService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Helps in saving and restoring the last known position and state of the pets.
 */
class PetSessionManager(private val context: Context) {

    data class RestoredPet(val pet: Pet, val position: Pair<Int, Int>?)

    suspend fun saveAllPetPositions(activePets: List<PetState>) {
        activePets.forEach { pet ->
            PreferencesManager.setPetPosition(context, pet.id, pet.x, pet.y)
        }
    }

    fun restoreSession(
            intent: Intent?,
            scope: CoroutineScope,
            onPetRestored: (RestoredPet) -> Unit,
            onSessionEmpty: () -> Unit
    ) {
        val petIdsArg = intent?.getStringArrayListExtra(MainService.EXTRA_PET_IDS)

        if (petIdsArg != null) {
            // Started manually with new list
            petIdsArg.forEach { id ->
                val pet = PetRepository.getPetById(id)
                if (pet != null) {
                    scope.launch(Dispatchers.Main) {
                        val pos = PreferencesManager.getPetPosition(context, id).first()
                        onPetRestored(RestoredPet(pet, pos))
                    }
                }
            }
        } else {
            // Restarted by system or no args - load from DataStore
            scope.launch(Dispatchers.Main) {
                PreferencesManager.getSelectedPets(context).collect { storedIds ->
                    // Stop collecting after first emission to avoid constant updates here during
                    // this session
                    // We only want initial restoration.
                    cancel()

                    if (storedIds.isNotEmpty()) {
                        storedIds.forEach { id ->
                            val pet = PetRepository.getPetById(id)
                            if (pet != null) {
                                val pos = PreferencesManager.getPetPosition(context, id).first()
                                onPetRestored(RestoredPet(pet, pos))
                            }
                        }
                    } else {
                        onSessionEmpty()
                    }
                }
            }
        }
    }
}
