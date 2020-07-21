package com.boswelja.contactringtonegenerator.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.SUCCESS
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * A class to handle queueing and synthesizing jobs through a [TextToSpeech] instance.
 * @param context The [Context] to run everything with.
 */
class TtsManager(context: Context) :
    TextToSpeech.OnInitListener,
    UtteranceProgressListener() {

    private val cacheDirectory = context.cacheDir
    private val completedJobIds = ArrayList<String?>()
    private val tts: TextToSpeech = TextToSpeech(context, this)

    var engineEventListener: EngineEventListener? = null

    /**
     * Whether the TTS engine is ready.
     */
    var isEngineReady: Boolean = false
        private set(value) {
            if (field != value) {
                field = value
            }
            engineEventListener?.onInitialised(value)
        }

    override fun onInit(status: Int) {
        Timber.d("Init $status")
        isEngineReady = status == SUCCESS
        if (isEngineReady) {
            tts.setOnUtteranceProgressListener(this)
        }
    }

    override fun onStart(utteranceId: String?) {
        Timber.d("onStart($utteranceId) called")
    }

    override fun onDone(utteranceId: String?) {
        completedJobIds.add(utteranceId)
    }

    override fun onError(utteranceId: String?) {
        completedJobIds.add(utteranceId)
    }

    /**
     * Requests a [SynthesisJob] to be synthesized to a file as soon as possible.
     * @param synthesisJob The [SynthesisJob] to synthesise.
     */
    suspend fun synthesizeToFile(synthesisJob: SynthesisJob): SynthesisResult {
        return withContext(Dispatchers.Default) {
            val id = synthesisJob.id

            val file = File(cacheDirectory, "${id.replace(" ", "-")}.ogg")
            withContext(Dispatchers.IO) {
                if (!file.exists()) file.createNewFile()
            }

            val result = SynthesisResult(id, file)
            tts.synthesizeToFile(synthesisJob.text, null, file, id)
            val startTime = System.currentTimeMillis()

            // Wait for synthesis completion
            while (!completedJobIds.contains(id)) {
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed > TimeUnit.SECONDS.toMillis(MAX_TASK_TIMEOUT_SECONDS)) {
                    throw Exception("TTS timed out waiting for $id to complete")
                }
                continue
            }
            completedJobIds.remove(id)

            return@withContext result
        }
    }

    /**
     * Destroy the [TtsManager].
     */
    fun destroy() {
        tts.shutdown()
    }

    interface EngineEventListener {

        /**
         * Called when [TtsManager] has been initialised.
         * @param success true if [TtsManager] was successfully initialised, false otherwise.
         */
        fun onInitialised(success: Boolean)
    }

    companion object {
        private const val MAX_TASK_TIMEOUT_SECONDS: Long = 30
    }
}
