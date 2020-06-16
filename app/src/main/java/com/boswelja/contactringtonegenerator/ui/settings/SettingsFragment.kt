package com.boswelja.contactringtonegenerator.ui.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.contacts.ContactsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SettingsFragment :
        PreferenceFragmentCompat(),
        Preference.OnPreferenceClickListener {

    private val coroutineScope = MainScope()

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            RESET_RINGTONES_KEY -> {
                resetContactRingtones()
                true
            }
            else -> false
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addResetPreferences()
    }

    private fun addResetPreferences() {
        addPreferencesFromResource(R.xml.settings)
        findPreference<Preference>(RESET_RINGTONES_KEY)!!.onPreferenceClickListener = this
    }

    private fun resetContactRingtones() {
        coroutineScope.launch(Dispatchers.IO) {
            ContactsHelper.getContacts(requireContext()).forEach {
                ContactsHelper.removeContactRingtone(requireContext(), it)
            }
        }
    }

    companion object {
        private const val RESET_RINGTONES_KEY = "reset_ringtones"
    }
}
