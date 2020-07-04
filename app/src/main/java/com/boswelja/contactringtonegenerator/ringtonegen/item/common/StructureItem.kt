package com.boswelja.contactringtonegenerator.ringtonegen.item.common

import com.boswelja.contactringtonegenerator.ringtonegen.item.ID

abstract class StructureItem(val id: ID) {

    /**
     * Indicates whether this [StructureItem] is a user-adjustable item.
     */
    abstract val isUserAdjustable: Boolean

    abstract fun getLabelRes(): Int
    abstract fun getIconRes(): Int

    override fun equals(other: Any?): Boolean {
        if (other is StructureItem) {
            return other.id == id
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
