package com.boswelja.contactringtonegenerator.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.QUEUE_FLUSH
import android.speech.tts.TextToSpeech.SUCCESS
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.boswelja.contactringtonegenerator.contacts.ContactRingtone
import com.boswelja.contactringtonegenerator.mediastore.MediaStoreManager
import java.util.*
import kotlin.collections.ArrayList

class TtsManager(private val context: Context) :
    TextToSpeech.OnInitListener,
    UtteranceProgressListener() {

    private var tts: TextToSpeech? = null

    private val contactRingtones = ArrayList<ContactRingtone>()
    private val utteranceJobs = ArrayList<TtsUtterance>()
    private val ttsInterfaces = ArrayList<TtsManagerInterface>()

    var ttsEngineReady: Boolean = false

    override fun onInit(status: Int) {
        ttsEngineReady = status == SUCCESS
        if (ttsEngineReady) {
            tts!!.setOnUtteranceProgressListener(this)
        }
        setIsReady()
    }

    override fun onStart(utteranceId: String?) {
        val utteranceJob = utteranceJobs.firstOrNull { it.utteranceId == utteranceId }
        if (utteranceJob != null) {
            for (listener in ttsInterfaces) {
                listener.onJobStart(utteranceJob)
            }
        }
    }

    override fun onDone(utteranceId: String?) {
        val utteranceJob = utteranceJobs.firstOrNull { it.utteranceId == utteranceId }

        if (utteranceJob != null) {
            for (listener in ttsInterfaces) {
                listener.onJobFinished(utteranceJob)
            }
            utteranceJobs.remove(utteranceJob)
            if (utteranceJobs.isEmpty()) {
                MediaStoreManager.scanNewFiles(
                    context,
                    contactRingtones
                )
                for (listener in ttsInterfaces) {
                    listener.onSynthesisComplete()
                }
            }
        }
    }

    override fun onError(utteranceId: String?) {
        val utteranceJob = utteranceJobs.firstOrNull { it.utteranceId == utteranceId }
        if (utteranceJob != null) {
            utteranceJobs.remove(utteranceJob)
            if (utteranceJob.ringtoneFile.exists()) {
                utteranceJob.ringtoneFile.delete()
            }
            for (listener in ttsInterfaces) {
                listener.onJobError(utteranceJob)
            }
        }
    }

    private fun setIsReady() {
        if (ttsEngineReady) {
            for (listener in ttsInterfaces) {
                listener.onTtsReady()
            }
        }
    }

    fun initTts() {
        tts = TextToSpeech(context, this)
    }

    fun getAvailableVoices(locale: Locale): List<Voice>? {
        if (ttsEngineReady) {
            return tts?.voices?.filter { it.locale == locale }?.sortedBy { it.name }
        }
        return null
    }

    fun setVoice(voice: Voice): Boolean {
        if (ttsEngineReady) {
            return tts!!.setVoice(voice) == SUCCESS
        }
        return false
    }

    fun setSpeechRate(speechRate: Float): Boolean {
        if (ttsEngineReady) {
            return tts!!.setSpeechRate(speechRate) == SUCCESS
        }
        return false
    }

    fun preview(message: String) {
        if (ttsEngineReady) {
            tts!!.speak(message, QUEUE_FLUSH, null,
                PREVIEW_UTTERANCE_ID
            )
        }
    }

    fun addToQueue(contactRingtone: ContactRingtone, message: String) {
        val utteranceJob = TtsUtterance(
                contactRingtone,
                message
        )
        utteranceJobs.add(utteranceJob)
        contactRingtones.add(contactRingtone)
    }

    fun startSynthesizing(): Boolean {
        if (ttsEngineReady) {
            val jobCount = utteranceJobs.count() * 3
            for (listener in ttsInterfaces) {
                listener.onStartSynthesizing(jobCount)
            }
            for (ttsUtterance in utteranceJobs) {
                tts!!.synthesizeToFile(ttsUtterance.personalisedMessage, null, ttsUtterance.ringtoneFile, ttsUtterance.utteranceId)
            }
            return true
        }
        return false
    }

    fun destroy() {
        ttsInterfaces.clear()
        tts?.shutdown()
    }

    fun addTtsManagerInterface(ttsManagerInterface: TtsManagerInterface) {
        ttsInterfaces.add(ttsManagerInterface)
    }

    fun removeTtsManagerInterface(ttsManagerInterface: TtsManagerInterface) {
        ttsInterfaces.remove(ttsManagerInterface)
    }

    interface TtsManagerInterface {
        fun onTtsReady()

        fun onStartSynthesizing(jobCount: Int)
        fun onSynthesisComplete()

        fun onJobStart(ttsUtterance: TtsUtterance)
        fun onJobFinished(ttsUtterance: TtsUtterance)
        fun onJobError(ttsUtterance: TtsUtterance)
    }

    companion object {
        private const val PREVIEW_UTTERANCE_ID = "speaking_utterance_id"
    }
}