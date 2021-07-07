package com.boswelja.contactringtonegenerator.common

import androidx.compose.runtime.compositionLocalOf

val LocalSearchComposition = compositionLocalOf<String> {
    error("No string provided")
}
