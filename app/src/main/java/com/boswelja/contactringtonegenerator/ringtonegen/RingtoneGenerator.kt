package com.boswelja.contactringtonegenerator.ringtonegen

import com.boswelja.contactringtonegenerator.StringJoinerCompat
import com.boswelja.contactringtonegenerator.contacts.Contact
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
        private val cacheDir: File,
        private val ttsManager: TtsManager,
        private val ringtoneStructure: List<BaseItem>,
        private val contacts: List<Contact>
) :
    TtsManager.TtsJobProgressListener,
    TtsManager.TtsEngineEventListener {

    private val coroutineScope = MainScope()

    private var remainingJobs: Int = 0

    var progressListener: ProgressListener? = null

    init {
        ttsManager.apply {
            jobProgressListener = this@RingtoneGenerator
            engineEventListener = this@RingtoneGenerator
        }
    }

    override fun onInitialised(success: Boolean) {

    }

    override fun onJobStarted(synthesisJob: SynthesisJob) {
        progressListener?.onJobStarted()
    }

    override fun onJobCompleted(success: Boolean, synthesisResult: SynthesisResult) {
        progressListener?.onJobCompleted(success, synthesisResult)
        remainingJobs -= 1
        if (remainingJobs < 1) progressListener?.onGenerateFinished()
    }

    private fun calculateJobCount(): Int {
        return contacts.count()
    }

    private suspend fun queueJobFor(contact: Contact) {
        withContext(Dispatchers.Default) {
            val messageBuilder = StringJoinerCompat(" ")
            ringtoneStructure.forEach {
                messageBuilder.add(it.getEngineText())
            }
            val contactName = contact.contactNickname ?: contact.contactName
            val message = messageBuilder.toString()
                    .replace(Constants.CONTACT_NAME_PLACEHOLDER, contactName)
            SynthesisJob.create(message, contact.id.toString()).also {
                ttsManager.enqueueJob(it)
            }
        }
    }

    fun start() {
        remainingJobs = calculateJobCount()
        progressListener?.onGenerateStarted(remainingJobs)
        coroutineScope.launch(Dispatchers.Default) {
            contacts.forEach {
                queueJobFor(it)
            }
        }
    }

    fun destroy() {
        ttsManager.destroy()
        cacheDir.deleteRecursively()
    }

    interface ProgressListener {
        fun onGenerateStarted(totalJobCount: Int)
        fun onJobStarted()
        fun onJobCompleted(success: Boolean, synthesisResult: SynthesisResult)
        fun onGenerateFinished()
    }
}