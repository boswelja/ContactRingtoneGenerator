package com.boswelja.contactringtonegenerator.contacts

import org.junit.Assert.* // ktlint-disable
import org.junit.Test

class ContactTest {

    @Test
    fun testEquals() {
        val contact1 = Contact(1, "", "Name")
        val contact2 = Contact(1, "", "Different Name")
        val contact3 = Contact(2, "", "Name")
        assertEquals(contact1, contact2)
        assertNotEquals(contact1, contact3)
    }
}
