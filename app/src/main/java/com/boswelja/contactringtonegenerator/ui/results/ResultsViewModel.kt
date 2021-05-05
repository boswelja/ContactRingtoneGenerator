package com.boswelja.contactringtonegenerator.ui.results

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.boswelja.contactringtonegenerator.R

class ResultsViewModel(successCount: Int, failCount: Int) : ViewModel() {

    val state = MutableLiveData(Result.UNKNOWN)

    val resultIconRes = Transformations.map(state) {
        when (it) {
            Result.SUCCESSFUL -> R.drawable.ic_success_indicator
            Result.FAILED -> R.drawable.ic_error_indicator
            Result.MIXED -> R.drawable.ic_mixed_indicator
            else -> R.drawable.ic_unknown_indicator
        }
    }
    val resultTitleRes = Transformations.map(state) {
        when (it) {
            Result.SUCCESSFUL -> R.string.result_success_title
            Result.FAILED -> R.string.result_failed_title
            Result.MIXED -> R.string.result_mixed_title
            else -> R.string.result_unknown_title
        }
    }
    val resultStatusRes = Transformations.map(state) {
        when (it) {
            Result.SUCCESSFUL -> R.string.result_success_status
            Result.FAILED -> R.string.result_failed_status
            Result.MIXED -> R.string.result_mixed_status
            else -> R.string.result_unknown_status
        }
    }

    init {
        state.postValue(getState(successCount, failCount))
    }

    private fun getState(successCount: Int, failCount: Int): Result {
        val hasSuccesses = successCount > 0
        val hasFailures = failCount > 0
        return if (hasSuccesses && hasFailures)
            Result.MIXED
        else if (hasSuccesses && !hasFailures)
            Result.SUCCESSFUL
        else if (!hasSuccesses && hasFailures)
            Result.FAILED
        else
            Result.UNKNOWN
    }
}
