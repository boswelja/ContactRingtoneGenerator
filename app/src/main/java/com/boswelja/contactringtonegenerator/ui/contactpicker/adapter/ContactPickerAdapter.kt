package com.boswelja.contactringtonegenerator.ui.contactpicker.adapter

import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import com.boswelja.contactringtonegenerator.contacts.Contact

class ContactPickerAdapter(
    private val useNicknames: Boolean,
    private val selectionListener: ContactSelectionListener,
    private val selectedContacts: HashMap<Long, Boolean> = HashMap()
) : ListAdapter<Contact, ContactViewHolder>(ContactDiffCallback()) {

    private val selectionCallback = object : SelectionCallback {
        override fun onSelected(contact: Contact, isSelected: Boolean) {
            selectedContacts[contact.id] = isSelected
            if (isSelected) selectionListener.onContactSelected(contact.id)
            else selectionListener.onContactDeselected(contact.id)
        }
    }

    private val _allContactsSelected = MutableLiveData(false)
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

    override fun submitList(list: List<Contact>?) {
        super.submitList(list)
        updateAllContactsSelected()
    }

    /**
     * Checks whether all contacts are currently selected and updates [allContactsSelected].
     */
    @VisibleForTesting
    fun updateAllContactsSelected() {
        _allContactsSelected.postValue(
            if (itemCount == 0) false
            else selectedContacts.count { it.value } >= itemCount
        )
    }

    /**
     * Selects all [Contact]s in a given list. This will call back to [selectionListener].
     * @param newSelection The [List] of contacts to select.
     */
    fun selectContacts(newSelection: List<Contact>) {
        deselectAllContacts()
        newSelection.forEach {
            selectedContacts[it.id] = true
            selectionListener.onContactSelected(it.id)
        }
        updateAllContactsSelected()
    }

    /**
     * Deselect all currently selected contacts. This will call back to [selectionListener].
     */
    fun deselectAllContacts() {
        selectedContacts.keys.forEach {
            selectionListener.onContactDeselected(it)
        }
        selectedContacts.clear()
        _allContactsSelected.postValue(false)
    }
}
