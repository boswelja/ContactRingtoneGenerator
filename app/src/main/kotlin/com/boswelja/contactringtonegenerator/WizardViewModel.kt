package com.boswelja.contactringtonegenerator

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.boswelja.contactringtonegenerator.contactpicker.ContactsHelper
import com.boswelja.contactringtonegenerator.ringtonegen.item.StructureItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

class WizardViewModel(application: Application) : AndroidViewModel(application) {

    var selectedContacts by mutableStateOf(hashSetOf<String>())
        private set

    @FlowPreview
    @ExperimentalCoroutinesApi
    val allContacts = ContactsHelper.getContacts(
        application.contentResolver,
        500
    )

    val ringtoneStructure = mutableStateListOf<StructureItem>()
    val isRingtoneValid: Boolean
        get() = ringtoneStructure.isNotEmpty() && ringtoneStructure.all { it.isDataValid }

    fun selectContacts(newContactKeys: List<String>) {
        val newList = selectedContacts + newContactKeys
        selectedContacts = newList.toHashSet()
    }

    fun deselectContacts(newContactKeys: List<String>) {
        val newList = selectedContacts - newContactKeys
        selectedContacts = newList.toHashSet()
    }
}
