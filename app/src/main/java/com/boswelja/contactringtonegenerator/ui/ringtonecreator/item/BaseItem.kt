package com.boswelja.contactringtonegenerator.ui.ringtonecreator.item

import java.util.Locale

abstract class BaseItem(val id: ID) {

    abstract val isDynamic: Boolean

    abstract fun getLabel(): String
    abstract fun getEngineText(): String

    fun getEngineId(): String =
            getEngineText().replace(" ", "_").toLowerCase(Locale.ROOT)
}
