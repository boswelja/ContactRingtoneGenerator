package com.boswelja.contactringtonegenerator.ui.contactpicker

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ContactPickerFragment : ListFragment(), ContactSelectionListener {

    private val dataModel: WizardDataViewModel by activityViewModels()
    private val contactsModel: ContactsViewModel by activityViewModels()
    private val coroutineScope = MainScope()
    private val searchHandler = Handler(Looper.myLooper()!!)
    private val searchRunnable = Runnable {
        coroutineScope.launch(Dispatchers.Default) {
            searchQuery?.let { searchQuery ->
                val query = searchQuery.toString().toLowerCase(Locale.ROOT)
                val filteredList = contactsModel.contacts.value?.filter {
                    it.displayName.toLowerCase(Locale.ROOT).contains(query) ||
                        it.displayName.toLowerCase(Locale.ROOT).contains(query)
                }
                adapter.submitList(filteredList)
            }
            withContext(Dispatchers.Main) {
                setLoading(false)
            }
        }
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

    override fun onContactDeselected(contact: Contact) {
        dataModel.selectedContacts.remove(contact)
        widgetBinding.checkBox.isChecked = false
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
                if (checked) adapter.setSelectedContacts(contactsModel.contacts.value!!)
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
