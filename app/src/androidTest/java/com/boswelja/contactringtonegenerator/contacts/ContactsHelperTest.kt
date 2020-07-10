package com.boswelja.contactringtonegenerator.contacts

import android.provider.ContactsContract
import androidx.test.core.app.ApplicationProvider
import com.boswelja.contactringtonegenerator.MainApplication
import kotlinx.coroutines.runBlocking
import org.junit.Assert.* // ktlint-disable
import org.junit.Test

class ContactsHelperTest {

    private val context = ApplicationProvider.getApplicationContext<MainApplication>()

    @Test
    fun getContacts() = runBlocking {
        val trueContactsCount = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            ContactsHelper.CONTACTS_PROJECTION, null, null, null
        )?.count ?: 0
        val contacts = ContactsHelper.getContacts(context)
        assertEquals(trueContactsCount, contacts.count())
    }
}
