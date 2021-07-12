package com.boswelja.contactringtonegenerator.ringtonegen.generator

import android.content.Context
import android.net.Uri
import com.arthenica.ffmpegkit.FFmpegKit
import com.boswelja.contactringtonegenerator.common.MediaStoreHelper
import com.boswelja.contactringtonegenerator.contactpicker.ContactsHelper
import com.boswelja.contactringtonegenerator.ringtonebuilder.StructureItem
import com.boswelja.contactringtonegenerator.ringtonegen.Constants
import com.boswelja.tts.Result
import com.boswelja.tts.TextToSpeech
import com.boswelja.tts.withTextToSpeech
import java.io.File
import timber.log.Timber

class RingtoneGenerator(
    private val context: Context,
    private val contactLookupKeys: Array<String>,
    ringtoneStructure: List<StructureItem>
) {

    private val blocks = ringtoneStructure.toBlocks()

    suspend fun generate() {
        contactLookupKeys.forEach { lookupKey ->
            val ringtoneFile = generateRingtoneFor(lookupKey)
            val ringtoneUri = saveRingtone(ringtoneFile) ?: return
            ContactsHelper.setContactRingtone(
                context,
                ContactsHelper.getContactUri(context, lookupKey)!!,
                ringtoneUri
            )
            ringtoneFile.delete()
        }

        // Empty cache on finish
        context.cacheDir.delete()
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

        val file = getPartFileFor(context, synthesisText)
        val synthResult = synthesizeToFile(
            synthesisText,
            getPartFileFor(context, synthesisText)
        )
        if (synthResult != Result.SUCCESS) return null

        return file
    }

    private suspend fun generateRingtoneFor(
        contactLookupKey: String
    ): File {
        // Convert blocks to files
        val parts = mutableListOf<File>()
        context.withTextToSpeech {
            blocks.forEach { item ->
                when (item) {
                    is TextBlock -> {
                        val file = synthesizeTextForContact(
                            contactLookupKey, item.text
                        )
                        requireNotNull(file)
                        parts.add(file)
                    }
                    is FileBlock -> {
                        val file = getPartFileFor(context, item.uri.toString())
                        parts.add(file)
                    }
                }
            }
        }

        val contactName = ContactsHelper.getContactStructuredName(
            context.contentResolver, contactLookupKey
        )!!.let { "${it.firstName} ${it.middleName} ${it.lastName}" }
        var commandInputs = ""
        var filterInputs = ""
        var filters = ""
        var trueFileCount = 0

        parts.forEach {
            val filter = "[a$trueFileCount]"
            filterInputs += "[$trueFileCount:0]volume=1.0$filter;"
            filters += filter
            trueFileCount += 1
            commandInputs += " -i ${it.absolutePath}"
        }

        Timber.d("Got $trueFileCount files")
        val output = getContactFileFor(context, contactName)
        val command =
            "$commandInputs -filter_complex '${filterInputs}${filters}concat=n=$trueFileCount:v=0:a=1[out]' -map '[out]' ${output.absolutePath}"
        Timber.i("ffmpeg $command")
        val result = FFmpegKit.execute(command)
        val generateSuccess = result.returnCode.isSuccess

        return output
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

    private fun getPartFileFor(context: Context, engineRepresentation: String): File {
        val fileName = engineRepresentation.replace(" ", "_") + ".ogg"
        return File(context.cacheDir, fileName)
    }

    private fun getContactFileFor(context: Context, contactName: String): File {
        val fileName = contactName.replace(" ", "_") + ".ogg"
        return File(context.cacheDir, fileName)
    }
}
