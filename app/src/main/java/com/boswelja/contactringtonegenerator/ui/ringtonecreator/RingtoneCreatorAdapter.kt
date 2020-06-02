package com.boswelja.contactringtonegenerator.ui.ringtonecreator

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorItemBinding
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.item.BaseItem
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.item.ID

class RingtoneCreatorAdapter(private val listener: ItemEventListener) :
        RecyclerView.Adapter<BaseViewHolder>() {

    private val items: ArrayList<BaseItem> = ArrayList()

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
                CustomTextViewHolder(itemBinding)
            }
            else -> throw Exception("Invalid item in adapter")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun getItem(position: Int): BaseItem = items[position]

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
        notifyItemRemoved(position)
        listener.onItemRemoved(items.isEmpty())
    }

    interface ItemEventListener {
        fun onItemAdded()
        fun onItemRemoved(isEmpty: Boolean)
    }
}