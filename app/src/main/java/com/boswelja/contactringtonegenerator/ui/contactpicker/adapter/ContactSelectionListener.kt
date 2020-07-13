package com.boswelja.contactringtonegenerator.ui.contactpicker.adapter

import com.boswelja.contactringtonegenerator.contacts.Contact

interface ContactSelectionListener {

    fun onContactSelected(contactId: Long)
    fun onContactDeselected(contactId: Long)
}
