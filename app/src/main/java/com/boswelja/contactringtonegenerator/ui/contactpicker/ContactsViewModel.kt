package com.boswelja.contactringtonegenerator.ui.contactpicker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.contacts.ContactsHelper

class ContactsViewModel(application: Application) : AndroidViewModel(application) {
    val contacts: LiveData<List<Contact>> = liveData {
        val contacts = ContactsHelper.getContacts(application)
        emit(contacts)
    }
}
