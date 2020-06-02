package com.boswelja.contactringtonegenerator

import android.content.res.Resources
import android.util.TypedValue

object Extensions {
    val Int.dp: Float
        get() = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics)
}