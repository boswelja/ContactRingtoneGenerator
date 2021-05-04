package com.boswelja.contactringtonegenerator.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.databinding.FragmentProgressBinding
import com.boswelja.contactringtonegenerator.ringtonegen.RingtoneGenerator
import com.boswelja.contactringtonegenerator.ui.WizardViewModel

class ProgressFragment : Fragment() {

    private val wizardViewModel: WizardViewModel by activityViewModels()

    private lateinit var binding: FragmentProgressBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        wizardViewModel.generatorState.observe(viewLifecycleOwner) { state ->
            when (state) {
                RingtoneGenerator.State.NOT_READY -> wizardViewModel.initialiseGenerator()
                RingtoneGenerator.State.READY -> wizardViewModel.startGenerating()
                RingtoneGenerator.State.GENERATING -> onGenerateStarted()
                RingtoneGenerator.State.FINISHED -> navigateNext()
                else -> {} // Do nothing
            }
        }

        wizardViewModel.successCount.observe(viewLifecycleOwner) {
            binding.progressBar.progress = it
        }
        wizardViewModel.startedJobCount.observe(viewLifecycleOwner) {
            binding.progressBar.secondaryProgress = it
        }
    }

    private fun navigateNext() {
        findNavController().navigate(
            ProgressFragmentDirections.toFinishedFragment(
                wizardViewModel.successCount.value!!,
                wizardViewModel.failCount.value!!
            )
        )
    }

    private fun onGenerateStarted() {
        binding.apply {
            progressBar.apply {
                isIndeterminate = false
                progress = 0
                secondaryProgress = 0
                max = wizardViewModel.totalJobCount
            }
            loadingTitle.text = getString(R.string.progress_title_generating)
            loadingStatus.text = getString(R.string.progress_status_generating)
        }
    }
}
