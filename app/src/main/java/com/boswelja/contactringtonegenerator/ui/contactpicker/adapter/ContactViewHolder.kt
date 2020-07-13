package com.boswelja.contactringtonegenerator.ui.contactpicker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.databinding.ContactPickerRecyclerviewItemBinding

class ContactViewHolder(
        private val binding: ContactPickerRecyclerviewItemBinding,
        private val useNicknames: Boolean
) : RecyclerView.ViewHolder(binding.root) {

    val checkbox = binding.checkbox

    fun bind(contact: Contact, clickListener: ContactClickListener, allSelectedLiveData: LiveData<Boolean>) {
        binding.showNickname = useNicknames
        binding.contact = contact
        binding.clickListener = clickListener
        binding.position = adapterPosition
        binding.executePendingBindings()

        allSelectedLiveData.observe(checkbox.context as LifecycleOwner) {
            checkbox.isChecked = it
        }
    }

    companion object {
        fun from(parent: ViewGroup, useNicknames: Boolean): ContactViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ContactPickerRecyclerviewItemBinding.inflate(layoutInflater, parent, false)
            return ContactViewHolder(binding, useNicknames)
        }
    }
}