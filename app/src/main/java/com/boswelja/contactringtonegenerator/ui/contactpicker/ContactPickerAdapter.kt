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

        return ContactViewHolder(ContactPickerRecyclerviewItemBinding.inflate(layoutInflater!!, parent, false), useNicknames)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = filteredContacts[position]
        val selected = selectedContacts.contains(contact)
        holder.apply {
            bind(contact)
            checkbox.isChecked = selected
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
        }
    }

    private fun sortContacts() {
        filteredContacts.sortBy {
            if (useNicknames) {
                it.nickname ?: it.displayName
            } else {
                it.displayName
            }
        }
    }

    override fun getFilter(): Filter = filter

    fun setSelectedContacts(newSelection: List<Contact>) {
        selectedContacts.clear()
        selectedContacts.addAll(newSelection)
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
    }

    fun selectAllContacts() {
        allContacts.minus(selectedContacts).forEach {
            selectedContacts.add(it)
            listener.onContactSelected(it)
        }
    }

    fun deselectAllContacts() {
        selectedContacts.forEach {
            listener.onContactDeselected(it)
        }
        selectedContacts.clear()
    }

    class ContactViewHolder(
        private val binding: ContactPickerRecyclerviewItemBinding,
        private val useNicknames: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {

        val checkbox = binding.checkbox

        var isChecked: Boolean get() = binding.checkbox.isChecked
            set(value) { binding.checkbox.isChecked = value }

        fun bind(contact: Contact) {
            binding.contactName.text = if (useNicknames) {
                contact.nickname ?: contact.displayName
            } else {
                contact.displayName
            }
            if (contact.photoUri != null) {
                binding.contactIcon.setImageURI(contact.photoUri)
            } else {
                binding.contactIcon.setImageResource(R.drawable.ic_default_contact)
            }
        }
    }

    inner class ItemFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val defaultData = allContacts
            val results = FilterResults()
            if (constraint != null) {
                val search = constraint.toString().toLowerCase(Locale.ROOT)
                val filteredData = defaultData.filter {
                    it.displayName.toLowerCase(Locale.ROOT).contains(search) ||
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
