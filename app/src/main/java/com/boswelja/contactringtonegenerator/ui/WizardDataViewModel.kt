package com.boswelja.contactringtonegenerator.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.ringtonegen.RingtoneGenerator
import com.boswelja.contactringtonegenerator.ringtonegen.item.BaseItem

class WizardDataViewModel : ViewModel() {

    val selectedContacts = ArrayList<Contact>()
    val ringtoneStructure = ArrayList<BaseItem>()

    fun createRingtoneGenerator(context: Context): RingtoneGenerator =
        RingtoneGenerator(context, ringtoneStructure, selectedContacts)
}
