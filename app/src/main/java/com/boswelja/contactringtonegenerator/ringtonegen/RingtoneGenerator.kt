package com.boswelja.contactringtonegenerator.ringtonegen

import android.content.Context
import com.boswelja.contactringtonegenerator.StringJoinerCompat
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.contacts.ContactsHelper
import com.boswelja.contactringtonegenerator.mediastore.MediaStoreHelper
import com.boswelja.contactringtonegenerator.tts.TtsManager
import com.boswelja.contactringtonegenerator.tts.SynthesisJob
import com.boswelja.contactringtonegenerator.tts.SynthesisResult
import com.boswelja.contactringtonegenerator.ringtonegen.item.BaseItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class RingtoneGenerator(
        private val context: Context,
        private val ringtoneStructure: List<BaseItem>,
        private val contacts: List<Contact>
) :
    TtsManager.TtsJobProgressListener,
    TtsManager.TtsEngineEventListener {

    private val coroutineScope = MainScope()
    private val cacheDir: File = context.cacheDir
    private val ttsManager = TtsManager(context)

    private var remainingJobs: HashMap<String, Contact> = HashMap()
    private var jobsQueued: Boolean = false

    val totalJobCount: Int get() = remainingJobs.count()

    var progressListener: ProgressListener? = null
    var stateListener: StateListener? = null
    var state: State = State.NOT_READY
        private set(value) {
            field = value
            stateListener?.onStateChanged(value)
        }

    init {
        ttsManager.apply {
            jobProgressListener = this@RingtoneGenerator
            engineEventListener = this@RingtoneGenerator
        }
        coroutineScope.launch(Dispatchers.Default) {
            contacts.forEach {
                queueJobFor(it)
            }
            jobsQueued = true
            if (ttsManager.isEngineReady) state = State.READY
        }
    }

    override fun onInitialised(success: Boolean) {
        if (!success) throw IllegalStateException("TTS failed to initialise")
        if (jobsQueued) state = State.READY
    }

    override fun onJobStarted(synthesisJob: SynthesisJob) {
        val contact = remainingJobs[synthesisJob.id]!!
        progressListener?.onJobStarted(contact)
    }

    override fun onJobCompleted(success: Boolean, synthesisResult: SynthesisResult) {
        val contact = remainingJobs[synthesisResult.id]
        if (success) handleGenerateCompleted(contact!!, synthesisResult)
        else {
            remainingJobs.remove(synthesisResult.id)
            progressListener?.onJobCompleted(success, synthesisResult)
            if (remainingJobs.count() < 1) state = State.FINISHED
        }
    }

    private suspend fun queueJobFor(contact: Contact) {
        withContext(Dispatchers.Default) {
            val messageBuilder = StringJoinerCompat(" ")
            ringtoneStructure.forEach {
                messageBuilder.add(it.getEngineText())
            }
            val contactName = contact.nickname ?: contact.name
            val message = messageBuilder.toString()
                    .replace(Constants.CONTACT_NAME_PLACEHOLDER, contactName)
            val synthesisId = contactName.replace(" ", "_") + "-ringtone"
            SynthesisJob(synthesisId, message).also {
                ttsManager.enqueueJob(it)
                remainingJobs[it.id] = contact
            }
        }
    }

    private fun handleGenerateCompleted(contact: Contact, synthesisResult: SynthesisResult) {
        coroutineScope.launch(Dispatchers.IO) {
            val uri = MediaStoreHelper.scanNewFile(context, synthesisResult.result)
            val success = if (uri != null) {
                ContactsHelper.setContactRingtone(context, contact, uri)
                true
            } else false
            remainingJobs.remove(synthesisResult.id)
            withContext(Dispatchers.Main) {
                progressListener?.onJobCompleted(success, synthesisResult)
                if (remainingJobs.count() < 1) {
                    state = State.FINISHED
                }
            }
        }
    }


    fun start() {
        if (state == State.READY) {
            state = State.GENERATING
            ttsManager.startSynthesis()
        } else {
            throw IllegalStateException("Generator not ready")
        }
    }

    fun destroy() {
        ttsManager.destroy()
        cacheDir.deleteRecursively()
    }

    interface StateListener {
        fun onStateChanged(state: State)
    }

    interface ProgressListener {
        fun onJobStarted(contact: Contact)
        fun onJobCompleted(success: Boolean, synthesisResult: SynthesisResult)
    }

    enum class State {
        NOT_READY,
        READY,
        GENERATING,
        FINISHED
    }
}