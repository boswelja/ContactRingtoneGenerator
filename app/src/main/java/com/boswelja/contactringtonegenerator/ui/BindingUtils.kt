package com.boswelja.contactringtonegenerator.ui

import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter

@BindingAdapter("srcRes")
fun ImageView.setImageRes(@DrawableRes drawableRes: Int) {
    if (drawableRes != 0) setImageResource(drawableRes)
}

@BindingAdapter("textRes")
fun TextView.setTextRes(@StringRes stringRes: Int) {
    if (stringRes != 0) setText(stringRes)
}
