package com.boswelja.contactringtonegenerator.ringtonegen

import android.content.Context
import androidx.preference.PreferenceManager
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.contacts.ContactsHelper
import com.boswelja.contactringtonegenerator.mediastore.MediaStoreHelper
import com.boswelja.contactringtonegenerator.ringtonegen.item.Constants
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.AudioItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.TextItem
import com.boswelja.contactringtonegenerator.tts.SynthesisJob
import com.boswelja.contactringtonegenerator.tts.SynthesisResult
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
    private val volumeMultiplier = calculateVolumeMultiplier()

    private var initialSetupComplete: Boolean = false

    val totalJobCount: Int get() = contacts.count()
    var jobsCompleted: Int = 0

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
                ContactsHelper.setContactRingtone(context, contact, uri)
                true
            } else false
        }
    }

    private suspend fun synthesizeString(workingString: String, contact: Contact): SynthesisResult {
        return withContext(Dispatchers.IO) {
            val id = counter.incrementAndGet().toString()
            val message = workingString
                .replace(Constants.FIRST_NAME_PLACEHOLDER, contact.firstName)
                .replace(Constants.MIDDLE_NAME_PLACEHOLDER, contact.middleName ?: "")
                .replace(Constants.LAST_NAME_PLACEHOLDER, contact.lastName ?: "")
                .replace(Constants.NAME_PREFIX_PLACEHOLDER, contact.prefix ?: "")
                .replace(Constants.NAME_SUFFIX_PLACEHOLDER, contact.suffix ?: "")
                .replace(Constants.NICKNAME_PLACEHOLDER, contact.nickname ?: "")
            return@withContext ttsManager.synthesizeToFile(SynthesisJob(id, message))
        }
    }

    private fun handleJobCompleted() {
        jobsCompleted += 1
        if (jobsCompleted >= totalJobCount) state = State.FINISHED
    }

    private fun calculateVolumeMultiplier(): Float {
        val userBoost = PreferenceManager.getDefaultSharedPreferences(context).getInt("volume_boost", 0)
        val baseVolume = 10
        return (baseVolume + userBoost) / 10f
    }

    private fun createJobFor(contact: Contact): Job {
        Timber.d("createJobFor($contact)")
        return coroutineScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                progressListener?.onJobStarted(contact)
            }
            var workingString = ""
            var commandInputs = ""
            var filterInputs = ""
            var filters = ""
            val cacheFiles = ArrayList<File>()
            var trueFileCount = 0
            ringtoneStructure.forEach {
                if (it !is TextItem && workingString.isNotEmpty()) {
                    Timber.i("End of TTS block, synthesizing")
                    val result = synthesizeString(workingString, contact)
                    cacheFiles.add(result.result)
                    val filter = "[a$trueFileCount]"
                    filterInputs += "[$trueFileCount:0]volume=$volumeMultiplier$filter;"
                    filters += filter
                    trueFileCount += 1
                    commandInputs += " -i ${result.result.absolutePath}"
                    workingString = ""
                }
                when (it) {
                    is AudioItem -> {
                        Timber.i("Got AudioItem")
                        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(it.getAudioContentUri()!!, "r")
                        val path = String.format("pipe:%d", parcelFileDescriptor?.fd)
                        val filter = "[a$trueFileCount]"
                        filterInputs += "[$trueFileCount:0]volume=$volumeMultiplier$filter;"
                        filters += filter
                        trueFileCount += 1
                        commandInputs += " -i $path"
                    }
                    is TextItem -> {
                        Timber.i("Got TextItem")
                        workingString += it.getEngineText()
                    }
                }
            }
            if (workingString.isNotEmpty()) {
                Timber.i("TTS working string not empty, synthesizing")
                val result = synthesizeString(workingString, contact)
                cacheFiles.add(result.result)
                val filter = "[a$trueFileCount]"
                filterInputs += "[$trueFileCount:0]volume=$volumeMultiplier$filter;"
                filters += filter
                trueFileCount += 1
                commandInputs += " -i ${result.result.absolutePath}"
                workingString = ""
            }

            Timber.d("Got $trueFileCount files")
            val output = File(cacheDir, "${contact.displayName.replace(" ", "-")}.ogg")
            cacheFiles.add(output)
            val command = "$commandInputs -filter_complex '${filterInputs}${filters}concat=n=$trueFileCount:v=0:a=1[out]' -map '[out]' ${output.absolutePath}"
            Timber.i("ffmpeg $command")
            val result = FFmpeg.execute(command)
            val generateSuccess = result == Config.RETURN_CODE_SUCCESS
            val success = if (generateSuccess) handleGenerateCompleted(contact, output) else false
            withContext(Dispatchers.Main) {
                progressListener?.onJobCompleted(success, contact)
            }
            cacheFiles.forEach { it.delete() }
            handleJobCompleted()
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
