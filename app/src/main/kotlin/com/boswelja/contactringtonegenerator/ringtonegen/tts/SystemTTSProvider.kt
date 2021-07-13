package com.boswelja.contactringtonegenerator.ringtonegen.tts

import android.content.Context
import com.boswelja.tts.Result
import com.boswelja.tts.TextToSpeech
import com.boswelja.tts.getTextToSpeech
import java.io.File

class SystemTTSProvider : TTSProvider {

    private var tts: TextToSpeech? = null

    override suspend fun initialise(
        context: Context,
        speechRate: Float,
        voicePitch: Float
    ): Boolean {
        return try {
            tts = context.getTextToSpeech(
                speechRate = speechRate,
                voicePitch = voicePitch
            )
            true
        } catch (e: IllegalStateException) {
            false
        }
    }

    override suspend fun synthesizeToFile(text: String, file: File): Boolean {
        return tts?.let {
            val result = it.synthesizeToFile(text, file)
            result == Result.SUCCESS
        } ?: false
    }

    override suspend fun shutdown() {
        tts?.shutdown()
    }
}
