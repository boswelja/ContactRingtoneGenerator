package com.boswelja.contactringtonegenerator.ui.contactpicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.databinding.FragmentContactPickerBinding
import com.boswelja.contactringtonegenerator.ui.ContactPickerFragmentDirections
import com.boswelja.contactringtonegenerator.ui.advanced.ContactPickerDialog

class ContactPickerFragment : Fragment(), ContactPickerDialog.DialogEventListener {

    private val selectedContacts = ArrayList<Contact>()

    private lateinit var binding: FragmentContactPickerBinding
    private lateinit var contactPicker: ContactPickerDialog

    override fun onContactsSelected(selectedContacts: List<Contact>) {
        this.selectedContacts.apply {
            clear()
            addAll(selectedContacts)
        }
        updateSelectedContactsView()
        updateNextEnabled()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactPicker = ContactPickerDialog().apply {
            dialogEventListeners.add(this@ContactPickerFragment)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentContactPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateSelectedContactsView()
        binding.selectContactsButton.setOnClickListener {
            contactPicker.show(parentFragmentManager)
        }
        binding.nextButton.setOnClickListener {
            findNavController().navigate(ContactPickerFragmentDirections.toRingtoneCreatorFragment())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        contactPicker.dialogEventListeners.remove(this)
    }

    private fun updateSelectedContactsView() {
        val count = selectedContacts.count()
        binding.selectionCountView.text =
                resources.getQuantityString(R.plurals.selected_contacts_summary, count, count)
    }

    private fun updateNextEnabled() {
        binding.nextButton.isEnabled = selectedContacts.isNotEmpty()
    }
}
