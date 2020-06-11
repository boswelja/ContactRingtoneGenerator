package com.boswelja.contactringtonegenerator.ringtonegen.item

import java.util.Locale

abstract class BaseItem(val id: ID) {

    abstract fun getLabel(): String
    abstract fun getEngineText(): String

    fun getEngineId(): String =
            getEngineText().replace(" ", "_").toLowerCase(Locale.ROOT)
}
