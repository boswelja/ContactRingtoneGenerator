package com.boswelja.contactringtonegenerator.ringtonegen

import android.content.Context
import android.os.Build
import android.os.Environment
import android.speech.tts.Voice
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.contacts.ContactRingtone
import com.boswelja.contactringtonegenerator.tts.TtsManager
import com.boswelja.contactringtonegenerator.tts.TtsUtterance
import java.io.File
import java.util.Locale
import kotlin.collections.ArrayList

class RingtoneGenerator(context: Context) :
    TtsManager.TtsManagerInterface {

    private val ringtoneDirectory: File = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
        context.getExternalFilesDir(Environment.DIRECTORY_RINGTONES)!!
    } else {
        File(Environment.getExternalStorageDirectory(), "Ringtones")
    }

    private val ttsManager = TtsManager(context)
    private val contactRingtones = ArrayList<ContactRingtone>()
    private val progressListeners = ArrayList<ProgressListener>()
    private val eventListeners = ArrayList<EventListener>()

    private var message: String = ""

    val isReady: Boolean get() = ttsManager.ttsEngineReady && message.isNotEmpty() && contactRingtones.isNotEmpty()
    var useNicknames: Boolean = true

    init {
        ttsManager.addTtsManagerInterface(this)
    }

    override fun onTtsReady() {
        eventListeners.forEach { it.onReady() }
    }

    override fun onStartSynthesizing(jobCount: Int) {
        for (listener in progressListeners) {
            listener.onStart(jobCount)
        }
    }

    override fun onSynthesisComplete() {
        for (listener in progressListeners) {
            listener.onFinish()
        }
    }

    override fun onJobStart(ttsUtterance: TtsUtterance) {
        for (listener in progressListeners) {
            listener.onRingtoneProcessed(true)
        }
    }

    override fun onJobFinished(ttsUtterance: TtsUtterance) {}

    override fun onJobError(ttsUtterance: TtsUtterance) {
        for (listener in progressListeners) {
            listener.onRingtoneProcessed(false)
        }
    }

    private fun createContactMessage(name: String): String? {
        return if (message.isNotEmpty()) {
            message.replace("%NAME", name)
        } else {
            null
        }
    }

    private fun getRingtoneFile(utteranceId: String): File {
        return File(ringtoneDirectory, "$utteranceId.ogg").also {
            it.createNewFile()
        }
    }

    private fun getRingtoneId(name: String): String {
        return name.replace(" ", "_")
    }

    private fun createContactRingtone(contact: Contact) {
        val id = getRingtoneId(contact.contactName)
        contactRingtones.add(
            ContactRingtone(
                contact,
                id,
                getRingtoneFile(id)
            )
        )
    }

    fun getAvailableVoices(locale: Locale): List<Voice>? = ttsManager.getAvailableVoices(locale)
    fun setVoice(voice: Voice) = ttsManager.setVoice(voice)
    fun setSpeechRate(speechRate: Float) = ttsManager.setSpeechRate(speechRate)
    fun preview() = ttsManager.preview("This is the voice your ring tones will use")

    fun init() {
        ttsManager.initTts()
    }

    fun addEventListener(eventListener: EventListener) {
        if (!eventListeners.contains(eventListener)) {
            eventListeners.add(eventListener)
        }
    }

    fun removeEventListener(eventListener: EventListener) {
        if (eventListeners.contains(eventListener)) {
            eventListeners.remove(eventListener)
        }
    }

    fun addProgressListener(progressListener: ProgressListener) {
        if (!progressListeners.contains(progressListener)) {
            progressListeners.add(progressListener)
        }
    }

    fun removeProgressListener(progressListener: ProgressListener) {
        if (progressListeners.contains(progressListener)) {
            progressListeners.remove(progressListener)
        }
    }

    fun setRingtoneMessage(newMessage: String): Boolean {
        if (newMessage.contains("%NAME")) {
            message = newMessage
            return true
        }
        return false
    }

    fun setContacts(newContacts: List<Contact>) {
        contactRingtones.retainAll { newContacts.contains(it.contact) }
        val newContactRingtones = ArrayList<ContactRingtone>()
        for (contact in newContacts) {
            if (!contactRingtones.any { it.contact == contact }) {
                createContactRingtone(contact)
            }
        }
        contactRingtones.addAll(newContactRingtones)
    }

    fun generate(): Boolean {
        if (isReady) {
            for (contactRingtone in contactRingtones) {
                val name = if (useNicknames) {
                    contactRingtone.contact.contactNickname ?: contactRingtone.contact.contactName
                } else {
                    contactRingtone.contact.contactName
                }
                ttsManager.addToQueue(contactRingtone, createContactMessage(name)!!)
            }
            return ttsManager.startSynthesizing()
        }
        return false
    }

    fun destroy() {
        ttsManager.removeTtsManagerInterface(this)
        ttsManager.destroy()
    }

    interface EventListener {
        fun onReady()
    }

    interface ProgressListener {
        fun onStart(jobCount: Int)
        fun onRingtoneProcessed(success: Boolean)
        fun onFinish()
    }
}