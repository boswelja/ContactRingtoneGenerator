package com.boswelja.contactringtonegenerator.ui.contactpicker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
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
    private val selectedContacts = HashMap<Contact, Boolean>()
    private val allContactsSelected: MutableLiveData<Boolean> = MutableLiveData(canSelectAllContacts)

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
        val selected = selectedContacts[contact] ?: false
        holder.apply {
            bind(contact, allContactsSelected)
            checkbox.isChecked = selected
            itemView.setOnClickListener {
                if (holder.isChecked) {
                    selectedContacts[contact] = false
                    holder.isChecked = false
                    listener.onContactDeselected(contact)
                } else {
                    selectedContacts[contact] = true
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
        newSelection.forEach {
            selectedContacts[it] = true
        }
    }

    fun setContacts(newContacts: List<Contact>) {
        ArrayList(newContacts).apply {
            allContacts.retainAll(this)
            selectedContacts.keys.retainAll(newContacts)
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
        allContacts.minus(selectedContacts.keys).forEach {
            selectedContacts[it] = true
            listener.onContactSelected(it)
        }
        allContactsSelected.postValue(true)
    }

    fun deselectAllContacts() {
        selectedContacts.keys.forEach {
            listener.onContactDeselected(it)
        }
        selectedContacts.clear()
        allContactsSelected.postValue(false)
    }

    suspend fun filter(constraint: CharSequence) {
        withContext(Dispatchers.IO) {
            val newSearch = constraint.toString().toLowerCase(Locale.ROOT)
            val newData = allContacts.filter {
                it.displayName.toLowerCase(Locale.ROOT).contains(newSearch) ||
                    it.nickname?.toLowerCase(Locale.ROOT)?.contains(newSearch) ?: false
            }
            val oldData = filteredContacts.toList()
            filteredContacts.apply {
                clear()
                addAll(newData)
            }
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int = oldData.count()
                override fun getNewListSize(): Int = newData.count()

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                        oldData[oldItemPosition] == newData[newItemPosition]

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                        oldData[oldItemPosition] == newData[newItemPosition]
            })
            withContext(Dispatchers.Main) {
                diffResult.dispatchUpdatesTo(this@ContactPickerAdapter)
            }
        }
    }

    class ContactViewHolder(
        private val binding: ContactPickerRecyclerviewItemBinding,
        private val useNicknames: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {

        val checkbox = binding.checkbox

        var isChecked: Boolean get() = binding.checkbox.isChecked
            set(value) { binding.checkbox.isChecked = value }

        fun bind(contact: Contact, allSelectedLiveData: LiveData<Boolean>) {
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
            allSelectedLiveData.observe(checkbox.context as LifecycleOwner) {
                checkbox.isChecked = it
            }
        }
    }
}
