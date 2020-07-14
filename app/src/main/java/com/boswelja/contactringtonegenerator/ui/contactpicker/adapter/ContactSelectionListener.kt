package com.boswelja.contactringtonegenerator.ui.contactpicker.adapter

interface ContactSelectionListener {

    fun onContactSelected(contactId: Long)
    fun onContactDeselected(contactId: Long)
}
