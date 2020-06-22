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
        private val ttsManager: TtsManager,
        private val ringtoneStructure: List<BaseItem>,
        private val contacts: List<Contact>
) :
    TtsManager.TtsJobProgressListener,
    TtsManager.TtsEngineEventListener {

    private val coroutineScope = MainScope()
    private val cacheDir: File = context.cacheDir

    private var remainingJobs: HashMap<String, Contact> = HashMap()

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
        val contact = remainingJobs[synthesisResult.synthesisId]
        if (success) handleGenerateCompleted(contact!!, synthesisResult)
        else {
            remainingJobs.remove(synthesisResult.synthesisId)
            progressListener?.onJobCompleted(success, synthesisResult)
            if (remainingJobs.count() < 1) progressListener?.onGenerateFinished()
        }
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
            val synthesisId = contactName.replace(" ", "_") + "-ringtone"
            SynthesisJob.create(message, synthesisId).also {
                ttsManager.enqueueJob(it)
                remainingJobs[it.synthesisId] = contact
            }
        }
    }

    private fun handleGenerateCompleted(contact: Contact, synthesisResult: SynthesisResult) {
        coroutineScope.launch(Dispatchers.IO) {
            val uri = MediaStoreHelper.scanNewFile(context, synthesisResult.file)
            val success = if (uri != null) {
                ContactsHelper.setContactRingtone(context, contact, uri)
                true
            } else false
            remainingJobs.remove(synthesisResult.synthesisId)
            withContext(Dispatchers.Main) {
                progressListener?.onJobCompleted(success, synthesisResult)
                if (remainingJobs.count() < 1) progressListener?.onGenerateFinished()
            }
        }
    }

    fun start() {
        progressListener?.onGenerateStarted(remainingJobs.count())
        coroutineScope.launch(Dispatchers.Default) {
            contacts.forEach {
                queueJobFor(it)
            }
            ttsManager.startSynthesis()
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