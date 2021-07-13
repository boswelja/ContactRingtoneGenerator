package com.boswelja.contactringtonegenerator

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.ExperimentalExpeditedWork
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.boswelja.contactringtonegenerator.contactpicker.Contact
import com.boswelja.contactringtonegenerator.ringtonebuilder.StructureItem
import com.boswelja.contactringtonegenerator.ringtonegen.GeneratorResult
import com.boswelja.contactringtonegenerator.ringtonegen.RingtoneGeneratorWorker
import com.boswelja.contactringtonegenerator.ringtonegen.RingtoneGeneratorWorker.Inputs.ContactLookupKeys
import com.boswelja.contactringtonegenerator.ringtonegen.RingtoneGeneratorWorker.Inputs.RingtoneStructure
import java.util.UUID
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WizardViewModel(application: Application) : AndroidViewModel(application) {

    private var workRequestId: UUID? = null

    var selectedContacts by mutableStateOf(hashSetOf<String>())
        private set

    var generatorResult: GeneratorResult? = null

    val ringtoneStructure = mutableStateListOf<StructureItem>()
    val isRingtoneValid: Boolean
        get() = ringtoneStructure.isNotEmpty() && ringtoneStructure.all { it.isDataValid }

    fun selectContact(contact: Contact) {
        val newList = selectedContacts + contact.lookupKey
        selectedContacts = newList.toHashSet()
    }

    fun deselectContact(contact: Contact) {
        val newList = selectedContacts - contact.lookupKey
        selectedContacts = newList.toHashSet()
    }

    @ExperimentalExpeditedWork
    fun startWorker() {
        val serializedStructure = ringtoneStructure.map {
            Json.encodeToString(it)
        }
        val request = OneTimeWorkRequestBuilder<RingtoneGeneratorWorker>()
            .setInputData(
                workDataOf(
                    RingtoneStructure to serializedStructure.toTypedArray(),
                    ContactLookupKeys to selectedContacts.toTypedArray()
                )
            )
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workRequestId = request.id

        WorkManager.getInstance(getApplication()).enqueue(request)
    }

    fun getWorkInfo(): LiveData<WorkInfo> {
        checkNotNull(workRequestId)

        return WorkManager.getInstance(getApplication())
            .getWorkInfoByIdLiveData(workRequestId!!)
    }
}
