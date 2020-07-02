package com.boswelja.contactringtonegenerator.ringtonegen.item

class TextItem : StructureItem(ID.TEXT_ITEM) {

    override val isDynamic: Boolean = true

    var text: String = ""

    override fun getLabel(): String = label
    override fun getEngineText(): String = text

    companion object {
        const val label: String = "Custom Text"
    }
}
