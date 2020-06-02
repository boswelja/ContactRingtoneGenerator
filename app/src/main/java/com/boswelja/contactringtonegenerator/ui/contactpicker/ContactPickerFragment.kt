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
import com.boswelja.contactringtonegenerator.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactPickerFragment : Fragment(), ContactSelectionListener {

    private val selectedContacts = ArrayList<Contact>()
    private val coroutineScope = MainScope()

    private lateinit var binding: FragmentEasyModeListBinding
    private val adapter = ContactPickerAdapter(this)

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
        setLoading(true)
        updateSelectedContactsView()
        updateNextEnabled()
        binding.apply {
            nextButton.setOnClickListener {
                findNavController().navigate(ContactPickerFragmentDirections.toVoicePickerFragment())
            }
            recyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = this@ContactPickerFragment.adapter.apply {
                    setUseNicknames(true)
                }
            }
        }
        updateContacts()
    }

    override fun onStop() {
        super.onStop()
        removeSubtitle()
    }

    private fun setLoading(loading: Boolean) {
        binding.apply {
            if (loading) {
                loadingSpinner.visibility = View.VISIBLE
                recyclerView.visibility = View.INVISIBLE
            } else {
                loadingSpinner.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun updateContacts() {
        coroutineScope.launch(Dispatchers.IO) {
            val contacts = ContactManager.getContacts(requireContext())
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
