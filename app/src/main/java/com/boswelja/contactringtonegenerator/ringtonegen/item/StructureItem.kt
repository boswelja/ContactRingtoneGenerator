package com.boswelja.contactringtonegenerator.ringtonegen.item

import androidx.compose.ui.graphics.vector.ImageVector

abstract class StructureItem(val id: ID) {

    abstract val icon: ImageVector
    abstract val labelRes: Int

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
