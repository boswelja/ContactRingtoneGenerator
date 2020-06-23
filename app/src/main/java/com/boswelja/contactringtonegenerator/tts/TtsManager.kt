package com.boswelja.contactringtonegenerator.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.QUEUE_FLUSH
import android.speech.tts.TextToSpeech.SUCCESS
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import timber.log.Timber
import java.io.File
import java.util.Locale
import kotlin.collections.ArrayList

class TtsManager(context: Context) :
    TextToSpeech.OnInitListener,
    UtteranceProgressListener() {

    private val cacheDirectory = context.cacheDir
    private val synthesisJobs = ArrayList<SynthesisJob>()
    private val synthesisResults = ArrayList<SynthesisResult>()

    private val tts: TextToSpeech = TextToSpeech(context, this)
    private var restoreVoice: Voice? = null

    /**
     * The number of [SynthesisJob] that are still enqueued.
     */
    val synthesisJobCount: Int
        get() = synthesisJobs.count()

    var jobProgressListener: TtsJobProgressListener? = null
    var engineEventListener: TtsEngineEventListener? = null

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
        val utteranceJob = getSynthesisJob(utteranceId)
        if (utteranceJob != null) {
            jobProgressListener?.onJobStarted(utteranceJob)
        }
    }

    override fun onDone(utteranceId: String?) {
        val synthesisJob = getSynthesisJob(utteranceId)
        val synthesisResult = synthesisResults.firstOrNull { it.id == utteranceId }
        if (synthesisJob != null) synthesisJobs.remove(synthesisJob)
        if (synthesisResult != null) {
            synthesisResults.remove(synthesisResult)
            if (utteranceId == PREVIEW_UTTERANCE_ID && restoreVoice != null) {
                tts.voice = restoreVoice
                restoreVoice = null
            } else {
                jobProgressListener?.onJobCompleted(true, synthesisResult)
            }
        }
    }

    override fun onError(utteranceId: String?) {
        val synthesisJob = getSynthesisJob(utteranceId)
        val synthesisResult = synthesisResults.firstOrNull { it.id == utteranceId }
        if (synthesisJob != null) synthesisJobs.remove(synthesisJob)
        if (synthesisResult != null) {
            synthesisResults.remove(synthesisResult)
            if (utteranceId != PREVIEW_UTTERANCE_ID)
                jobProgressListener?.onJobCompleted(false, synthesisResult)
        }
    }

    /**
     * Gets an existing [SynthesisJob] with a given ID, or null if none exist.
     * @param utteranceId The ID of the [SynthesisJob] to get.
     * @return The existing [SynthesisJob], or null if none were found.
     */
    private fun getSynthesisJob(utteranceId: String?): SynthesisJob? {
        return synthesisJobs.firstOrNull { it.id == utteranceId }
    }

    fun startSynthesis() {
        if (isEngineReady) {
            for (synthesisJob in synthesisJobs) {
                val synthesisId = synthesisJob.id
                val file = File(cacheDirectory, "$synthesisId.ogg")
                if (!file.exists()) file.createNewFile()
                tts.synthesizeToFile(synthesisJob.text, null, file, synthesisId)
                synthesisResults.add(SynthesisResult(synthesisId, file))
            }
        }
    }

    /**
     * Get a [List] of available [Voice] classes.
     * @param locale The optional [Locale] to get available voices for.
     * @return A [List] of [Voice] classes, or null if the engine isn't ready.
     */
    fun getAvailableVoices(locale: Locale = Locale.getDefault()): List<Voice>? {
        if (isEngineReady) {
            return tts.voices?.filter { it.locale == locale }?.sortedBy { it.name }
        }
        return null
    }

    /**
     * Get the TTS engine's default [Voice].
     * @return The engine's default [Voice], or null if the engine isn't ready.
     */
    fun getDefaultVoice(): Voice? {
        if (isEngineReady) {
            return tts.defaultVoice
        }
        return null
    }

    /**
     * The [List] of [TextToSpeech.EngineInfo] that describes available TTS engines.
     */
    val engines: List<TextToSpeech.EngineInfo>
        get() = tts.engines

    /**
     * The package name of the default TTS engine.
     */
    val defaultEngine: String
        get() = tts.defaultEngine

    /**
     * Sets the [Voice] to use for speech synthesis.
     * @param voice The new [Voice] to use.
     * @return true if setting the voice was successful, false otherwise.
     */
    fun setVoice(voice: Voice): Boolean {
        if (isEngineReady) {
            return tts.setVoice(voice) == SUCCESS
        }
        return false
    }

    /**
     * Set the speech rate.
     * @param speechRate The speech rate to use, 1.0 being the default,
     * lower numbers are slower and higher numbers are faster.
     * @return true if setting the speech rate was successful, false otherwise.
     */
    fun setSpeechRate(speechRate: Float): Boolean {
        if (isEngineReady) {
            return tts.setSpeechRate(speechRate) == SUCCESS
        }
        return false
    }

    /**
     * Speak a message with the current voice.
     * @param message The message to speak.
     */
    fun preview(message: String) {
        previewVoice(tts.voice, message)
    }

    /**
     * Speak a message in a specified voice.
     * @param voice The [Voice] to speak in.
     * @param message The message to speak.
     */
    fun previewVoice(voice: Voice, message: String) {
        if (isEngineReady) {
            if (voice != tts.voice) {
                restoreVoice = tts.voice
                tts.voice = voice
            }
            tts.speak(message, QUEUE_FLUSH, null,
                    PREVIEW_UTTERANCE_ID
            )
        }
    }

    /**
     * Add a [SynthesisJob] to the queue.
     * @param synthesisJob The [SynthesisJob] to enqueue.
     * @return true if successfully queued, false otherwise.
     */
    fun enqueueJob(synthesisJob: SynthesisJob): Boolean {
        if (!synthesisJobs.contains(synthesisJob)) {
            synthesisJobs.add(synthesisJob)
            return true
        }
        return false
    }

    /**
     * Destroy the TtsManager.
     */
    fun destroy() {
        tts.shutdown()
    }

    interface TtsJobProgressListener {
        fun onJobStarted(synthesisJob: SynthesisJob)
        fun onJobCompleted(success: Boolean, synthesisResult: SynthesisResult)
    }

    interface TtsEngineEventListener {
        fun onInitialised(success: Boolean)
    }

    companion object {
        private const val PREVIEW_UTTERANCE_ID = "speaking_utterance_id"
    }
}