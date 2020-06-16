package com.boswelja.contactringtonegenerator.ringtonegen.item

class ContactName : BaseItem(ID.CONTACT_NAME) {

    override fun getLabel(): String {
        return "Contact Name"
    }

    override fun getEngineText(): String = Constants.CONTACT_NAME_PLACEHOLDER
}