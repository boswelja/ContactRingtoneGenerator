package com.boswelja.contactringtonegenerator.ringtonebuilder

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.boswelja.contactringtonegenerator.ringtonegen.item.StructureItem

class RingtoneCreatorViewModel : ViewModel() {

    val ringtoneStructure = mutableStateListOf<StructureItem<*>>()
    val isDataValid: Boolean
        get() = ringtoneStructure.all { it.isDataValid }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val item = ringtoneStructure.removeAt(fromPosition)
        ringtoneStructure.add(toPosition, item)
    }
}
