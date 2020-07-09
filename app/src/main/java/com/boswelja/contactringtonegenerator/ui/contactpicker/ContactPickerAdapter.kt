package com.boswelja.contactringtonegenerator.ui.contactpicker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.databinding.ContactPickerRecyclerviewItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class ContactPickerAdapter(
    private val useNicknames: Boolean,
    private val listener: ContactSelectionListener
) : RecyclerView.Adapter<ContactPickerAdapter.ContactViewHolder>() {

    private var layoutInflater: LayoutInflater? = null

    private val allContacts = ArrayList<Contact>()
    private val filteredContacts = ArrayList<Contact>(allContacts)
    private val selectedContacts = ArrayList<Contact>()

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

    suspend fun filter(constraint: CharSequence) {
        withContext(Dispatchers.IO) {
            val search = constraint.toString().toLowerCase(Locale.ROOT)
            val filteredData = allContacts.filter {
                it.displayName.toLowerCase(Locale.ROOT).contains(search) ||
                    it.nickname?.toLowerCase(Locale.ROOT)?.contains(search) ?: false
            }
            filteredContacts.apply {
                clear()
                addAll(filteredData)
            }
        }
        withContext(Dispatchers.Main) {
            notifyDataSetChanged()
        }
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
}
