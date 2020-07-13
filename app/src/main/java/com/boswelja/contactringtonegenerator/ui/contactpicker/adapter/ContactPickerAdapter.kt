package com.boswelja.contactringtonegenerator.ui.contactpicker.adapter

import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import com.boswelja.contactringtonegenerator.contacts.Contact

class ContactPickerAdapter(
        private val useNicknames: Boolean,
        private val selectionListener: ContactSelectionListener
) : ListAdapter<Contact, ContactViewHolder>(ContactDiffCallback()) {

    private val selectedContacts = HashMap<Contact, Boolean>()
    private val allContactsSelected: MutableLiveData<Boolean> = MutableLiveData(canSelectAllContacts)
    private val selectionCallback = object : SelectionCallback {
        override fun onSelected(contact: Contact, isSelected: Boolean) {
            selectedContacts[contact] = isSelected
            if (isSelected) selectionListener.onContactSelected(contact)
            else selectionListener.onContactDeselected(contact)
        }
    }

    val canSelectAllContacts: Boolean get() = selectedContacts.count() < itemCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder.from(parent, useNicknames)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = getItem(position)
        val selected = selectedContacts[contact] ?: false
        holder.apply {
            bind(contact, allContactsSelected, selectionCallback)
            checkbox.isChecked = selected
        }
    }

    fun setSelectedContacts(newSelection: List<Contact>) {
        deselectAllContacts()
        newSelection.forEach {
            selectedContacts[it] = true
            selectionListener.onContactSelected(it)
        }
        allContactsSelected.value = selectedContacts.count() >= itemCount
    }

    fun deselectAllContacts() {
        selectedContacts.keys.forEach {
            selectionListener.onContactDeselected(it)
        }
        selectedContacts.clear()
        allContactsSelected.value = false
    }
}

