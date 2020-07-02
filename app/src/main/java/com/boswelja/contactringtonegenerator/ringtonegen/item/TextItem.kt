package com.boswelja.contactringtonegenerator.ringtonegen.item

class TextItem : StructureItem(ID.TEXT_ITEM) {

    override val isDynamic: Boolean = true

    var text: String = ""

    override fun getLabel(): String {
        return "Custom Text"
    }

    override fun getEngineText(): String = text
}
