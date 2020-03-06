package com.boswelja.contactringtonegenerator.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.contacts.Contact

class ContactPickerAdapter : RecyclerView.Adapter<ContactPickerAdapter.ContactViewHolder>() {

    private var layoutInflater: LayoutInflater? = null
    private var useNicknames: Boolean = true

    private val contacts = ArrayList<Contact>()
    val selectedContacts = ArrayList<Contact>()

    override fun getItemCount(): Int = contacts.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.context)
        }
        return ContactViewHolder(layoutInflater!!.inflate(R.layout.contact_picker_item, parent, false))
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.checkbox.isChecked = selectedContacts.contains(contact)
        holder.itemView.setOnClickListener {
            if (holder.checkbox.isChecked) {
                selectedContacts.remove(contact)
                holder.checkbox.isChecked = false
            } else {
                selectedContacts.add(contact)
                holder.checkbox.isChecked = true
            }
        }

        holder.contactNameView.text = if (useNicknames) {
            contact.contactNickname ?: contact.contactName
        } else {
            contact.contactName
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
        contacts.clear()
        selectedContacts.clear()
        contacts.addAll(newContacts)
        Log.d("ContactPickerAdapter", "Found ${contacts.count()} contacts")
        sortContacts()
        notifyDataSetChanged()
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: AppCompatCheckBox = itemView.findViewById(R.id.checkbox)
        val contactNameView: AppCompatTextView = itemView.findViewById(R.id.contact_name)
    }
}