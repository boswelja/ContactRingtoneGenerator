package com.boswelja.contactringtonegenerator.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.boswelja.contactringtonegenerator.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)
    }
}