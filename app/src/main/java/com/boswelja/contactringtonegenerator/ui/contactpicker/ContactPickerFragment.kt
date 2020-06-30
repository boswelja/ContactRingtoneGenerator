package com.boswelja.contactringtonegenerator.ui.contactpicker

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat
import androidx.core.view.setPadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.contacts.ContactsViewModel
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

    private lateinit var searchBox: AppCompatEditText

    override fun onContactDeselected(contact: Contact) {
        updateSelectedContactsView()
        updateNextEnabled()
        dataModel.selectedContacts.remove(contact)
    }

    override fun onContactSelected(contact: Contact) {
        updateSelectedContactsView()
        updateNextEnabled()
        dataModel.selectedContacts.add(contact)
    }

    override fun onCreateWidgetView(): View? {
        val widgetPadding = resources.getDimensionPixelSize(R.dimen.list_widget_padding)
        searchBox = AppCompatEditText(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_search, 0, 0, 0)
            compoundDrawablePadding = widgetPadding
            setPadding(widgetPadding)
            isSingleLine = true
            setBackgroundResource(R.drawable.search_background)
            setHint(R.string.search_hint)
            doAfterTextChanged {
                setLoading(true)
                adapter.filter.filter(it.toString())
                setLoading(false)
            }
        }.also {
            ViewCompat.setElevation(it, resources.getDimension(R.dimen.list_widget_elevation))
        }
        return searchBox
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setLoading(true)
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
