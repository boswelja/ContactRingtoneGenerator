package com.boswelja.contactringtonegenerator.tts

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.SUCCESS
import android.speech.tts.UtteranceProgressListener
import timber.log.Timber
import java.io.File
import kotlin.collections.ArrayList

/**
 * A class to handle queueing and synthesizing jobs through a [TextToSpeech] instance.
 * @param context The [Context] to run everything with.
 */
class TtsManager(context: Context) :
    TextToSpeech.OnInitListener,
    UtteranceProgressListener() {

    private val cacheDirectory = context.cacheDir
    private val synthesisJobs = ArrayList<SynthesisJob>()
    private val synthesisResults = ArrayList<SynthesisResult>()
    private val startedJobIds by lazy { ArrayList<String>() }
    private val tts: TextToSpeech = TextToSpeech(context, this)

    /**
     * The number of [SynthesisJob] that are still enqueued.
     */
    val synthesisJobCount: Int
        get() = synthesisJobs.count()

    var jobProgressListener: JobProgressListener? = null
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
        val utteranceJob = getSynthesisJob(utteranceId)
        if (utteranceJob != null) {
            // onStart seems to get called twice per job on API versions 28 and lower, work around this by keeping track of which job IDs have been called
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                jobProgressListener?.onJobStarted(utteranceJob)
            } else {
                if (!startedJobIds.contains(utteranceId)) {
                    startedJobIds.add(utteranceId!!)
                    jobProgressListener?.onJobStarted(utteranceJob)
                }
            }
        }
    }

    override fun onDone(utteranceId: String?) {
        val synthesisJob = getSynthesisJob(utteranceId)
        val synthesisResult = synthesisResults.firstOrNull { it.id == utteranceId }
        if (synthesisJob != null) synthesisJobs.remove(synthesisJob)
        if (synthesisResult != null) {
            synthesisResults.remove(synthesisResult)
            jobProgressListener?.onJobCompleted(true, synthesisResult)
        }
    }

    override fun onError(utteranceId: String?) {
        val synthesisJob = getSynthesisJob(utteranceId)
        val synthesisResult = synthesisResults.firstOrNull { it.id == utteranceId }
        if (synthesisJob != null) synthesisJobs.remove(synthesisJob)
        if (synthesisResult != null) {
            synthesisResults.remove(synthesisResult)
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

    /**
     * Schedules all [SynthesisJob]s in the queue for synthesis.
     * Does nothing if [isEngineReady] is false.
     */
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

    interface JobProgressListener {

        /**
         * Called when a [SynthesisJob] is started.\
         * @param synthesisJob The job that has been started.
         */
        fun onJobStarted(synthesisJob: SynthesisJob)

        /**
         * Called when a [SynthesisJob] has been completed.
         * @param success true if the synthesis was successful, false otherwise.
         * @param synthesisResult The [SynthesisResult] for the job.
         */
        fun onJobCompleted(success: Boolean, synthesisResult: SynthesisResult)
    }

    interface EngineEventListener {

        /**
         * Called when [TtsManager] has been initialised.
         * @param success true if [TtsManager] was successfully initialised, false otherwise.
         */
        fun onInitialised(success: Boolean)
    }
}
