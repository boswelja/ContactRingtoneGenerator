package com.boswelja.contactringtonegenerator.ui.contactpicker.adapter

import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import com.boswelja.contactringtonegenerator.contacts.Contact

class ContactPickerAdapter(
    private val useNicknames: Boolean,
    private val selectionListener: ContactSelectionListener
) : ListAdapter<Contact, ContactViewHolder>(ContactDiffCallback()) {

    private val selectedContacts = HashMap<Long, Boolean>()

    private val selectionCallback = object : SelectionCallback {
        override fun onSelected(contact: Contact, isSelected: Boolean) {
            selectedContacts[contact.id] = isSelected
            if (isSelected) selectionListener.onContactSelected(contact.id)
            else selectionListener.onContactDeselected(contact.id)
        }
    }

    private val _allContactsSelected = MutableLiveData(selectedContacts.count { it.value } >= itemCount)
    val allContactsSelected: LiveData<Boolean>
        get() = _allContactsSelected


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder.from(parent, useNicknames)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = getItem(position)
        val selected = selectedContacts[contact.id] ?: false
        holder.apply {
            bind(contact, allContactsSelected, selectionCallback)
            checkbox.isChecked = selected
        }
    }

    fun setSelectedContacts(newSelection: List<Contact>) {
        deselectAllContacts()
        newSelection.forEach {
            selectedContacts[it.id] = true
            selectionListener.onContactSelected(it.id)
        }
        _allContactsSelected.value = selectedContacts.count() >= itemCount
    }

    fun deselectAllContacts() {
        selectedContacts.keys.forEach {
            selectionListener.onContactDeselected(it)
        }
        selectedContacts.clear()
        _allContactsSelected.value = false
    }
}
