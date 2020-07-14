package com.boswelja.contactringtonegenerator.ui.contactpicker

import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.ui.contactpicker.adapter.ContactPickerAdapter
import com.boswelja.contactringtonegenerator.ui.contactpicker.adapter.ContactSelectionListener
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.* // ktlint-disable
import org.junit.Before
import org.junit.Test

class ContactPickerAdapterTest {

    private val dummyContacts = listOf(
        Contact(0, "key1", "Bob Stuart", "Bobby"),
        Contact(1, "key2", "James Smith")
    )

    @MockK
    lateinit var selectionListener: ContactSelectionListener

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun testItemCountUpdated() {
        val adapter = createAdapter()
        assertEquals(0, adapter.itemCount)
        adapter.submitList(dummyContacts)
        assertEquals(dummyContacts.count(), adapter.itemCount)
    }

    @Test
    fun testSelectAll() {
        val adapter = createAdapter()
        adapter.submitList(dummyContacts)
        adapter.selectContacts(dummyContacts)
        dummyContacts.forEach {
            verify(exactly = 1) { selectionListener.onContactSelected(it.id) }
        }
        adapter.deselectAllContacts()
        dummyContacts.forEach {
            verify(exactly = 1) { selectionListener.onContactDeselected(it.id) }
        }
        confirmVerified(selectionListener)
    }

    private fun createAdapter(useNicknames: Boolean = true) =
        spyk(ContactPickerAdapter(useNicknames, selectionListener)).also {
            every { it.notifyDataSetChanged() } returns Unit
        }
}
