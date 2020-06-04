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
    private var restoreVoice: Voice? = null

    private val contactRingtones = ArrayList<ContactRingtone>()
    private val utteranceJobs = ArrayList<TtsUtterance>()
    private val ttsInterfaces = ArrayList<TtsManagerInterface>()

    var isEngineReady: Boolean = false

    override fun onInit(status: Int) {
        isEngineReady = status == SUCCESS
        if (isEngineReady) {
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
            if (utteranceId == PREVIEW_UTTERANCE_ID && restoreVoice != null) {
                tts!!.voice = restoreVoice
                restoreVoice = null
            }
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
        if (isEngineReady) {
            for (listener in ttsInterfaces) {
                listener.onTtsReady()
            }
        }
    }

    fun initTts() {
        tts = TextToSpeech(context, this)
    }

    fun getAvailableVoices(locale: Locale): List<Voice>? {
        if (isEngineReady) {
            return tts?.voices?.filter { it.locale == locale }?.sortedBy { it.name }
        }
        return null
    }

    fun getDefaultVoice(): Voice? {
        if (isEngineReady) {
            return tts?.defaultVoice
        }
        return null
    }

    fun setVoice(voice: Voice): Boolean {
        if (isEngineReady) {
            return tts!!.setVoice(voice) == SUCCESS
        }
        return false
    }

    fun setSpeechRate(speechRate: Float): Boolean {
        if (isEngineReady) {
            return tts!!.setSpeechRate(speechRate) == SUCCESS
        }
        return false
    }

    fun preview(message: String) {
        previewVoice(tts!!.voice, message)
    }

    fun previewVoice(voice: Voice, message: String) {
        if (isEngineReady) {
            if (voice != tts!!.voice) {
                restoreVoice = tts!!.voice
                tts!!.voice = voice
            }
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
        if (isEngineReady) {
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