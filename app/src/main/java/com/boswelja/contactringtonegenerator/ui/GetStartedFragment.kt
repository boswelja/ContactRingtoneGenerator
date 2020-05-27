package com.boswelja.contactringtonegenerator.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
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
                val action = if (context?.checkSelfPermission(Manifest.permission.WRITE_CONTACTS)
                        == PackageManager.PERMISSION_GRANTED){
                    GetStartedFragmentDirections.toContactPickerFragment()
                } else {
                    GetStartedFragmentDirections.toPermissionFragment(Manifest.permission.WRITE_CONTACTS)
                }
                findNavController().navigate(action)
            }
            advancedModeButton.setOnClickListener(Navigation.createNavigateOnClickListener(GetStartedFragmentDirections.toAdvancedModeFragment()))
        }
    }
}