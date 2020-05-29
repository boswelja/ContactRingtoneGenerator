package com.boswelja.contactringtonegenerator.ui.contactpicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.contacts.ContactManager
import com.boswelja.contactringtonegenerator.databinding.FragmentEasyModeListBinding

class ContactPickerFragment : Fragment(), ContactSelectionListener {

    private val selectedContacts = ArrayList<Contact>()

    private lateinit var binding: FragmentEasyModeListBinding

    override fun onContactDeselected(contact: Contact) {
        selectedContacts.remove(contact)
        updateSelectedContactsView()
        updateNextEnabled()
    }

    override fun onContactSelected(contact: Contact) {
        selectedContacts.add(contact)
        updateSelectedContactsView()
        updateNextEnabled()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEasyModeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateSelectedContactsView()
        updateNextEnabled()
        binding.apply {
            titleView.setText(R.string.contact_picker_title)
            nextButton.setOnClickListener {
                findNavController().navigate(ContactPickerFragmentDirections.toVoicePickerFragment())
            }
            recyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = ContactPickerAdapter(this@ContactPickerFragment).apply {
                    setUseNicknames(true)
                }
            }
        }
        updateContacts()
    }

    private fun updateContacts() {
        (binding.recyclerView.adapter as ContactPickerAdapter)
                .setContacts(ContactManager.getContacts(requireContext()))
    }

    private fun updateSelectedContactsView() {
        val count = selectedContacts.count()
        binding.subtitleView.text =
                resources.getQuantityString(R.plurals.selected_contacts_summary, count, count)
    }

    private fun updateNextEnabled() {
        binding.nextButton.isEnabled = selectedContacts.isNotEmpty()
    }
}
