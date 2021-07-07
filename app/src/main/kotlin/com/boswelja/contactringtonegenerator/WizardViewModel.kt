package com.boswelja.contactringtonegenerator

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.boswelja.contactringtonegenerator.contactpicker.ContactsHelper
import com.boswelja.contactringtonegenerator.ringtonegen.item.StructureItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

class WizardViewModel(application: Application) : AndroidViewModel(application) {

    val selectedContacts = hashSetOf<String>()

    @FlowPreview
    @ExperimentalCoroutinesApi
    val allContacts = ContactsHelper.getContacts(
        application.contentResolver,
        500
    )

    val ringtoneStructure = mutableStateListOf<StructureItem<*>>()
    val isRingtoneValid: Boolean
        get() = ringtoneStructure.isNotEmpty() && ringtoneStructure.all { it.isDataValid }
}
