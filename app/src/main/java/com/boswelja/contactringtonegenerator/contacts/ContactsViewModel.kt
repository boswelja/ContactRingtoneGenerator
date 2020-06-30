package com.boswelja.contactringtonegenerator.contacts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData

class ContactsViewModel(application: Application) : AndroidViewModel(application) {
    val contacts: LiveData<List<Contact>> = liveData {
        val contacts = ContactsHelper.getContacts(application)
        emit(contacts)
    }
}