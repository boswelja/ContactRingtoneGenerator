package com.boswelja.contactringtonegenerator.ui.ringtonecreator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem

class RingtoneCreatorViewModel : ViewModel() {

    private val _ringtoneStructure = ArrayList<StructureItem>()
    val ringtoneStructure: List<StructureItem>
        get() = _ringtoneStructure

    val isDataValid = MutableLiveData(_ringtoneStructure.isNotEmpty())

    private val _isDataEmpty = MutableLiveData(true)
    val isDataEmpty: LiveData<Boolean>
        get() = _isDataEmpty

    fun addItem(item: StructureItem) {
        _ringtoneStructure.add(item)
        _isDataEmpty.postValue(false)
    }

    fun removeItemAtPosition(position: Int) {
        _ringtoneStructure.removeAt(position)
        _isDataEmpty.postValue(_ringtoneStructure.isEmpty())
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val item = _ringtoneStructure.removeAt(fromPosition)
        _ringtoneStructure.add(toPosition, item)
    }
}