package com.boswelja.contactringtonegenerator.ui.contactpicker

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.databinding.ContactPickerRecyclerviewItemBinding
import java.util.Locale

class ContactPickerAdapter(private val listener: ContactSelectionListener? = null) :
        RecyclerView.Adapter<ContactPickerAdapter.ContactViewHolder>(), Filterable {

    private var layoutInflater: LayoutInflater? = null
    private var useNicknames: Boolean = true

    private val allContacts = ArrayList<Contact>()
    private val filteredContacts = ArrayList<Contact>(allContacts)
    private val selectedContacts = ArrayList<Contact>()
    private val filter = ItemFilter()

    override fun getItemCount(): Int = filteredContacts.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.context)
        }

        return ContactViewHolder(ContactPickerRecyclerviewItemBinding.inflate(layoutInflater!!, parent, false))
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = filteredContacts[position]
        val selected = selectedContacts.contains(contact)
        holder.apply {
            contactName.text = if (useNicknames) {
                contact.contactNickname ?: contact.contactName
            } else {
                contact.contactName
            }
            itemView.setOnClickListener {
                if (holder.isChecked) {
                    selectedContacts.remove(contact)
                    holder.isChecked = false
                    listener?.onContactDeselected(contact)
                } else {
                    selectedContacts.add(contact)
                    holder.isChecked = true
                    listener?.onContactSelected(contact)
                }
            }
            checkbox.isChecked = selected
            if (contact.photoUri != null) {
                contactIcon.setImageURI(contact.photoUri)
            } else {
                contactIcon.setImageResource(R.drawable.ic_default_contact)
            }
        }
    }

    private fun sortContacts() {
        filteredContacts.sortBy {
            if (useNicknames) {
                it.contactNickname ?: it.contactName
            } else {
                it.contactName
            }
        }
    }

    override fun getFilter(): Filter = filter

    fun getSelectedContacts(): ArrayList<Contact> = selectedContacts

    fun setUseNicknames(useNicknames: Boolean) {
        if (this.useNicknames != useNicknames) {
            this.useNicknames = useNicknames
            sortContacts()
            notifyDataSetChanged()
        }
    }

    fun setContacts(newContacts: List<Contact>) {
        ArrayList(newContacts).apply {
            allContacts.retainAll(this)
            selectedContacts.retainAll(this)
            removeAll(allContacts)
            forEach {
                allContacts.add(it)
            }
        }
        filteredContacts.clear()
        filteredContacts.addAll(newContacts)
        sortContacts()
        notifyDataSetChanged()
    }

    class ContactViewHolder(private val binding: ContactPickerRecyclerviewItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

        val contactName = binding.contactName
        val contactIcon = binding.contactIcon
        val checkbox = binding.checkbox

        var isChecked: Boolean get() = binding.checkbox.isChecked
        set(value) { binding.checkbox.isChecked = value }
    }

    inner class ItemFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val defaultData = allContacts
            val results = FilterResults()
            if (constraint != null) {
                val search = constraint.toString().toLowerCase(Locale.ROOT)
                val filteredData = defaultData.filter {
                    it.contactName.toLowerCase(Locale.ROOT).contains(search) ||
                            it.contactNickname?.toLowerCase(Locale.ROOT)?.contains(search) ?: false
                }
                results.values = filteredData
            } else {
                results.values = defaultData
            }
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results != null) {
                val list = results.values
                if (list is List<*>) {
                    val values = list.filterIsInstance<Contact>()
                    filteredContacts.clear()
                    filteredContacts.addAll(values)
                    notifyDataSetChanged()
                }
            }
        }
    }
}