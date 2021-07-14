package com.boswelja.contactringtonegenerator.ringtonegen.generator

import android.content.Context
import android.net.Uri
import com.arthenica.ffmpegkit.FFmpegKit
import com.boswelja.contactringtonegenerator.common.mediastore.scanRingtone
import com.boswelja.contactringtonegenerator.contactpicker.ContactsHelper
import com.boswelja.contactringtonegenerator.ringtonebuilder.StructureItem
import com.boswelja.contactringtonegenerator.ringtonegen.Constants
import com.boswelja.contactringtonegenerator.ringtonegen.GeneratorResult
import com.boswelja.contactringtonegenerator.ringtonegen.tts.TTSProvider
import com.boswelja.contactringtonegenerator.settings.settingsDataStore
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class RingtoneGenerator(
    private val context: Context,
    private val ttsProvider: TTSProvider,
    private val contactLookupKeys: Set<String>,
    ringtoneStructure: List<StructureItem>
) {

    private val cacheDir = context.cacheDir
    private val blocks = ringtoneStructure.toBlocks()

    suspend fun generate(): GeneratorResult {
        // Load settings
        val speechRate = context.settingsDataStore.data.map { it.ttsSpeechRate }.first()
        val voicePitch = context.settingsDataStore.data.map { it.ttsPitch }.first()
        val volumeMultiplier = context.settingsDataStore.data.map { it.volumeMultiplier }.first()

        // Init TTS
        val initSuccess = ttsProvider.initialise(context, speechRate, voicePitch)
        if (!initSuccess) return GeneratorResult(failedContacts = contactLookupKeys)

        // Read FileBlock Uris into cache
        blocks.filterIsInstance<FileBlock>().forEach { fileBlock ->
            val inStream = context.contentResolver.openInputStream(fileBlock.uri)
                ?: return GeneratorResult(failedContacts = contactLookupKeys)
            getPartFileFor(fileBlock.uri.toString()).outputStream().use {
                inStream.copyTo(it)
            }
        }

        val failedContactKeys = mutableSetOf<String>()
        contactLookupKeys.forEach { lookupKey ->
            val ringtoneFile = generateRingtoneFor(
                lookupKey,
                volumeMultiplier
            )
            val ringtoneUri = ringtoneFile?.let { saveRingtone(ringtoneFile) }
            if (ringtoneUri != null) {
                ContactsHelper.setContactRingtone(
                    context,
                    ContactsHelper.getContactUri(context, lookupKey)!!,
                    ringtoneUri
                )
                ringtoneFile.delete()
            } else {
                failedContactKeys.add(lookupKey)
            }
        }

        // Shut down TTS
        ttsProvider.shutdown()

        // Empty cache on finish
        cacheDir.deleteRecursively()

        return GeneratorResult(
            successfulContacts = contactLookupKeys.minus(failedContactKeys),
            failedContacts = failedContactKeys
        )
    }

    private suspend fun synthesizeTextForContact(
        contactLookupKey: String,
        text: String
    ): File? {
        val contactName = ContactsHelper.getContactStructuredName(
            context.contentResolver,
            contactLookupKey
        ) ?: return null
        val contactNickname = ContactsHelper.getContactNickname(
            context.contentResolver,
            contactLookupKey
        )

        val synthesisText = text
            .replace(Constants.NAME_PREFIX_PLACEHOLDER, contactName.prefix)
            .replace(Constants.NAME_SUFFIX_PLACEHOLDER, contactName.suffix)
            .replace(Constants.FIRST_NAME_PLACEHOLDER, contactName.firstName)
            .replace(Constants.MIDDLE_NAME_PLACEHOLDER, contactName.middleName)
            .replace(Constants.LAST_NAME_PLACEHOLDER, contactName.lastName)
            .replace(Constants.NICKNAME_PLACEHOLDER, contactNickname)

        val file = getPartFileFor(synthesisText)
        val synthResult = ttsProvider.synthesizeToFile(
            synthesisText,
            getPartFileFor(synthesisText)
        )
        if (!synthResult) return null

        return file
    }

    private suspend fun generateRingtoneFor(
        contactLookupKey: String,
        volumeMultiplier: Float
    ): File? {
        // Convert blocks to files
        val parts = mutableListOf<File>()
        blocks.forEach { item ->
            val file = when (item) {
                is TextBlock -> synthesizeTextForContact(contactLookupKey, item.text)
                is FileBlock -> getPartFileFor(item.uri.toString())
            } ?: return null
            parts.add(file)
        }

        // TODO Improve file name logic here
        val contactName = ContactsHelper.getContactStructuredName(
            context.contentResolver, contactLookupKey
        )!!.let { "${it.firstName} ${it.middleName} ${it.lastName}" }
        val output = getContactFileFor(contactName)

        // Build an array of arguments
        var arguments = emptyArray<String>()
        var streamFilters = ""
        var filters = ""
        var counter = 0
        parts.forEach {
            val filter = "[a$counter]"
            streamFilters += "[$counter:0]volume=$volumeMultiplier$filter;"
            filters += filter
            counter += 1
            arguments += "-i ${it.absolutePath}"
        }
        arguments += "-filter_complex '${streamFilters}${filters}concat=n=$counter:v=0:a=1[out]'"
        arguments += "-map '[out]' ${output.absolutePath}"

        // Execute FFmpeg command
        Timber.i("ffmpeg $arguments")
        val result = FFmpegKit.execute(arguments)
        val generateSuccess = result.returnCode.isSuccess

        return if (generateSuccess) output else null
    }

    private suspend fun saveRingtone(file: File): Uri? {
        val uri = context.contentResolver.scanRingtone(file)
        if (uri != null) {
            file.delete()
        } else {
            Timber.w("Failed to save ringtone ${file.name}")
        }
        return uri
    }

    private fun getPartFileFor(key: String): File {
        val fileName = key.hashCode().toString()
        return File(cacheDir, fileName)
    }

    private fun getContactFileFor(contactName: String): File {
        val fileName = contactName.replace(" ", "_") + ".ogg"
        return File(cacheDir, fileName)
    }
}
