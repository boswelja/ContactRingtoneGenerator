package com.boswelja.contactringtonegenerator.ui.contactpicker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.boswelja.contactringtonegenerator.contacts.ContactsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class ContactsViewModel(application: Application) : AndroidViewModel(application) {

    val searchQuery = MutableLiveData("")

    val adapterContacts = searchQuery.switchMap { query ->
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
        ).asLiveData(Dispatchers.Default)
    }
}
