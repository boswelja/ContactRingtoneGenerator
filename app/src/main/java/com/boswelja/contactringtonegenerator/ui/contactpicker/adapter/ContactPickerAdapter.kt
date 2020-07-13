package com.boswelja.contactringtonegenerator.ui.contactpicker.adapter

import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import com.boswelja.contactringtonegenerator.contacts.Contact

class ContactPickerAdapter(
        private val useNicknames: Boolean,
        private val selectionListener: ContactSelectionListener,
        private val clickListener: ContactClickListener
) : ListAdapter<Contact, ContactViewHolder>(ContactDiffCallback()) {

    private val selectedContacts = HashMap<Contact, Boolean>()
    private val allContactsSelected: MutableLiveData<Boolean> = MutableLiveData(canSelectAllContacts)

    val canSelectAllContacts: Boolean get() = selectedContacts.count() < itemCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder.from(parent, useNicknames)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = getItem(position)
        val selected = selectedContacts[contact] ?: false
        holder.apply {
            bind(contact, clickListener, allContactsSelected)
            checkbox.isChecked = selected
        }
    }

    fun toggleSelected(position: Int) {
        val item = getItem(position)
        val selected: Boolean = selectedContacts[item] ?: false
        if (selected) {
            selectedContacts[item] = false
            selectionListener.onContactDeselected(item)
        } else {
            selectedContacts[item] = true
            selectionListener.onContactSelected(item)
        }
        notifyItemChanged(position)
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

