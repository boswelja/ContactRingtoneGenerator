package com.boswelja.contactringtonegenerator.ui.contactpicker.adapter

import androidx.recyclerview.widget.DiffUtil
import com.boswelja.contactringtonegenerator.contacts.Contact

class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
    override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
        return oldItem.id == newItem.id
    }
}