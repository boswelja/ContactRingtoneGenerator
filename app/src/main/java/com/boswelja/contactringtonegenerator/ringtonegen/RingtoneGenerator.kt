package com.boswelja.contactringtonegenerator.ringtonegen

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.contacts.ContactsHelper
import com.boswelja.contactringtonegenerator.mediastore.MediaStoreHelper
import com.boswelja.contactringtonegenerator.ringtonegen.item.AudioItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.Constants
import com.boswelja.contactringtonegenerator.ringtonegen.item.TextItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.common.StructureItem
import com.boswelja.contactringtonegenerator.tts.SynthesisJob
import com.boswelja.contactringtonegenerator.tts.SynthesisResult
import com.boswelja.contactringtonegenerator.tts.TtsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class RingtoneGenerator private constructor(private val context: Context) :
    TtsManager.EngineEventListener {

    private var ringtoneStructure: List<StructureItem> = emptyList()
    private var contacts: List<Contact> = emptyList()

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val multithreaded = sharedPreferences.getBoolean("multithreaded_generation", false)
    private val volumeMultiplier: Float =
        (sharedPreferences.getInt("volume_boost", 0) + 10) / 10.0f

    private val generatorJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + generatorJob)
    private val semaphore = Semaphore(if (multithreaded) Runtime.getRuntime().availableProcessors() else 1)

    private val cacheDir: File = context.cacheDir
    private val ttsManager = TtsManager(context)
    private val counter = AtomicInteger()

    private var initialSetupComplete: Boolean = false

    val totalJobCount: Int get() = contacts.count()
    var jobsCompleted: Int = 0

    var progressListener: ProgressListener? = null

    private val _state = MutableLiveData(State.NOT_READY)
    val state: LiveData<State>
        get() = _state

    init {
        ttsManager.apply {
            engineEventListener = this@RingtoneGenerator
        }
        coroutineScope.launch {
            initialSetupComplete = true
            checkIsReady()
        }
    }

    override fun onInitialised(success: Boolean) {
        if (!success) throw IllegalStateException("TTS failed to initialise")
        checkIsReady()
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

    private fun checkIsReady() {
        if (initialSetupComplete &&
            contacts.isNotEmpty() &&
            ringtoneStructure.isNotEmpty() &&
            ttsManager.isEngineReady &&
            _state.value == State.NOT_READY
        ) {
            _state.postValue(State.READY)
        }
    }

    private fun handleJobCompleted() {
        jobsCompleted += 1
        if (jobsCompleted >= totalJobCount) _state.postValue(State.FINISHED)
    }

    private fun createJobFor(contact: Contact): Job {
        Timber.d("createJobFor($contact)")
        return coroutineScope.launch {
            semaphore.withPermit {
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
                            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(it.audioUri!!, "r")
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
    }

    fun setContacts(newContacts: List<Contact>) {
        if (_state.value == State.NOT_READY) {
            contacts = newContacts
            checkIsReady()
        }
    }

    fun setRingtoneStructure(newStructure: List<StructureItem>) {
        if (_state.value == State.NOT_READY) {
            ringtoneStructure = newStructure
            checkIsReady()
        }
    }

    fun start() {
        if (_state.value == State.READY) {
            _state.postValue(State.GENERATING)
            coroutineScope.launch {
                contacts.forEach {
                    createJobFor(it)
                }
            }
        } else {
            throw IllegalStateException("Generator not ready")
        }
    }

    fun destroy() {
        ttsManager.destroy()
        cacheDir.deleteRecursively()
        generatorJob.cancel()
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

    companion object {
        private var INSTANCE: RingtoneGenerator? = null

        fun get(context: Context): RingtoneGenerator {
            synchronized(this) {
                if (INSTANCE == null)
                    INSTANCE = RingtoneGenerator(context)
                return INSTANCE!!
            }
        }
    }
}
