package com.boswelja.contactringtonegenerator.ui.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ResultViewModelFactory(private val successCount: Int, private val failCount: Int) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(Int::class.java, Int::class.java).newInstance(successCount, failCount)
    }
}
