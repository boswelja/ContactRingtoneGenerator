package com.boswelja.contactringtonegenerator.ui.contactpicker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.boswelja.contactringtonegenerator.contacts.ContactsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import java.util.Locale

class ContactsViewModel(application: Application) : AndroidViewModel(application) {

    val searchQuery = MutableLiveData("")

    @ExperimentalCoroutinesApi
    val allContacts = ContactsHelper.getContacts(application.contentResolver, 25)

    @ExperimentalCoroutinesApi
    val adapterContacts = searchQuery.switchMap { query ->
        val formattedQuery = query.toLowerCase(Locale.ROOT)
        allContacts.map { contacts ->
            contacts.filter {
                it.displayName.toLowerCase(Locale.ROOT).contains(formattedQuery) ||
                    it.nickname?.toLowerCase(Locale.ROOT)?.contains(formattedQuery) == true
            }
        }.asLiveData(Dispatchers.IO)
    }
}
