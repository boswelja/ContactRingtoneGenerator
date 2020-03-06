package com.boswelja.contactringtonegenerator.tts

import android.content.Context
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.QUEUE_FLUSH
import android.speech.tts.TextToSpeech.SUCCESS
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.contacts.ContactRingtone
import com.boswelja.contactringtonegenerator.mediastore.MediaStoreManager
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class TtsManager(private val context: Context) :
    TextToSpeech.OnInitListener,
    UtteranceProgressListener() {

    private val outDirectory = File(Environment.getExternalStorageDirectory(), "/Ringtones/")

    private var tts: TextToSpeech? = null

    private var message: String = ""
    private var ttsEngineReady: Boolean = false
    private var messageSet: Boolean = false

    var useNicknames: Boolean = true

    private val contactRingtones = ArrayList<ContactRingtone>()

    private val utteranceJobs = ArrayList<TtsUtterance>()
    private val utteranceListeners = ArrayList<UtteranceJobListener>()
    private val ttsReadyListeners = ArrayList<TtsReadyListener>()

    var isReady: Boolean = false

    override fun onInit(status: Int) {
        ttsEngineReady = status == SUCCESS
        if (ttsEngineReady) {
            tts!!.setOnUtteranceProgressListener(this)
        }
        setIsReady()
    }

    override fun onStart(utteranceId: String?) {
        for (listener in utteranceListeners) {
            listener.onJobStart()
        }
    }

    override fun onDone(utteranceId: String?) {
        val ttsUtterance = utteranceJobs.firstOrNull { it.utteranceId == utteranceId }

        if (ttsUtterance != null) {
            utteranceJobs.remove(ttsUtterance)
            if (utteranceJobs.isEmpty()) {
                MediaStoreManager.scanNewFiles(
                    context,
                    contactRingtones
                )
                for (listener in utteranceListeners) {
                    listener.onComplete()
                }
            }
        }
    }

    override fun onError(utteranceId: String?) {
        val ttsUtterance = utteranceJobs.first { it.utteranceId == utteranceId }
        utteranceJobs.remove(ttsUtterance)
        if (ttsUtterance.file.exists()) {
            ttsUtterance.file.delete()
        }
        for (listener in utteranceListeners) {
            listener.onJobError()
        }
    }

    init {
        initDirectory()
    }

    private fun generatePersonalisedMessage(name: String): String? {
        return if (messageSet) {
            message.replace("%NAME", name)
        } else {
            null
        }
    }

    private fun generateUtteranceId(name: String): String {
        return name.replace(" ", "_")
    }

    private fun getOutFile(utteranceId: String): File {
        return File(outDirectory, "$utteranceId.ogg").also {
            Log.d("TtsManager", "Saving to ${it.absolutePath}")
            it.createNewFile()
        }
    }

    private fun setIsReady() {
        isReady = ttsEngineReady and messageSet
        Log.d("TtsManager", "isReady = $isReady")
        if (isReady) {
            for (listener in ttsReadyListeners) {
                listener.ttsReady()
            }
        }
    }

    private fun initDirectory() {
        if (!outDirectory.exists()) {
            outDirectory.mkdirs()
        }
    }

    fun initTts() {
        tts = TextToSpeech(context, this)
    }

    fun setUtteranceProgressListener(utteranceProgressListener: UtteranceProgressListener): Boolean {
        if (isReady) {
            return tts!!.setOnUtteranceProgressListener(utteranceProgressListener) == SUCCESS
        }
        return false
    }

    fun getAvailableVoices(locale: Locale): List<Voice>? {
        if (isReady) {
            return tts!!.voices.filter { it.locale == locale &&
                    (it.name.contains("language") ||
                            it.name.contains("female") ||
                            it.name.contains("male")) }.sortedBy { it.name }
        }
        return null
    }

    fun setVoice(voice: Voice): Boolean {
        if (isReady) {
            return tts!!.setVoice(voice) == SUCCESS
        }
        return false
    }

    fun setMessage(message: String): Boolean {
        if (message.contains("%NAME")) {
            this.message = message
            messageSet = true
            setIsReady()
            return true
        }
        return false
    }

    fun setSpeechRate(speechRate: Float): Boolean {
        if (isReady) {
            return tts!!.setSpeechRate(speechRate) == SUCCESS
        }
        return false
    }

    fun preview() {
        if (isReady) {
            tts!!.speak("This is the voice your ring tones will use", QUEUE_FLUSH, null,
                PREVIEW_UTTERANCE_ID
            )
        }
    }

    fun setContacts(contacts: List<Contact>) {
        utteranceJobs.clear()
        contactRingtones.clear()
        for (contact in contacts) {
            val utteranceId = generateUtteranceId(contact.contactName)
            val contactName = if (useNicknames) {
                contact.contactNickname ?: contact.contactName
            } else {
                contact.contactName
            }
            val ttsUtterance =
                TtsUtterance(
                    utteranceId,
                    generatePersonalisedMessage(contactName)!!,
                    contact,
                    getOutFile(utteranceId)
                )
            utteranceJobs.add(ttsUtterance)
            contactRingtones.add(
                ContactRingtone(
                    contact,
                    ttsUtterance.file.absolutePath
                )
            )
        }
    }

    fun startSynthesizing(): Boolean {
        if (isReady) {
            for (ttsUtterance in utteranceJobs) {
                tts!!.synthesizeToFile(ttsUtterance.personalisedMessage, null, ttsUtterance.file, ttsUtterance.utteranceId)
            }
            return true
        }
        return false
    }

    fun destroy() {
        utteranceListeners.clear()
        ttsReadyListeners.clear()
        tts?.shutdown()
    }

    fun registerUtteranceListener(utteranceProgressListener: UtteranceJobListener) {
        utteranceListeners.add(utteranceProgressListener)
    }

    fun unregisterUtteranceListener(utteranceProgressListener: UtteranceJobListener) {
        utteranceListeners.remove(utteranceProgressListener)
    }

    fun registerTtsReadyListener(listener: TtsReadyListener) {
        ttsReadyListeners.add(listener)
    }

    fun unregisterTtsReadyListener(listener: TtsReadyListener) {
        ttsReadyListeners.remove(listener)
    }

    interface UtteranceJobListener {
        fun onJobStart()
        fun onComplete()
        fun onJobError()
    }

    interface TtsReadyListener {
        fun ttsReady()
    }

    companion object {
        private const val PREVIEW_UTTERANCE_ID = "speaking_utterance_id"
    }
}