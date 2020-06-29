package com.boswelja.contactringtonegenerator.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.ringtonegen.RingtoneGenerator
import com.boswelja.contactringtonegenerator.ringtonegen.item.BaseItem

class WizardDataViewModel : ViewModel() {

    private val selectedContacts = ArrayList<Contact>()
    private val ringtoneStructure = ArrayList<BaseItem>()

    fun setSelectedContacts(newContacts: List<Contact>) {
        selectedContacts.apply {
            clear()
            addAll(newContacts)
        }
    }

    fun setRingtoneStructure(newStructure: List<BaseItem>) {
        ringtoneStructure.apply {
            clear()
            addAll(newStructure)
        }
    }

    fun createRingtoneGenerator(context: Context): RingtoneGenerator =
            RingtoneGenerator(context, ringtoneStructure, selectedContacts)
}