package com.boswelja.contactringtonegenerator.ui.contactpicker

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.boswelja.contactringtonegenerator.R

@BindingAdapter("srcUri")
fun ImageView.setImageUri(uri: Uri?) {
    if (uri != null) {
        setImageURI(uri)
    } else {
        setImageResource(R.drawable.ic_default_contact)
    }
}
