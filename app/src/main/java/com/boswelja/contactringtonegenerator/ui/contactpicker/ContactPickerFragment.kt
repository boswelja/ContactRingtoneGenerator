package com.boswelja.contactringtonegenerator.ui.contactpicker

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat
import androidx.core.view.setPadding
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.contacts.ContactsHelper
import com.boswelja.contactringtonegenerator.ui.MainActivity
import com.boswelja.contactringtonegenerator.ui.common.ListFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactPickerFragment : ListFragment<ArrayList<Contact>>(), ContactSelectionListener {

    private val selectedContacts = ArrayList<Contact>()
    private val coroutineScope = MainScope()
    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }
    private val adapter: ContactPickerAdapter by lazy {
        ContactPickerAdapter(sharedPreferences.getBoolean("use_nicknames", true), this)
    }

    private lateinit var searchBox: AppCompatEditText

    override fun requestData(): ArrayList<Contact>? = adapter.getSelectedContacts()

    override fun onSaveData(activity: MainActivity, data: ArrayList<Contact>) {
        activity.selectedContacts.apply {
            clear()
            addAll(data)
        }
    }

    override fun onContactDeselected(contact: Contact) {
        selectedContacts.remove(contact)
        updateSelectedContactsView()
        updateNextEnabled()
        saveData()
    }

    override fun onContactSelected(contact: Contact) {
        selectedContacts.add(contact)
        updateSelectedContactsView()
        updateNextEnabled()
        saveData()
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
        updateContacts()
    }

    override fun onStop() {
        super.onStop()
        removeSubtitle()
    }

    private fun updateContacts() {
        coroutineScope.launch(Dispatchers.IO) {
            val contacts = ContactsHelper.getContacts(requireContext())
            withContext(Dispatchers.Main) {
                adapter.setContacts(contacts)
                setLoading(false)
            }
        }
    }

    private fun updateSelectedContactsView() {
        val count = selectedContacts.count()
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
        binding.nextButton.isEnabled = selectedContacts.isNotEmpty()
    }
}
