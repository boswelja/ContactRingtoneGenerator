package com.boswelja.contactringtonegenerator.ringtonegen

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.arthenica.ffmpegkit.FFmpegKit
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.contacts.ContactsHelper
import com.boswelja.contactringtonegenerator.mediastore.MediaStoreHelper
import com.boswelja.contactringtonegenerator.ringtonegen.item.Constants
import com.boswelja.contactringtonegenerator.ringtonegen.item.StructureItem
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
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicInteger

class RingtoneGenerator(private val context: Context) :
    TtsManager.EngineEventListener {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val multithreaded = sharedPreferences.getBoolean("multithreaded_generation", true)
    private val volumeMultiplier: Float =
        (sharedPreferences.getInt("volume_boost", 0) + 10) / 10.0f

    private val generatorJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + generatorJob)
    private val semaphore = Semaphore(if (multithreaded) Runtime.getRuntime().availableProcessors() else 1)

    private val cacheDir: File = context.cacheDir
    private val audioItemPaths = HashMap<Uri, String>()
    private val ttsManager = TtsManager(context)
    private val counter = AtomicInteger()

    private var initialSetupComplete: Boolean = false

    val totalJobCount: Int get() = contacts.count()
    var jobsCompleted: Int = 0

    var progressListener: ProgressListener? = null
    var contacts: List<Contact> = emptyList()
        set(value) {
            if (_state.value == State.NOT_READY) {
                field = value
                checkIsReady()
            }
        }
    var ringtoneStructure: List<StructureItem<*>> = emptyList()
        set(value) {
            if (_state.value == State.NOT_READY) {
                field = value
                checkIsReady()
            }
        }

    private val _state = MutableLiveData(State.NOT_READY)
    val state: LiveData<State>
        get() = _state

    init {
        ttsManager.apply {
            engineEventListener = this@RingtoneGenerator
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

    fun initialise() {
        if (!initialSetupComplete) {
            coroutineScope.launch {
                saveAudioItems()
                initialSetupComplete = true
                checkIsReady()
            }
        }
    }

    private suspend fun saveAudioItems() {
        Timber.d("saveAudioItem() called")
        withContext(Dispatchers.IO) {
            ringtoneStructure.filter {
                it.dataType == StructureItem.DataType.AUDIO_FILE ||
                    it.dataType == StructureItem.DataType.SYSTEM_RINGTONE
            }.forEach { item ->
                Timber.i("Found AudioItem with uri ${item.data}")
                (item.data as Uri?)?.let { uri ->
                    context.contentResolver.openInputStream(uri).use { inStream ->
                        val outFile = File(cacheDir, item.engineRepresentation)
                        Timber.i("Saving $uri to ${outFile.absolutePath}")
                        FileOutputStream(outFile).use { outStream ->
                            val buffer = ByteArray(4 * 1024)
                            var read: Int
                            while (inStream!!.read(buffer).also { read = it } != -1) {
                                outStream.write(buffer, 0, read)
                            }
                        }
                        audioItemPaths[uri] = outFile.absolutePath
                    }
                } ?: Timber.w("Audio item Uri null")
            }
        }
    }

    private suspend fun synthesizeString(workingString: String, contact: Contact): SynthesisResult {
        return withContext(Dispatchers.IO) {
            val id = counter.incrementAndGet().toString()
            val structuredName = ContactsHelper.getContactStructuredName(
                context.contentResolver,
                contact.lookupKey
            )
            val nickname = ContactsHelper.getContactNickname(
                context.contentResolver,
                contact.lookupKey
            )
            val message = workingString
                .replace(Constants.FIRST_NAME_PLACEHOLDER, structuredName?.firstName ?: "")
                .replace(Constants.MIDDLE_NAME_PLACEHOLDER, structuredName?.middleName ?: "")
                .replace(Constants.LAST_NAME_PLACEHOLDER, structuredName?.lastName ?: "")
                .replace(Constants.NAME_PREFIX_PLACEHOLDER, structuredName?.prefix ?: "")
                .replace(Constants.NAME_SUFFIX_PLACEHOLDER, structuredName?.suffix ?: "")
                .replace(Constants.NICKNAME_PLACEHOLDER, nickname ?: "")
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
                    if (it.data !is String && workingString.isNotEmpty()) {
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
                    when (it.dataType) {
                        StructureItem.DataType.AUDIO_FILE,
                        StructureItem.DataType.SYSTEM_RINGTONE -> {
                            Timber.i("Got AudioItem")
                            val uri = it.data as Uri
                            val path = audioItemPaths[uri]
                            val filter = "[a$trueFileCount]"
                            filterInputs += "[$trueFileCount:0]volume=$volumeMultiplier$filter;"
                            filters += filter
                            trueFileCount += 1
                            commandInputs += " -i $path"
                        }
                        StructureItem.DataType.IMMUTABLE,
                        StructureItem.DataType.CUSTOM_TEXT -> {
                            Timber.i("Got TextItem")
                            workingString += it.engineRepresentation
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
                val result = FFmpegKit.execute(command)
                val generateSuccess = result.returnCode.isSuccess
                val success = if (generateSuccess) handleGenerateCompleted(contact, output) else false
                withContext(Dispatchers.Main) {
                    progressListener?.onJobCompleted(success, contact)
                }
                cacheFiles.forEach { it.delete() }
                handleJobCompleted()
            }
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
}
