package com.boswelja.contactringtonegenerator.ui

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.contacts.ContactManager

class ContactPickerDialog : DialogFragment() {

    val dialogEventListeners = ArrayList<DialogEventListener>()

    private var contactsRecyclerView: RecyclerView? = null
    private lateinit var noContactsView: View
    private lateinit var loadingIndicator: ProgressBar

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(context, R.layout.contact_picker_dialog, null)
        contactsRecyclerView = view.findViewById<RecyclerView>(R.id.contacts_recyclerview).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = ContactPickerAdapter()
        }
        noContactsView = view.findViewById(R.id.no_contacts_view)
        loadingIndicator = view.findViewById(R.id.loading_indicator)
        return AlertDialog.Builder(context!!).apply {
            setTitle(R.string.contact_picker_dialog_title)
            setPositiveButton(R.string.dialog_button_done) { _, _ ->
                for (listener in dialogEventListeners) {
                    listener.onContactsSelected((contactsRecyclerView!!.adapter as ContactPickerAdapter).selectedContacts)
                }
            }
            setView(view)
        }.create()
    }

    override fun onResume() {
        super.onResume()

        setLoading(true)
        updateContacts()
        setLoading(false)
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, "ContactPickerDialog")
    }

    private fun updateContacts() {
        (contactsRecyclerView!!.adapter as ContactPickerAdapter)
            .setContacts(ContactManager.getContacts(context!!))
    }

    fun getSelectedContacts(): List<Contact> {
        return if (contactsRecyclerView != null) {
            (contactsRecyclerView?.adapter as ContactPickerAdapter).selectedContacts
        } else {
            ArrayList()
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            loadingIndicator.visibility = View.VISIBLE
            contactsRecyclerView!!.visibility = View.GONE
            noContactsView.visibility = View.GONE
        } else {
            loadingIndicator.visibility = View.GONE
            if ((contactsRecyclerView!!.adapter as ContactPickerAdapter).itemCount > 0) {
                noContactsView.visibility = View.GONE
                contactsRecyclerView!!.visibility = View.VISIBLE
            } else {
                noContactsView.visibility = View.VISIBLE
                contactsRecyclerView!!.visibility = View.GONE
            }
        }
    }

    interface DialogEventListener {
        fun onContactsSelected(selectedContacts: List<Contact>)
    }
}