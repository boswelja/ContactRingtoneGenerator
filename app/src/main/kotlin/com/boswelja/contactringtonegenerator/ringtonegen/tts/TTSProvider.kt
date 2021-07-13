package com.boswelja.contactringtonegenerator.ringtonegen.tts

import android.content.Context
import java.io.File

/**
 * An interface for calling TTS functions used by the ringtone generator.
 */
interface TTSProvider {

    /**
     * Initialise the associated TTS engine with the given parameters.
     * @param context [Context].
     * @param speechRate The speech rate multiplier for speech synthesis.
     * @param voicePitch The pitch shift for speech synthesis.
     * @return true if TTS was initialised successfully, false otherwise.
     */
    suspend fun initialise(
        context: Context,
        speechRate: Float = 1f,
        voicePitch: Float = 1f
    ): Boolean

    /**
     * Synthesize some text to a file.
     * @param text The text to synthesize.
     * @param file The file to store the synthesized text.
     * @return true if synthesis was successful, false otherwise.
     */
    suspend fun synthesizeToFile(text: String, file: File): Boolean

    /**
     * Shut down the associated TTS engine.
     */
    suspend fun shutdown()
}
