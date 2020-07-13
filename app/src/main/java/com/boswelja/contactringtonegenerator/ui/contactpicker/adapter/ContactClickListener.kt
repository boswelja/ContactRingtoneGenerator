package com.boswelja.contactringtonegenerator.ui.contactpicker.adapter

import com.boswelja.contactringtonegenerator.contacts.Contact

class ContactClickListener(val clickListener: (position: Int) -> Unit) {
    fun onClick(position: Int) = clickListener(position)
}