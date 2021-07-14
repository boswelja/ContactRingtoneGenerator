package com.boswelja.contactringtonegenerator.contactpicker.ui

import android.app.Application
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import com.boswelja.contactringtonegenerator.common.contacts.ContactsHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest

class ContactPickerViewModel(
    application: Application
) : AndroidViewModel(application) {

    @ExperimentalCoroutinesApi
    val allContacts = ContactsHelper.getContacts(application.contentResolver).mapLatest {
        it.map { contact ->
            val stream = ContactsHelper.openContactPhotoStream(getApplication(), contact)
            val imageBitmap = stream?.let {
                BitmapFactory.decodeStream(it).asImageBitmap()
            }
            Pair(imageBitmap, contact)
        }
    }
}
