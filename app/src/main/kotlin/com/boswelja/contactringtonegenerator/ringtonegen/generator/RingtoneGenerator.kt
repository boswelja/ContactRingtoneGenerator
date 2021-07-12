package com.boswelja.contactringtonegenerator.ringtonegen.generator

import android.content.Context
import android.net.Uri
import com.arthenica.ffmpegkit.FFmpegKit
import com.boswelja.contactringtonegenerator.common.MediaStoreHelper
import com.boswelja.contactringtonegenerator.contactpicker.ContactsHelper
import com.boswelja.contactringtonegenerator.ringtonebuilder.StructureItem
import com.boswelja.contactringtonegenerator.ringtonegen.Constants
import com.boswelja.contactringtonegenerator.settings.settingsDataStore
import com.boswelja.tts.Result
import com.boswelja.tts.TextToSpeech
import com.boswelja.tts.withTextToSpeech
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class RingtoneGenerator(
    private val context: Context,
    private val contactLookupKeys: Array<String>,
    ringtoneStructure: List<StructureItem>
) {

    private val cacheDir = context.cacheDir
    private val blocks = ringtoneStructure.toBlocks()

    suspend fun generate() {
        // Load settings
        val speechRate = context.settingsDataStore.data.map { it.ttsSpeechRate }.first()
        val voicePitch = context.settingsDataStore.data.map { it.ttsPitch }.first()
        val volumeMultiplier = context.settingsDataStore.data.map { it.volumeMultiplier }.first()

        // Read FileBlock Uris into cache
        blocks.filterIsInstance<FileBlock>().forEach { fileBlock ->
            val inStream = context.contentResolver.openInputStream(fileBlock.uri) ?: return
            getPartFileFor(fileBlock.uri.toString()).outputStream().use {
                inStream.copyTo(it)
            }
        }

        contactLookupKeys.forEach { lookupKey ->
            val ringtoneFile = generateRingtoneFor(
                lookupKey,
                voicePitch,
                speechRate,
                volumeMultiplier
            ) ?: return
            val ringtoneUri = saveRingtone(ringtoneFile) ?: return
            ContactsHelper.setContactRingtone(
                context,
                ContactsHelper.getContactUri(context, lookupKey)!!,
                ringtoneUri
            )
            ringtoneFile.delete()
        }

        // Empty cache on finish
        cacheDir.deleteRecursively()
    }

    private suspend fun TextToSpeech.synthesizeTextForContact(
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
        val synthResult = synthesizeToFile(
            synthesisText,
            getPartFileFor(synthesisText)
        )
        if (synthResult != Result.SUCCESS) return null

        return file
    }

    private suspend fun generateRingtoneFor(
        contactLookupKey: String,
        voicePitch: Float,
        speechRate: Float,
        volumeMultiplier: Float
    ): File? {
        // Convert blocks to files
        val parts = mutableListOf<File>()
        val success = context.withTextToSpeech(
            voicePitch = voicePitch,
            speechRate = speechRate
        ) {
            blocks.forEach { item ->
                val file = when (item) {
                    is TextBlock -> synthesizeTextForContact(contactLookupKey, item.text)
                    is FileBlock -> getPartFileFor(item.uri.toString())
                } ?: return@withTextToSpeech false
                parts.add(file)
            }
            return@withTextToSpeech true
        }
        if (!success) return null

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
        val uri = MediaStoreHelper.scanNewFile(context, file)
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