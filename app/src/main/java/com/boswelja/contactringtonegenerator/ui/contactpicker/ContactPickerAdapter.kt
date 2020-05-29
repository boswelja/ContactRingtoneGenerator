package com.boswelja.contactringtonegenerator.ui.contactpicker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.databinding.ContactPickerItemBinding

class ContactPickerAdapter(private val listener: ContactSelectionListener? = null) :
        RecyclerView.Adapter<ContactPickerAdapter.ContactViewHolder>() {

    private var layoutInflater: LayoutInflater? = null
    private var useNicknames: Boolean = true

    private val contacts = ArrayList<Contact>()
    val selectedContacts = ArrayList<Contact>()

    override fun getItemCount(): Int = contacts.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.context)
        }

        return ContactViewHolder(ContactPickerItemBinding.inflate(layoutInflater!!, parent, false))
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(
            if (useNicknames) {
                contact.contactNickname ?: contact.contactName
            } else {
                contact.contactName
            },
            selectedContacts.contains(contact)
        )
        holder.itemView.setOnClickListener {
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

    class ContactViewHolder(private val binding: ContactPickerItemBinding) : RecyclerView.ViewHolder(binding.root) {

        var isChecked: Boolean get() = binding.checkbox.isChecked
        set(value) { binding.checkbox.isChecked = value }

        fun bind(contactNameString: String, selected: Boolean) {
            binding.apply {
                contactName.text = contactNameString
                checkbox.isChecked = selected
            }
        }
    }
}