package com.boswelja.contactringtonegenerator.common.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.WizardViewModel
import com.boswelja.contactringtonegenerator.ringtonegen.RingtoneGenerator

class ProgressFragment : Fragment() {

    private val wizardViewModel: WizardViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    val progress by wizardViewModel.successCount.observeAsState()
                    val generatorState by wizardViewModel.generatorState.observeAsState()
                    ProgressScreen(
                        indeterminate = generatorState == RingtoneGenerator.State.NOT_READY,
                        progress = ((progress?.toFloat() ?: 0f) / wizardViewModel.totalJobCount),
                        status = stringResource(R.string.progress_title_generating),
                        step = stringResource(R.string.progress_status_generating)
                    )
                }
            }
        }
    }

    @Composable
    fun ProgressScreen(
        indeterminate: Boolean,
        progress: Float,
        status: String,
        step: String
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.h4
            )
            Text(
                text = step,
                style = MaterialTheme.typography.h5
            )
            if (indeterminate) {
                LinearProgressIndicator()
            } else {
                LinearProgressIndicator(
                    progress = progress
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        wizardViewModel.generatorState.observe(viewLifecycleOwner) { state ->
            when (state) {
                RingtoneGenerator.State.NOT_READY -> wizardViewModel.initialiseGenerator()
                RingtoneGenerator.State.READY -> wizardViewModel.startGenerating()
                RingtoneGenerator.State.FINISHED -> navigateNext()
                else -> {} // Do nothing
            }
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
}
