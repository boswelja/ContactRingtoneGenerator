package com.boswelja.contactringtonegenerator.settings.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.boswelja.contactringtonegenerator.common.MediaStoreHelper
import com.boswelja.contactringtonegenerator.contactpicker.ContactsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    var volumeBoostValue by mutableStateOf(
        sharedPreferences.getInt(VOLUME_BOOST_KEY, 0) / 10f
    )
    var multithreadedGeneration by mutableStateOf(
        sharedPreferences.getBoolean("multithreaded_generation", true)
    )

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

    fun updateVolumeBoost() {
        sharedPreferences.edit {
            putInt(VOLUME_BOOST_KEY, (volumeBoostValue * 10).toInt())
        }
    }

    fun updateMultithreadedGeneration() {
        sharedPreferences.edit {
            putBoolean(MULTITHREADED_GENERATION_KEY, multithreadedGeneration)
        }
    }

    companion object {
        const val MULTITHREADED_GENERATION_KEY = "multithreaded_generation"
        const val VOLUME_BOOST_KEY = "volume_boost"
    }
}
