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
        binding.finishButton.setOnClickListener {
            activity?.finish()
        }
        updateStatus(args.successCount, args.failureCount)
    }

    private fun updateStatus(successCount: Int, failCount: Int) {
        val statusIndicatorRes: Int
        val statusTitleRes: Int
        val statusDesc: String
        when (getState(successCount, failCount)) {
            State.SUCCESSFUL -> {
                statusIndicatorRes = R.drawable.ic_success_indicator
                statusTitleRes = R.string.finished_title_success
                statusDesc = getString(R.string.finished_status_success, successCount.toString())
            }
            State.FAILED -> {
                statusIndicatorRes = R.drawable.ic_error_indicator
                statusTitleRes = R.string.finished_title_error
                statusDesc = getString(R.string.finished_status_error, failCount.toString())
            }
            State.MIXED -> {
                statusIndicatorRes = R.drawable.ic_mixed_indicator
                statusTitleRes = R.string.finished_title_mixed
                statusDesc = getString(
                    R.string.finished_status_mixed,
                    successCount.toString(),
                    failCount.toString()
                )
            }
            State.UNKNOWN -> {
                statusIndicatorRes = R.drawable.ic_unknown_indicator
                statusTitleRes = R.string.finished_title_unknown
                statusDesc = getString(R.string.finished_status_unknown)
            }
        }

        binding.apply {
            statusIndicator.setImageResource(statusIndicatorRes)
            statusTitle.setText(statusTitleRes)
            statusDescription.text = statusDesc
        }
    }

    enum class State {
        FAILED,
        MIXED,
        SUCCESSFUL,
        UNKNOWN
    }

    companion object {
        fun getState(successCount: Int, failCount: Int): State {
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
    }
}
