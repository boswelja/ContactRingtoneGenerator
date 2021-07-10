package com.boswelja.contactringtonegenerator

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.work.ExperimentalExpeditedWork
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.boswelja.contactringtonegenerator.contactpicker.Contact
import com.boswelja.contactringtonegenerator.ringtonegen.RingtoneGeneratorWorker
import com.boswelja.contactringtonegenerator.ringtonegen.RingtoneGeneratorWorker.Companion.ContactLookupKeys
import com.boswelja.contactringtonegenerator.ringtonegen.RingtoneGeneratorWorker.Companion.RingtoneStructure
import com.boswelja.contactringtonegenerator.ringtonegen.item.StructureItem
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WizardViewModel(application: Application) : AndroidViewModel(application) {

    private var workRequestId: UUID? = null

    var selectedContacts by mutableStateOf(hashSetOf<String>())
        private set

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
                    RingtoneStructure to serializedStructure,
                    ContactLookupKeys to selectedContacts.toTypedArray()
                )
            )
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workRequestId = request.id

        WorkManager.getInstance(getApplication()).enqueue(request)
    }

    fun getWorkInfo(): Flow<WorkInfo> {
        checkNotNull(workRequestId)

        return WorkManager.getInstance(getApplication())
            .getWorkInfoByIdLiveData(workRequestId!!).asFlow()
    }
}
