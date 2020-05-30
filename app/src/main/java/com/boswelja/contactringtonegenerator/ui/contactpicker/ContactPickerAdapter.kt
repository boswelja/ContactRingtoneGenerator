package com.boswelja.contactringtonegenerator.ui.contactpicker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.contacts.ContactManager
import com.boswelja.contactringtonegenerator.databinding.ContactPickerRecyclerviewItemBinding

class ContactPickerAdapter(private val listener: ContactSelectionListener? = null) :
        RecyclerView.Adapter<ContactPickerAdapter.ContactViewHolder>() {

    private var layoutInflater: LayoutInflater? = null
    private var useNicknames: Boolean = true

    private val contacts = ArrayList<Contact>()
    private val selectedContacts = ArrayList<Contact>()

    override fun getItemCount(): Int = contacts.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.context)
        }

        return ContactViewHolder(ContactPickerRecyclerviewItemBinding.inflate(layoutInflater!!, parent, false))
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        val selected = selectedContacts.contains(contact)
        holder.apply {
            contactName.text = if (useNicknames) {
                contact.contactNickname ?: contact.contactName
            } else {
                contact.contactName
            }
            itemView.setOnClickListener {
                if (holder.isChecked) {
                    selectedContacts.remove(contact)
                    holder.isChecked = false
                    listener?.onContactDeselected(contact)
                } else {
                    selectedContacts.add(contact)
                    holder.isChecked = true
                    listener?.onContactSelected(contact)
                }
            }
            checkbox.isChecked = selected
            val photoUri = ContactManager.getContactPhotoUri(holder.itemView.context, contact.id)
            if (photoUri != null) {
                contactIcon.setImageURI(photoUri)
            } else {
                contactIcon.setImageResource(R.drawable.ic_default_contact)
            }
        }
    }

    private fun sortContacts() {
        contacts.sortBy {
            if (useNicknames) {
                it.contactNickname ?: it.contactName
            } else {
                it.contactName
            }
        }
    }

    fun setUseNicknames(useNicknames: Boolean) {
        if (this.useNicknames != useNicknames) {
            this.useNicknames = useNicknames
            sortContacts()
            notifyDataSetChanged()
        }
    }

    fun setContacts(newContacts: List<Contact>) {
        ArrayList(newContacts).apply {
            contacts.retainAll(this)
            selectedContacts.retainAll(this)
            removeAll(contacts)
            forEach {
                contacts.add(it)
            }
        }
        sortContacts()
        notifyDataSetChanged()
    }

    class ContactViewHolder(private val binding: ContactPickerRecyclerviewItemBinding) : RecyclerView.ViewHolder(binding.root) {

        val contactName = binding.contactName
        val contactIcon = binding.contactIcon
        val checkbox = binding.checkbox

        var isChecked: Boolean get() = binding.checkbox.isChecked
        set(value) { binding.checkbox.isChecked = value }
    }
}