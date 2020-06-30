package com.boswelja.contactringtonegenerator.ui.contactpicker

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.contacts.ContactsViewModel
import com.boswelja.contactringtonegenerator.databinding.ContactPickerWidgetBinding
import com.boswelja.contactringtonegenerator.ui.MainActivity
import com.boswelja.contactringtonegenerator.ui.WizardDataViewModel
import com.boswelja.contactringtonegenerator.ui.common.ListFragment

class ContactPickerFragment : ListFragment(), ContactSelectionListener {

    private val dataModel: WizardDataViewModel by activityViewModels()
    private val contactsModel: ContactsViewModel by activityViewModels()

    private val adapter: ContactPickerAdapter by lazy {
        ContactPickerAdapter(
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getBoolean("use_nicknames", true),
            this
        )
    }

    private lateinit var widgetBinding: ContactPickerWidgetBinding

    override fun onContactDeselected(contact: Contact) {
        dataModel.selectedContacts.remove(contact)
        updateSelectedContactsView()
        updateNextEnabled()
    }

    override fun onContactSelected(contact: Contact) {
        dataModel.selectedContacts.add(contact)
        updateSelectedContactsView()
        updateNextEnabled()
    }

    override fun onCreateWidgetView(): View? {
        widgetBinding = ContactPickerWidgetBinding.inflate(layoutInflater)
        widgetBinding.apply {
            checkBox.setOnCheckedChangeListener { _, checked ->
                if (checked) adapter.selectAllContacts()
                else adapter.deselectAllContacts()
            }
            searchView.doAfterTextChanged {
                setLoading(true)
                adapter.filter.filter(it.toString())
                setLoading(false)
            }
        }
        return widgetBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setLoading(true)
        adapter.setSelectedContacts(dataModel.selectedContacts)
        updateSelectedContactsView()
        updateNextEnabled()
        binding.apply {
            nextButton.setOnClickListener {
                findNavController().navigate(ContactPickerFragmentDirections.toRingtoneCreatorFragment())
            }
            recyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = this@ContactPickerFragment.adapter
            }
        }
        contactsModel.contacts.observe(viewLifecycleOwner) {
            updateContacts(it)
        }
    }

    override fun onStop() {
        super.onStop()
        removeSubtitle()
    }

    private fun updateContacts(contacts: List<Contact>) {
        adapter.setContacts(contacts)
        setLoading(false)
        widgetBinding.checkBox.isChecked = !adapter.canSelectAllContacts
    }

    private fun updateSelectedContactsView() {
        val count = dataModel.selectedContacts.count()
        val activity = requireActivity()
        if (activity is MainActivity) {
            activity.setSubtitle(resources.getQuantityString(R.plurals.selected_contacts_summary, count, count))
        }
    }

    private fun removeSubtitle() {
        val activity = requireActivity()
        if (activity is MainActivity) {
            activity.setSubtitle(null)
        }
    }

    private fun updateNextEnabled() {
        binding.nextButton.apply {
            isEnabled = dataModel.selectedContacts.isNotEmpty()
            if (isEnabled) extend()
            else shrink()
        }
    }
}
