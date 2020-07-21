package com.boswelja.contactringtonegenerator.ui.results

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.boswelja.contactringtonegenerator.R

class ResultsViewModel(successCount: Int, failCount: Int) : ViewModel() {

    private val state = MutableLiveData(State.UNKNOWN)

    val resultIconRes = Transformations.map(state) {
        when (it) {
            State.SUCCESSFUL -> R.drawable.ic_success_indicator
            State.FAILED -> R.drawable.ic_error_indicator
            State.MIXED -> R.drawable.ic_mixed_indicator
            else -> R.drawable.ic_unknown_indicator
        }
    }
    val resultTitleRes = Transformations.map(state) {
        when (it) {
            State.SUCCESSFUL -> R.string.result_success_title
            State.FAILED -> R.string.result_failed_title
            State.MIXED -> R.string.result_mixed_title
            else -> R.string.result_unknown_title
        }
    }
    val resultStatusRes = Transformations.map(state) {
        when (it) {
            State.SUCCESSFUL -> R.string.result_success_status
            State.FAILED -> R.string.result_failed_status
            State.MIXED -> R.string.result_mixed_status
            else -> R.string.result_unknown_status
        }
    }

    init {
        state.postValue(getState(successCount, failCount))
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
}
