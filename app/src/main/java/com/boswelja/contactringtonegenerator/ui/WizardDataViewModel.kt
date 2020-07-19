package com.boswelja.contactringtonegenerator.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.ringtonegen.RingtoneGenerator
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem

class WizardDataViewModel(application: Application) : AndroidViewModel(application) {

    private val generatorProgressListener = object : RingtoneGenerator.ProgressListener {
        override fun onJobStarted(contact: Contact) {
            val newValue = _startedJobCount.value!!.plus(1)
            _startedJobCount.postValue(newValue)
        }

        override fun onJobCompleted(success: Boolean, contact: Contact) {
            if (success) {
                val newValue = _successCount.value!!.plus(1)
                _successCount.postValue(newValue)
            } else {
                val newValue = _failCount.value!!.plus(1)
                _failCount.postValue(newValue)
            }
        }
    }

    private val ringtoneGenerator: RingtoneGenerator =
            RingtoneGenerator.get(application).apply {
                progressListener = generatorProgressListener
            }

    private val _startedJobCount = MutableLiveData(0)
    private val _successCount = MutableLiveData(0)
    private val _failCount = MutableLiveData(0)

    val totalJobCount: Int
        get() = ringtoneGenerator.totalJobCount
    val generatorState: LiveData<RingtoneGenerator.State>
        get() = ringtoneGenerator.state
    val startedJobCount: LiveData<Int>
        get() = _startedJobCount
    val successCount: LiveData<Int>
        get() = _successCount
    val failCount: LiveData<Int>
        get() = _failCount

    fun startGenerating() {
        if (ringtoneGenerator.state.value == RingtoneGenerator.State.READY) {
            ringtoneGenerator.start()
        }
    }

    fun submitSelectedContacts(selectedContacts: List<Contact>) {
        ringtoneGenerator.setContacts(selectedContacts)
    }

    fun submitRingtoneStructure(ringtoneStructure: List<StructureItem>) {
        ringtoneGenerator.setRingtoneStructure(ringtoneStructure)
    }

    override fun onCleared() {
        super.onCleared()
        ringtoneGenerator.destroy()
    }
}
