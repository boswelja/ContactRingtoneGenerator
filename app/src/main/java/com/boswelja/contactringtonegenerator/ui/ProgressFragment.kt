package com.boswelja.contactringtonegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.databinding.FragmentProgressBinding
import com.boswelja.contactringtonegenerator.ringtonegen.RingtoneGenerator
import com.boswelja.contactringtonegenerator.tts.SynthesisResult
import timber.log.Timber

class ProgressFragment :
    Fragment(),
    RingtoneGenerator.ProgressListener,
    RingtoneGenerator.StateListener {

    private lateinit var binding: FragmentProgressBinding
    private lateinit var ringtoneGenerator: RingtoneGenerator

    override fun onStateChanged(state: RingtoneGenerator.State) {
        Timber.d("onStateChanged($state)")
        when (state) {
            RingtoneGenerator.State.READY -> {
                ringtoneGenerator.start()
            }
            RingtoneGenerator.State.GENERATING -> {
                binding.progressBar.apply {
                    isIndeterminate = false
                    progress = 0
                    secondaryProgress = 0
                    max = ringtoneGenerator.totalJobCount
                }
                binding.loadingTitle.text = getString(R.string.progress_title_generating)
            }
            RingtoneGenerator.State.FINISHED -> {
                binding.apply {
                    val successes = progressBar.progress
                    val failures = progressBar.secondaryProgress - progressBar.progress
                    findNavController().navigate(ProgressFragmentDirections.toFinishedFragment(successes, failures))
                }
                ringtoneGenerator.destroy()
            }
            else -> {
                // Do nothing
            }
        }
    }

    override fun onJobStarted(contact: Contact) {
        Timber.d("onJobStarted($contact)")
        binding.loadingStatus.text = getString(R.string.progress_status_generating, contact.nickname ?: contact.name)
    }

    override fun onJobCompleted(success: Boolean, synthesisResult: SynthesisResult) {
        Timber.d("onJobCompleted($success, $synthesisResult)")
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
        binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val activity = requireActivity()
        if (activity is MainActivity) {
            ringtoneGenerator = activity.createRingtoneGenerator().apply {
                progressListener = this@ProgressFragment
                stateListener = this@ProgressFragment
            }
        }
    }
}
