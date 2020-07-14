package com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter

import android.net.Uri
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.contactringtonegenerator.ringtonegen.item.AudioItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.holder.BaseViewHolder
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.holder.CustomAudioViewHolder
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.holder.CustomTextViewHolder
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.adapter.holder.NonDynamicViewHolder
import timber.log.Timber

class RingtoneCreatorAdapter(
        private val dataListener: DataEventListener,
        private val actionClickCallback: ActionClickCallback
) : RecyclerView.Adapter<BaseViewHolder>() {

    private val items: ArrayList<StructureItem> = ArrayList()
    private val itemValidList: ArrayList<Boolean> = ArrayList()

    override fun getItemCount(): Int = items.count()

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (!item.isUserAdjustable) NON_DYNAMIC_ITEM
        else {
            when (item) {
                is AudioItem -> CUSTOM_AUDIO_ITEM
                else -> CUSTOM_TEXT_ITEM
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            NON_DYNAMIC_ITEM -> NonDynamicViewHolder.from(parent)
            CUSTOM_AUDIO_ITEM -> CustomAudioViewHolder.from(this, parent)
            CUSTOM_TEXT_ITEM -> CustomTextViewHolder.from(this, parent)
            else -> throw IllegalArgumentException("Unsupported view type, does the adapter support the item type?")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is CustomAudioViewHolder -> holder.bind(item, actionClickCallback)
            else -> holder.bind(item)
        }
        if (!item.isUserAdjustable) setIsDataValid(position, true)
    }

    fun setIsDataValid(position: Int, isValid: Boolean) {
        if (position in itemValidList.indices) {
            if (itemValidList[position] != isValid) {
                itemValidList[position] = isValid
                dataListener.onDataValidityChanged(itemValidList.none { !it })
            }
        } else {
            itemValidList.add(position, isValid)
            dataListener.onDataValidityChanged(itemValidList.none { !it })
        }
    }

    fun getItem(position: Int): StructureItem = items[position]

    fun handleChooserResponse(uri: Uri?, position: Int) {
        Timber.i("Updating $position uri to $uri")
        if (position in items.indices) {
            val item = items[position]
            if (item is AudioItem) {
                item.audioUri = uri
                notifyItemChanged(position)
            }
        }
    }

    fun addItem(item: StructureItem) {
        if (items.add(item)) {
            notifyItemInserted(items.lastIndex)
            dataListener.onItemAdded(item)
        }
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val item = items.removeAt(fromPosition)
        items.add(toPosition, item)
        val isDataValid = itemValidList.removeAt(fromPosition)
        itemValidList.add(toPosition, isDataValid)
        notifyItemMoved(fromPosition, toPosition)
        dataListener.onItemMoved(fromPosition, toPosition)
        dataListener.onDataValidityChanged(itemValidList.none { !it })
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        itemValidList.removeAt(position)
        notifyItemRemoved(position)
        dataListener.onItemRemoved(position)
        dataListener.onDataValidityChanged(itemValidList.none { !it })
    }

    fun updateItems(newItems: List<StructureItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    interface DataEventListener {
        fun onItemAdded(item: StructureItem)
        fun onItemRemoved(position: Int)
        fun onItemMoved(fromPosition: Int, toPosition: Int)
        fun onDataValidityChanged(isDataValid: Boolean)
    }

    companion object {
        private const val NON_DYNAMIC_ITEM = 2
        private const val CUSTOM_AUDIO_ITEM = 3
        private const val CUSTOM_TEXT_ITEM = 4
    }
}
