package com.boswelja.contactringtonegenerator.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SeekBarPreference
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.contacts.ContactsHelper
import com.boswelja.contactringtonegenerator.mediastore.MediaStoreHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SettingsFragment :
    PreferenceFragmentCompat(),
    Preference.OnPreferenceClickListener,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val coroutineScope = MainScope()
    private val volumeBoostPreference: SeekBarPreference by lazy { findPreference(VOLUME_BOOST_KEY)!! }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            VOLUME_BOOST_KEY -> updateVolumeBoostSummary()
        }
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            RESET_RINGTONES_KEY -> {
                resetContactRingtones()
                true
            }
            LAUNCH_TTS_SETTINGS_KEY -> {
                launchTtsSettings()
                true
            }
            else -> false
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)
        findPreference<Preference>(RESET_RINGTONES_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(LAUNCH_TTS_SETTINGS_KEY)!!.onPreferenceClickListener = this
        volumeBoostPreference.apply {
            updatesContinuously = true
        }
        updateVolumeBoostSummary()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun launchTtsSettings() {
        Intent().apply {
            action = "com.android.settings.TTS_SETTINGS"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }.also {
            startActivity(it)
        }
    }

    private fun updateVolumeBoostSummary() {
        volumeBoostPreference.summary = getString(R.string.volume_boost_summary, calculateVolumeMultiplier())
    }

    private fun calculateVolumeMultiplier(): Float {
        val userBoost = PreferenceManager.getDefaultSharedPreferences(context).getInt("volume_boost", 0)
        val baseVolume = 10
        return (baseVolume + userBoost) / 10f
    }

    private fun resetContactRingtones() {
        coroutineScope.launch(Dispatchers.IO) {
            ContactsHelper.getContacts(requireContext()).forEach {
                ContactsHelper.removeContactRingtone(requireContext(), it)
            }
            MediaStoreHelper.deleteAllRingtones(requireContext())
        }
    }

    companion object {
        private const val RESET_RINGTONES_KEY = "reset_ringtones"
        private const val LAUNCH_TTS_SETTINGS_KEY = "launch_tts_settings"

        const val VOLUME_BOOST_KEY = "volume_boost"
    }
}
