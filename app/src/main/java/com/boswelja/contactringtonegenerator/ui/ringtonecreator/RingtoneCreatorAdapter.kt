package com.boswelja.contactringtonegenerator.ui.ringtonecreator

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.contactringtonegenerator.databinding.RingtoneCreatorItemBinding
import com.boswelja.contactringtonegenerator.ringtonegen.item.StructureItem

class RingtoneCreatorAdapter(private val listener: DataEventListener) :
    RecyclerView.Adapter<BaseViewHolder>() {

    private val items: ArrayList<StructureItem> = ArrayList()
    private val isDataValid: ArrayList<Boolean> = ArrayList()

    private var layoutInflater: LayoutInflater? = null

    override fun getItemCount(): Int = items.count()

    override fun getItemViewType(position: Int): Int =
            if (getItem(position).isDynamic) DYNAMIC_ITEM
            else NON_DYNAMIC_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        if (layoutInflater == null) layoutInflater = LayoutInflater.from(parent.context)
        val itemBinding = RingtoneCreatorItemBinding.inflate(layoutInflater!!, parent, false)
        return when (viewType) {
            NON_DYNAMIC_ITEM -> NonDynamicViewHolder(itemBinding)
            else -> {
                CustomTextViewHolder(this, itemBinding)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val contact = getItem(position)
        holder.bind(contact)
        if (!contact.isDynamic) setIsDataValid(position, true)
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
        private const val DYNAMIC_ITEM = 1
        private const val NON_DYNAMIC_ITEM = 2
    }
}
