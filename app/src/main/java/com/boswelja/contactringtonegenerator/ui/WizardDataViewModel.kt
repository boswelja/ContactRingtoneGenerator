package com.boswelja.contactringtonegenerator.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.ringtonegen.RingtoneGenerator
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem

class WizardDataViewModel(application: Application) : AndroidViewModel(application) {

    val ringtoneGenerator: RingtoneGenerator by lazy { RingtoneGenerator.get(application) }
    val selectedContacts = ArrayList<Contact>()
    val ringtoneStructure = ArrayList<StructureItem>()

    fun startGenerating() {
        ringtoneGenerator.setContacts(selectedContacts)
        ringtoneGenerator.setRingtoneStructure(ringtoneStructure)
        if (ringtoneGenerator.state == RingtoneGenerator.State.READY) {
            ringtoneGenerator.start()
        }
    }

    override fun onCleared() {
        super.onCleared()
        ringtoneGenerator.destroy()
    }
}
