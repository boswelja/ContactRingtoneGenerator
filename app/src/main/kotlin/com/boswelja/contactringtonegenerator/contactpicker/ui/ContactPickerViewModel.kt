package com.boswelja.contactringtonegenerator.contactpicker.ui

import android.app.Application
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import com.boswelja.contactringtonegenerator.common.contacts.getContacts
import com.boswelja.contactringtonegenerator.common.contacts.openContactPhotoStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest

class ContactPickerViewModel(
    application: Application
) : AndroidViewModel(application) {

    @ExperimentalCoroutinesApi
    val allContacts = getContacts(application.contentResolver).mapLatest {
        it.map { contact ->
            val stream = openContactPhotoStream(getApplication(), contact)
            val imageBitmap = stream?.let {
                BitmapFactory.decodeStream(it).asImageBitmap()
            }
            Pair(imageBitmap, contact)
        }
    }
}
