package com.boswelja.contactringtonegenerator.ui.contactpicker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.contacts.ContactsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ContactsViewModel(application: Application) : AndroidViewModel(application) {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val _adapterContacts = MutableLiveData<List<Contact>>()
    val adapterContacts: LiveData<List<Contact>>
        get() = _adapterContacts

    lateinit var allContacts: List<Contact>

    init {
        coroutineScope.launch {
            allContacts = ContactsHelper.getContacts(application)
            withContext(Dispatchers.Main) {
                _adapterContacts.value = allContacts
            }
        }
    }

    fun filterContacts(searchQuery: CharSequence?) {
        coroutineScope.launch {
            searchQuery?.let { searchQuery ->
                val query = searchQuery.toString().toLowerCase(Locale.ROOT)
                val filteredList = allContacts.filter {
                    it.displayName.toLowerCase(Locale.ROOT).contains(query) ||
                            it.displayName.toLowerCase(Locale.ROOT).contains(query)
                }
                withContext(Dispatchers.Main) {
                    _adapterContacts.value = filteredList
                }
            }
        }
    }
}
