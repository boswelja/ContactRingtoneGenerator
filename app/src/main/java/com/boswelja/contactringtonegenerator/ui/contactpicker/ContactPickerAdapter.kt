package com.boswelja.contactringtonegenerator.ui.contactpicker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.databinding.ContactPickerRecyclerviewItemBinding

class ContactPickerAdapter(
    private val useNicknames: Boolean,
    private val listener: ContactSelectionListener
) : ListAdapter<Contact, ContactPickerAdapter.ContactViewHolder>(ContactDiffCallback()) {

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
            bind(contact, allContactsSelected)
            checkbox.isChecked = selected
            itemView.setOnClickListener {
                if (holder.isChecked) {
                    selectedContacts[contact] = false
                    holder.isChecked = false
                    listener.onContactDeselected(contact)
                } else {
                    selectedContacts[contact] = true
                    holder.isChecked = true
                    listener.onContactSelected(contact)
                }
            }
        }
    }

    fun setSelectedContacts(newSelection: List<Contact>) {
        deselectAllContacts()
        newSelection.forEach {
            selectedContacts[it] = true
            listener.onContactSelected(it)
        }
        allContactsSelected.value = selectedContacts.count() >= itemCount
    }

    fun deselectAllContacts() {
        selectedContacts.keys.forEach {
            listener.onContactDeselected(it)
        }
        selectedContacts.clear()
        allContactsSelected.value = false
    }

    class ContactViewHolder(
        private val binding: ContactPickerRecyclerviewItemBinding,
        private val useNicknames: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {

        val checkbox = binding.checkbox

        var isChecked: Boolean get() = binding.checkbox.isChecked
            set(value) { binding.checkbox.isChecked = value }

        fun bind(contact: Contact, allSelectedLiveData: LiveData<Boolean>) {
            binding.contactName.text = if (useNicknames) {
                contact.nickname ?: contact.displayName
            } else {
                contact.displayName
            }
            if (contact.photoUri != null) {
                binding.contactIcon.setImageURI(contact.photoUri)
            } else {
                binding.contactIcon.setImageResource(R.drawable.ic_default_contact)
            }
            allSelectedLiveData.observe(checkbox.context as LifecycleOwner) {
                checkbox.isChecked = it
            }
        }

        companion object {
            fun from(parent: ViewGroup, useNicknames: Boolean): ContactViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ContactPickerRecyclerviewItemBinding.inflate(layoutInflater, parent, false)
                return ContactViewHolder(binding, useNicknames)
            }
        }
    }
}

class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
    override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
        return oldItem.id == newItem.id
    }
}