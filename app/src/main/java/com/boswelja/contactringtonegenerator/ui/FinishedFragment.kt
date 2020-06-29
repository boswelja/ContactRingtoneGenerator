package com.boswelja.contactringtonegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.databinding.FragmentFinishedBinding

class FinishedFragment : Fragment() {

    private val args: FinishedFragmentArgs by navArgs()

    private lateinit var binding: FragmentFinishedBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFinishedBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun getState(successCount: Int, failCount: Int): State {
        val hasSuccesses = successCount > 0
        val hasFailures = failCount > 0
        return if (hasSuccesses && hasFailures)
            State.MIXED
        else if (hasSuccesses && !hasFailures)
            State.SUCCESSFUL
        else if (!hasSuccesses && hasFailures)
            State.FAILED
        else
            State.UNKNOWN
    }

    private enum class State {
        FAILED,
        MIXED,
        SUCCESSFUL,
        UNKNOWN
    }
}