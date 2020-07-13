package com.boswelja.contactringtonegenerator.ui.contactpicker.adapter

import com.boswelja.contactringtonegenerator.contacts.Contact

interface SelectionCallback {
    fun onSelected(contact: Contact, isSelected: Boolean)
}