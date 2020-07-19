package com.boswelja.contactringtonegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.databinding.FragmentProgressBinding
import com.boswelja.contactringtonegenerator.ringtonegen.RingtoneGenerator
import timber.log.Timber

class ProgressFragment :
    Fragment(),
    RingtoneGenerator.ProgressListener,
    RingtoneGenerator.StateListener {

    private val dataModel: WizardDataViewModel by activityViewModels()

    private lateinit var binding: FragmentProgressBinding
    private lateinit var ringtoneGenerator: RingtoneGenerator

    override fun onStateChanged(state: RingtoneGenerator.State) {
        Timber.d("onStateChanged($state)")
        when (state) {
            RingtoneGenerator.State.READY -> {
                ringtoneGenerator.start()
            }
            RingtoneGenerator.State.GENERATING -> {
                binding.apply {
                    progressBar.apply {
                        isIndeterminate = false
                        progress = 0
                        secondaryProgress = 0
                        max = ringtoneGenerator.totalJobCount
                    }
                    loadingTitle.text = getString(R.string.progress_title_generating)
                    loadingStatus.text = getString(R.string.progress_status_generating)
                }
            }
            RingtoneGenerator.State.FINISHED -> {
                binding.apply {
                    val successes = progressBar.progress
                    val failures = progressBar.secondaryProgress - progressBar.progress
                    findNavController().navigate(ProgressFragmentDirections.toFinishedFragment(successes, failures))
                }
            }
            else -> {
                // Do nothing
            }
        }
    }

    override fun onJobStarted(contact: Contact) {
        Timber.d("onJobStarted($contact)")
        binding.progressBar.secondaryProgress += 1
    }

    override fun onJobCompleted(success: Boolean, contact: Contact) {
        Timber.d("onJobCompleted($success, $contact)")
        binding.progressBar.progress += 1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        ringtoneGenerator = dataModel.createRingtoneGenerator(requireContext()).apply {
            progressListener = this@ProgressFragment
            stateListener = this@ProgressFragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ringtoneGenerator.destroy()
    }
}
