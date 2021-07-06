package com.boswelja.contactringtonegenerator

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.boswelja.contactringtonegenerator.contactpicker.Contact
import com.boswelja.contactringtonegenerator.contactpicker.ContactsHelper
import com.boswelja.contactringtonegenerator.ringtonegen.item.StructureItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapConcat

class WizardViewModel(application: Application) : AndroidViewModel(application) {

    val contactsQuery = MutableStateFlow("")

    val selectedContacts = mutableStateListOf<Contact>()

    @FlowPreview
    @ExperimentalCoroutinesApi
    val adapterContacts = contactsQuery.flatMapConcat { query ->
        val formattedQuery = query.trim().let { trimmedQuery ->
            // If we actually have a query, format it as lower case
            if (trimmedQuery.isNotEmpty())
                trimmedQuery
            else null
        }
        ContactsHelper.getContacts(
            application.contentResolver,
            500,
            formattedQuery
        )
    }

    val ringtoneStructure = mutableStateListOf<StructureItem<*>>()
    val isRingtoneValid: Boolean
        get() = ringtoneStructure.isNotEmpty() && ringtoneStructure.all { it.isDataValid }
}
