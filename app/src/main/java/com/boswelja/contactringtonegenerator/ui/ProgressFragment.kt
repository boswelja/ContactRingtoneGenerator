package com.boswelja.contactringtonegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.boswelja.contactringtonegenerator.databinding.FragmentLoadingBinding
import com.boswelja.contactringtonegenerator.ringtonegen.RingtoneGenerator
import com.boswelja.contactringtonegenerator.tts.SynthesisResult

class ProgressFragment : Fragment(), RingtoneGenerator.ProgressListener {

    private lateinit var binding: FragmentLoadingBinding

    override fun onGenerateStarted(totalJobCount: Int) {
        binding.progressBar.apply {
            max = totalJobCount
            progress = 0
            secondaryProgress = 0
        }
    }

    override fun onGenerateFinished() {

    }

    override fun onJobStarted() {

    }

    override fun onJobCompleted(success: Boolean, synthesisResult: SynthesisResult) {
        binding.progressBar.apply {
            if (success) {
                progress += 1
                secondaryProgress += 1
            } else {
                secondaryProgress += 1
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity()
        if (activity is MainActivity) {
            activity.removeTitle()
        }
    }
}
