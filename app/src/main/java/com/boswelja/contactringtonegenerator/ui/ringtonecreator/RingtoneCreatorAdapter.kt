package com.boswelja.contactringtonegenerator.ui.ringtonecreator

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorItemBinding
import com.boswelja.contactringtonegenerator.ringtonegen.item.BaseItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.ID

class RingtoneCreatorAdapter(private val listener: DataEventListener) :
        RecyclerView.Adapter<BaseViewHolder>() {

    private val items: ArrayList<BaseItem> = ArrayList()
    private val isDataValid: ArrayList<Boolean> = ArrayList()

    private var layoutInflater: LayoutInflater? = null

    override fun getItemCount(): Int = items.count()

    override fun getItemViewType(position: Int): Int {
        return getItem(position).id.id.toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        if (layoutInflater == null) layoutInflater = LayoutInflater.from(parent.context)
        val itemBinding = RingtoneCreatorItemBinding.inflate(layoutInflater!!, parent, false)
        return when (viewType) {
            ID.CONTACT_NAME.id.toInt() -> {
                ContactNameViewHolder(itemBinding)
            }
            ID.TEXT_ITEM.id.toInt() -> {
                CustomTextViewHolder(this, itemBinding)
            }
            else -> throw Exception("Invalid item in adapter")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(getItem(position))
        when (holder) {
            is ContactNameViewHolder -> {
                setIsDataValid(position, true)
            }
        }
    }

    fun getItems(): ArrayList<BaseItem> = items

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

    fun getItem(position: Int): BaseItem = items[position]

    fun addItem(item: BaseItem) {
        if (items.add(item)) {
            listener.onItemAdded()
            notifyItemInserted(items.lastIndex)
        }
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val item = items.removeAt(fromPosition)
        items.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        isDataValid.removeAt(position)
        notifyItemRemoved(position)
        listener.onItemRemoved(items.isEmpty())
        listener.onDataValidityChanged(isDataValid.none { !it })
    }

    interface DataEventListener {
        fun onItemAdded()
        fun onItemRemoved(isEmpty: Boolean)
        fun onDataValidityChanged(isDataValid: Boolean)
    }
}