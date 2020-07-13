package com.boswelja.contactringtonegenerator.ui.contactpicker

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.databinding.ContactPickerWidgetBinding
import com.boswelja.contactringtonegenerator.ui.MainActivity
import com.boswelja.contactringtonegenerator.ui.WizardDataViewModel
import com.boswelja.contactringtonegenerator.ui.common.ListFragment
import com.boswelja.contactringtonegenerator.ui.contactpicker.adapter.ContactPickerAdapter
import com.boswelja.contactringtonegenerator.ui.contactpicker.adapter.ContactSelectionListener

class ContactPickerFragment : ListFragment(), ContactSelectionListener {

    private val dataModel: WizardDataViewModel by activityViewModels()
    private val viewModel: ContactsViewModel by activityViewModels()
    private val searchHandler = Handler(Looper.myLooper()!!)
    private val searchRunnable = Runnable {
        viewModel.filterContacts(searchQuery)
    }

    private val adapter: ContactPickerAdapter by lazy {
        ContactPickerAdapter(
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getBoolean("use_nicknames", true),
            this
        )
    }

    private lateinit var widgetBinding: ContactPickerWidgetBinding

    private var searchQuery: CharSequence? = null

    override fun onContactDeselected(contactId: Long) {
        dataModel.selectedContacts.removeAll { it.id == contactId }
        widgetBinding.checkBox.isChecked = false
        updateSelectedContactsView()
        updateNextEnabled()
    }

    override fun onContactSelected(contactId: Long) {
        dataModel.selectedContacts.add(viewModel.allContacts.first { it.id == contactId })
        updateSelectedContactsView()
        updateNextEnabled()
    }

    override fun onCreateWidgetView(): View? {
        widgetBinding = ContactPickerWidgetBinding.inflate(layoutInflater)
        widgetBinding.apply {
            checkBox.setOnCheckedChangeListener { _, checked ->
                if (checked) adapter.setSelectedContacts(viewModel.allContacts)
                else adapter.deselectAllContacts()
            }
            searchView.doAfterTextChanged {
                setLoading(true)
                searchQuery = it.toString()
                startContactSearchTimer()
            }
        }
        return widgetBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setLoading(true)
        adapter.setSelectedContacts(dataModel.selectedContacts)
        updateSelectedContactsView()
        updateNextEnabled()
        binding.recyclerView.adapter = this@ContactPickerFragment.adapter
        binding.nextButton.setOnClickListener {
            findNavController().navigate(ContactPickerFragmentDirections.toRingtoneCreatorFragment())
        }

        viewModel.adapterContacts.observe(viewLifecycleOwner) {
            updateContacts(it)
        }
    }

    override fun onStop() {
        super.onStop()
        removeSubtitle()
    }

    override fun setLoading(loading: Boolean) {
        super.setLoading(loading)
        widgetBinding.apply {
            checkBox.isEnabled = !loading
        }
    }

    private fun startContactSearchTimer() {
        searchHandler.removeCallbacks(searchRunnable)
        searchHandler.postDelayed(searchRunnable, SEARCH_TIMER_MILLIS)
    }

    private fun updateContacts(contacts: List<Contact>) {
        adapter.submitList(contacts)
        setLoading(false)
        widgetBinding.apply {
            checkBox.isChecked = !adapter.canSelectAllContacts
            searchView.isEnabled = true
        }
        adapter.notifyDataSetChanged()
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

    companion object {
        private const val SEARCH_TIMER_MILLIS: Long = 300
    }
}
