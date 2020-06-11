package com.boswelja.contactringtonegenerator.ringtonegen

import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.tts.TtsManager
import com.boswelja.contactringtonegenerator.tts.SynthesisJob
import com.boswelja.contactringtonegenerator.tts.SynthesisResult
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.item.BaseItem
import com.boswelja.contactringtonegenerator.ui.ringtonecreator.item.ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class RingtoneGenerator(
        private val cacheDir: File,
        private val ttsManager: TtsManager,
        private val ringtoneStructure: List<BaseItem>,
        private val contacts: List<Contact>
) :
    TtsManager.TtsJobProgressListener,
    TtsManager.TtsEngineEventListener,
    AudioJoiner.JoinProgressListener {

    private val coroutineScope = MainScope()
    private val nonDynamicComponents = HashMap<String, File?>()
    private val dynamicComponents = HashMap<String, File?>()

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
        if (success) {
            val id = synthesisResult.synthesisId
            if (nonDynamicComponents.containsKey(id)) {
                nonDynamicComponents[id] = synthesisResult.file
            } else if (dynamicComponents.containsKey(id)) {
                dynamicComponents[id] = synthesisResult.file
            }
        }
        progressListener?.onJobCompleted(success)
    }

    override fun onJoinStarted(jobId: String) {
        TODO("Not yet implemented")
    }

    override fun onJoinCompleted(isSuccessful: Boolean, jobId: String) {
        progressListener?.onJobCompleted(isSuccessful)
    }

    private suspend fun queueNonDynamicComponents() {
        withContext(Dispatchers.Default) {
            val components = ringtoneStructure.filter { !it.isDynamic }
            components.forEach { item ->
                SynthesisJob.create(item.getEngineText(), item.getEngineId()).also {
                    nonDynamicComponents[it.synthesisId] = null
                    ttsManager.enqueueJob(it)
                }
            }
        }
    }

    private suspend fun queueDynamicComponents() {
        withContext(Dispatchers.Default) {
            val components = ringtoneStructure.filter { it.isDynamic }
            components.forEach { dynamicComponent ->
                when (dynamicComponent.id) {
                    ID.CONTACT_NAME -> {
                        contacts.forEach { contact ->
                            val contactName = contact.contactNickname ?: contact.contactName
                            val id = dynamicComponent.getEngineId() + contactName.replace(" ", "_").toLowerCase(Locale.ROOT)
                            SynthesisJob.create(contactName, id).also {
                                dynamicComponents[it.synthesisId] = null
                                ttsManager.enqueueJob(it)
                            }
                        }
                    }
                    else -> throw IllegalArgumentException("Unknown dynamic component found.")
                }

            }
        }
    }

    private fun calculateJobCount(): Int {
        var jobCount = 0
        jobCount += ringtoneStructure.count { !it.isDynamic } // Non-dynamic components to generate
        jobCount += ringtoneStructure.count { it.isDynamic } * contacts.count() // Dynamic components to generate
        jobCount += contacts.count() // Ringtones to stitch together
        return jobCount
    }

    fun start() {
        progressListener?.onGenerateStarted(calculateJobCount())
        coroutineScope.launch(Dispatchers.Default) {
            queueNonDynamicComponents()
            queueDynamicComponents()
        }
    }

    fun destroy() {
        ttsManager.destroy()
        cacheDir.deleteRecursively()
    }

    interface ProgressListener {
        fun onGenerateStarted(totalJobCount: Int)
        fun onJobStarted()
        fun onJobCompleted(success: Boolean)
        fun onGenerateFinished()
    }
}