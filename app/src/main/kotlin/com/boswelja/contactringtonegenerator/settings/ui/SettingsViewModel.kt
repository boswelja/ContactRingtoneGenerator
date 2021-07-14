package com.boswelja.contactringtonegenerator.settings.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.contactringtonegenerator.common.mediastore.deleteGeneratedRingtones
import com.boswelja.contactringtonegenerator.common.contacts.ContactsHelper
import com.boswelja.contactringtonegenerator.settings.settingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.settingsDataStore

    var volumeMultiplier by mutableStateOf(1.0f)
    var ttsVoicePitch by mutableStateOf(1.0f)
    var ttsSpeechRate by mutableStateOf(1.0f)

    init {
        viewModelScope.launch {
            dataStore.data.take(1).collect {
                volumeMultiplier = it.volumeMultiplier
                ttsVoicePitch = it.ttsPitch
                ttsSpeechRate = it.ttsSpeechRate
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun resetContactRingtones() {
        val context = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            ContactsHelper.getContacts(
                context.contentResolver
            ).first().forEach { contact ->
                ContactsHelper.removeContactRingtone(context, contact)
            }
            context.contentResolver.deleteGeneratedRingtones()
        }
    }

    fun saveVolumeMultiplier(newMultiplier: Float) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(volumeMultiplier = newMultiplier) }
        }
    }

    fun saveTtsVoicePitch(newPitch: Float) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(ttsPitch = newPitch) }
        }
    }

    fun saveTtsSpeechRate(newRate: Float) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(ttsSpeechRate = newRate) }
        }
    }
}
