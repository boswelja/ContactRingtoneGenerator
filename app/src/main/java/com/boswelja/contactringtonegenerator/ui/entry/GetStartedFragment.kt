package com.boswelja.contactringtonegenerator.ui.entry

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.boswelja.contactringtonegenerator.databinding.FragmentGetStartedBinding
import com.boswelja.contactringtonegenerator.ui.GetStartedFragmentDirections
import com.boswelja.contactringtonegenerator.ui.PermissionSheet

class GetStartedFragment : Fragment() {

    private lateinit var binding: FragmentGetStartedBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGetStartedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            getStartedButton.setOnClickListener {
                getStarted()
            }
            settingsButton.setOnClickListener {
                navigateToSettings()
            }
        }
    }

    /**
     * Navigate to Settings.
     */
    private fun navigateToSettings() {
        findNavController().navigate(GetStartedFragmentDirections.toSettingsFragment())
    }

    /**
     * Starts the generator flow, or asks for permission if missing first.
     */
    private fun getStarted() {
        if (hasContactPermissions()) {
            findNavController().navigate(GetStartedFragmentDirections.toContactPickerFragment())
        } else {
            PermissionSheet().show(childFragmentManager, "PermissionSheet")
        }
    }

    /**
     * Checks whether this app has READ_CONTACTS or WRITE_CONTACTS.
     * @return true if we have both permissions, false otherwise.
     */
    private fun hasContactPermissions(): Boolean =
        context?.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
            context?.checkSelfPermission(Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED
}
