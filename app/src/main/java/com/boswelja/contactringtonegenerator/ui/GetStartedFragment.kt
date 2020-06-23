package com.boswelja.contactringtonegenerator.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.boswelja.contactringtonegenerator.databinding.FragmentGetStartedBinding

class GetStartedFragment : Fragment() {

    private lateinit var binding: FragmentGetStartedBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGetStartedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            getStartedButton.setOnClickListener {
                if (context?.checkSelfPermission(Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    findNavController().navigate(GetStartedFragmentDirections.toContactPickerFragment())
                } else {
                    PermissionSheet().show(childFragmentManager, "PermissionSheet")
                }
            }
            settingsButton.setOnClickListener {
                findNavController().navigate(GetStartedFragmentDirections.toSettingsFragment())
            }
        }
        val activity = requireActivity()
        if (activity is MainActivity) {
            activity.removeTitle()
        }
    }
}
