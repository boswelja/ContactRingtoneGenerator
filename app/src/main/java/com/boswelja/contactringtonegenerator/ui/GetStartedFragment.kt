package com.boswelja.contactringtonegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.boswelja.contactringtonegenerator.databinding.FragmentGetStartedBinding

class GetStartedFragment : Fragment() {

    private lateinit var binding: FragmentGetStartedBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGetStartedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            getStartedButton.setOnClickListener(Navigation.createNavigateOnClickListener(GetStartedFragmentDirections.toContactPickerFragment()))
            advancedModeButton.setOnClickListener(Navigation.createNavigateOnClickListener(GetStartedFragmentDirections.toAdvancedModeFragment()))
        }
    }
}