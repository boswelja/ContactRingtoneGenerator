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
import com.boswelja.contactringtonegenerator.ringtonegen.Result
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
                    ProgressScreen(
                        progress = ((progress?.toFloat() ?: 0f) / wizardViewModel.totalJobCount),
                        status = stringResource(R.string.progress_title_generating),
                        step = stringResource(R.string.progress_status_generating)
                    )
                }
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
        val successCount = wizardViewModel.successCount
        val failCount = wizardViewModel.failCount
        val result = when {
            failCount.value!! < 1 && successCount.value!! > 0 -> {
                Result.SUCCESSFUL
            }
            failCount.value!! > 0 && successCount.value!! > 0 -> {
                Result.MIXED
            }
            failCount.value!! > 0 && successCount.value!! < 1 -> {
                Result.FAILED
            }
            else -> Result.UNKNOWN
        }
        findNavController().navigate(
            ProgressFragmentDirections.toFinishedFragment(result)
        )
    }
}

@Composable
fun ProgressScreen(
    progress: Float = 0f,
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
        if (progress > 0) {
            LinearProgressIndicator(
                progress = progress
            )
        } else {
            LinearProgressIndicator()
        }
    }
}
