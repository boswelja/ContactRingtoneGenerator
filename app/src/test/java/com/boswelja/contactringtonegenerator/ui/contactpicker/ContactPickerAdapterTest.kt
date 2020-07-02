package com.boswelja.contactringtonegenerator.ui.contactpicker

import com.boswelja.contactringtonegenerator.contacts.Contact
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
        adapter.setContacts(dummyContacts)
        assertEquals(dummyContacts.count(), adapter.itemCount)
    }

    @Test
    fun testSelectAll() {
        val adapter = createAdapter()
        adapter.setContacts(dummyContacts)
        adapter.selectAllContacts()
        dummyContacts.forEach {
            verify(exactly = 1) { selectionListener.onContactSelected(it) }
        }
        adapter.deselectAllContacts()
        dummyContacts.forEach {
            verify(exactly = 1) { selectionListener.onContactDeselected(it) }
        }
        confirmVerified(selectionListener)
    }

    private fun createAdapter(useNicknames: Boolean = true) =
        spyk(ContactPickerAdapter(useNicknames, selectionListener)).also {
            every { it.notifyDataSetChanged() } returns Unit
        }
}
