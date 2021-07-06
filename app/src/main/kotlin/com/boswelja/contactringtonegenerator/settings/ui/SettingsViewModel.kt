package com.boswelja.contactringtonegenerator.settings.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.contactringtonegenerator.common.MediaStoreHelper
import com.boswelja.contactringtonegenerator.contactpicker.ContactsHelper
import com.boswelja.contactringtonegenerator.settings.settingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.settingsDataStore

    val volumeMultiplier = dataStore.data.map { it.volumeMultiplier }

    @ExperimentalCoroutinesApi
    fun resetContactRingtones() {
        val context = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            ContactsHelper.getContacts(
                context.contentResolver,
                Int.MAX_VALUE // Collect all contacts
            ).first().forEach { contact ->
                ContactsHelper.removeContactRingtone(context, contact)
            }
            MediaStoreHelper.deleteAllRingtones(context)
        }
    }

    fun setVolumeMultiplier(newMultiplier: Float) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(volumeMultiplier = newMultiplier) }
        }
    }
}