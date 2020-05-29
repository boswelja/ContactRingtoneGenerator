package com.boswelja.contactringtonegenerator.ui.contactpicker

import com.boswelja.contactringtonegenerator.contacts.Contact

interface ContactSelectionListener {

    fun onContactSelected(contact: Contact)
    fun onContactDeselected(contact: Contact)

}