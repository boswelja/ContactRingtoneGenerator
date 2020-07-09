package com.boswelja.contactringtonegenerator.ui.ringtonecreator

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorCustomAudioWidgetBinding
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorItemBinding
import com.boswelja.contactringtonegenerator.ringtonegen.item.CustomAudio
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.AudioItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.holder.BaseViewHolder
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.holder.CustomAudioViewHolder
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.holder.CustomTextViewHolder
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.holder.NonDynamicViewHolder

class RingtoneCreatorAdapter(
    private val fragment: RingtoneCreatorFragment,
    private val listener: DataEventListener
) : RecyclerView.Adapter<BaseViewHolder>() {

    private val items: ArrayList<StructureItem> = ArrayList()
    private val isDataValid: ArrayList<Boolean> = ArrayList()
    private val fileChooserIntent by lazy {
        Intent.createChooser(
            Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "audio/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            },
            "Pick an audio file"
        )
    }

    private var layoutInflater: LayoutInflater? = null
    private var startedChooserPosition = -1

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
        if (layoutInflater == null) layoutInflater = LayoutInflater.from(parent.context)
        val itemBinding = RingtoneCreatorItemBinding.inflate(layoutInflater!!, parent, false)
        return when (viewType) {
            NON_DYNAMIC_ITEM -> NonDynamicViewHolder(itemBinding)
            CUSTOM_AUDIO_ITEM ->
                CustomAudioViewHolder(
                    this,
                    RingtoneCreatorCustomAudioWidgetBinding.inflate(layoutInflater!!, parent, false),
                    itemBinding
                )
            CUSTOM_TEXT_ITEM -> CustomTextViewHolder(this, itemBinding)
            else -> throw IllegalArgumentException("Unsupported view type, does the adapter support the item type?")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        if (!item.isUserAdjustable) setIsDataValid(position, true)
    }

    fun setIsDataValid(position: Int, isValid: Boolean) {
        if (position in isDataValid.indices) {
            if (isDataValid[position] != isValid) {
                isDataValid[position] = isValid
                listener.onDataValidityChanged(isDataValid.none { !it })
            }
        } else {
            isDataValid.add(position, isValid)
            listener.onDataValidityChanged(isDataValid.none { !it })
        }
    }

    fun getItem(position: Int): StructureItem = items[position]

    fun startChooserForResult(position: Int) {
        startedChooserPosition = position
        fragment.startActivityForResult(fileChooserIntent, CHOOSER_REQUEST_CODE)
    }

    fun handleChooserResponse(data: Intent?) {
        if (startedChooserPosition in items.indices) {
            val item = items[startedChooserPosition]
            if (item is CustomAudio) {
                val uri = data?.data
                item.audioUri = uri
                notifyItemChanged(startedChooserPosition)
            }
            startedChooserPosition = -1
        }
    }

    fun addItem(item: StructureItem) {
        if (items.add(item)) {
            notifyItemInserted(items.lastIndex)
            listener.onItemAdded(item)
        }
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val item = items.removeAt(fromPosition)
        items.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
        listener.onItemMoved(fromPosition, toPosition)
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        isDataValid.removeAt(position)
        notifyItemRemoved(position)
        listener.onItemRemoved(position)
        listener.onDataValidityChanged(isDataValid.none { !it })
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

        const val CHOOSER_REQUEST_CODE = 62931
    }
}
