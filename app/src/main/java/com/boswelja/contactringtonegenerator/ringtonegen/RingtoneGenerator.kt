package com.boswelja.contactringtonegenerator.ringtonegen

import android.content.Context
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.contacts.ContactsHelper
import com.boswelja.contactringtonegenerator.mediastore.MediaStoreHelper
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.AudioItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.TextItem
import com.boswelja.contactringtonegenerator.tts.SynthesisJob
import com.boswelja.contactringtonegenerator.tts.TtsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class RingtoneGenerator(
    private val context: Context,
    private val ringtoneStructure: List<StructureItem>,
    private val contacts: List<Contact>
) : TtsManager.EngineEventListener {

    private val coroutineScope = MainScope()
    private val cacheDir: File = context.cacheDir
    private val ttsManager = TtsManager(context)
    private val counter = AtomicInteger()

    private var initialSetupComplete: Boolean = false

    val totalJobCount: Int get() = contacts.count()

    var progressListener: ProgressListener? = null
    var stateListener: StateListener? = null
    var state: State = State.NOT_READY
        private set(value) {
            field = value
            stateListener?.onStateChanged(value)
        }

    init {
        ttsManager.apply {
            engineEventListener = this@RingtoneGenerator
        }
        coroutineScope.launch(Dispatchers.Default) {
            initialSetupComplete = true
            if (ttsManager.isEngineReady) state = State.READY
        }
    }

    override fun onInitialised(success: Boolean) {
        if (!success) throw IllegalStateException("TTS failed to initialise")
        if (initialSetupComplete) state = State.READY
    }

    private suspend fun handleGenerateCompleted(contact: Contact, ringtone: File): Boolean {
        return withContext(Dispatchers.IO) {
            val uri = MediaStoreHelper.scanNewFile(context, ringtone)
            return@withContext if (uri != null) {
                ringtone.delete()
                ContactsHelper.setContactRingtone(context, contact, uri)
                true
            } else false
        }
    }

    private fun createJobFor(contact: Contact): Job {
        Timber.d("createJobFor($contact)")
        return coroutineScope.launch(Dispatchers.Default) {
            var workingString = ""
            var commandInputs = ""
            var filterInputs = ""
            var filesCount = 0
            ringtoneStructure.forEach {
                if (it !is TextItem && workingString.isNotEmpty()) {
                    val id = counter.incrementAndGet().toString()
                    val result = ttsManager.synthesizeToFile(SynthesisJob(id, workingString))
                    commandInputs += " -i ${result.result.absolutePath}"
                    filesCount += 1
                    filterInputs += "[$filesCount:0]"
                }
                when (it) {
                    is AudioItem -> {
                        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(it.getAudioContentUri()!!, "r")
                        val path = String.format("pipe:%d", parcelFileDescriptor?.fd)
                        commandInputs += " -i $path"
                        filesCount += 1
                        filterInputs += "[$filesCount:0]"
                    }
                    is TextItem -> {
                        workingString += it.getEngineText()
                    }
                }
            }
            if (workingString.isNotEmpty()) {
                val id = counter.incrementAndGet().toString()
                val result = ttsManager.synthesizeToFile(SynthesisJob(id, workingString))
                commandInputs += " -i ${result.result.absolutePath}"
                filesCount += 1
                filterInputs += "[$filesCount:0]"
            }

            Timber.d("Got $filesCount files")
            val output = File(cacheDir, "${contact.displayName.replace(" ", "-")}.ogg")
            val command = "$commandInputs -filter_complex '${filterInputs}concat=n=$filesCount:v=0:a=1[out]' -map '[out]' ${output.absolutePath}"
            Timber.i("ffmpeg $command")
            val result = FFmpeg.execute(command)
            val generateSuccess = result == Config.RETURN_CODE_SUCCESS
            val success = if (generateSuccess) handleGenerateCompleted(contact, output) else false
            progressListener?.onJobCompleted(success, contact)
        }
    }

    fun start() {
        if (state == State.READY) {
            state = State.GENERATING
            contacts.forEach {
                createJobFor(it)
            }
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
        fun onJobCompleted(success: Boolean, contact: Contact)
    }

    enum class State {
        NOT_READY,
        READY,
        GENERATING,
        FINISHED
    }
}
