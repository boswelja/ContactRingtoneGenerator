package com.boswelja.contactringtonegenerator.ui.voicepicker

import android.speech.tts.Voice

interface VoiceSelectedCallback {

    fun onSelected(item: Voice)
    fun onPreview(item: Voice)
}