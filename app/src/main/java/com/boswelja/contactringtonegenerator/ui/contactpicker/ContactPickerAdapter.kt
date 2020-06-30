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

class ContactPickerAdapter(private val useNicknames: Boolean, private val listener: ContactSelectionListener) :
    RecyclerView.Adapter<ContactPickerAdapter.ContactViewHolder>(), Filterable {

    private var layoutInflater: LayoutInflater? = null

    private val allContacts = ArrayList<Contact>()
    private val filteredContacts = ArrayList<Contact>(allContacts)
    private val selectedContacts = ArrayList<Contact>()
    private val filter = ItemFilter()

    val canSelectAllContacts: Boolean get() = selectedContacts.count() < allContacts.count()

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
                contact.nickname ?: contact.name
            } else {
                contact.name
            }
            itemView.setOnClickListener {
                if (holder.isChecked) {
                    selectedContacts.remove(contact)
                    holder.isChecked = false
                    listener.onContactDeselected(contact)
                } else {
                    selectedContacts.add(contact)
                    holder.isChecked = true
                    listener.onContactSelected(contact)
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
                it.nickname ?: it.name
            } else {
                it.name
            }
        }
    }

    override fun getFilter(): Filter = filter

    fun getSelectedContacts(): ArrayList<Contact> = selectedContacts

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

    fun selectAllContacts() {
        allContacts.minus(selectedContacts).forEach {
            selectedContacts.add(it)
            listener.onContactSelected(it)
        }
        notifyDataSetChanged()
    }

    fun deselectAllContacts() {
        selectedContacts.forEach {
            listener.onContactDeselected(it)
        }
        selectedContacts.clear()
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
                    it.name.toLowerCase(Locale.ROOT).contains(search) ||
                        it.nickname?.toLowerCase(Locale.ROOT)?.contains(search) ?: false
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
