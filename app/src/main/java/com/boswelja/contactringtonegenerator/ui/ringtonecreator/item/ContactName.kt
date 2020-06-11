package com.boswelja.contactringtonegenerator.ui.ringtonecreator.item

class ContactName : BaseItem(ID.CONTACT_NAME) {

    override val isDynamic: Boolean = true

    override fun getLabel(): String {
        return "Contact Name"
    }

    override fun getEngineText(): String = "contact_name"
}