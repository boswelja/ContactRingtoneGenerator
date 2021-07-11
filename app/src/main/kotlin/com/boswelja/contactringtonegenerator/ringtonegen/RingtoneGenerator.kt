package com.boswelja.contactringtonegenerator.ringtonegen

import android.content.Context
import android.net.Uri
import com.arthenica.ffmpegkit.FFmpegKit
import com.boswelja.contactringtonegenerator.common.MediaStoreHelper
import com.boswelja.contactringtonegenerator.contactpicker.ContactsHelper
import com.boswelja.contactringtonegenerator.ringtonegen.item.Constants
import com.boswelja.contactringtonegenerator.ringtonegen.item.ContactDataItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.CustomAudioItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.CustomTextItem
import com.boswelja.contactringtonegenerator.ringtonegen.item.StructureItem
import com.boswelja.tts.Result
import com.boswelja.tts.TextToSpeech
import com.boswelja.tts.withTextToSpeech
import java.io.File
import timber.log.Timber

class RingtoneGenerator(
    private val context: Context,
    private val contactLookupKeys: Array<String>,
    private val ringtoneStructure: List<StructureItem>
) {

    suspend fun generate() {
        contactLookupKeys.forEach { lookupKey ->
            val ringtoneFile = generateRingtoneFor(lookupKey, ringtoneStructure)
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

    /**
     * Synthesize all static text elements of the ringtone. Currently, this is only
     * [CustomTextItem.CustomText].
     * @param ringtoneStructure The structure of the ringtone.
     */
    private suspend fun synthesizeStaticParts(
        ringtoneStructure: List<StructureItem>
    ): Boolean {
        var synthResult = true
        context.withTextToSpeech {
            ringtoneStructure.forEach { item ->
                when (item) {
                    is CustomAudioItem -> {
                        // TODO Load audio files
                    }
                    is CustomTextItem -> {
                        // Get file and start synthesis
                        val file =
                            getPartFileFor(context, item.data!!)
                        val result = synthesizeToFile(item.data!!, file)
                        // If even one synthesis fails, fail the job.
                        if (result != Result.SUCCESS) {
                            synthResult = false
                            return@withTextToSpeech
                        }
                    }
                    else -> { }
                }
            }
        }
        return synthResult
    }

    private suspend fun TextToSpeech.synthesizeContactName(
        contactLookupKey: String,
        text: String
    ): File? {
        val textToSynthesize = when (text) {
            Constants.NAME_PREFIX_PLACEHOLDER -> {
                ContactsHelper.getContactStructuredName(
                    context.contentResolver,
                    contactLookupKey
                )?.prefix
            }
            Constants.NAME_SUFFIX_PLACEHOLDER -> {
                ContactsHelper.getContactStructuredName(
                    context.contentResolver,
                    contactLookupKey
                )?.suffix
            }
            Constants.FIRST_NAME_PLACEHOLDER -> {
                ContactsHelper.getContactStructuredName(
                    context.contentResolver,
                    contactLookupKey
                )?.firstName
            }
            Constants.MIDDLE_NAME_PLACEHOLDER -> {
                ContactsHelper.getContactStructuredName(
                    context.contentResolver,
                    contactLookupKey
                )?.middleName
            }
            Constants.LAST_NAME_PLACEHOLDER -> {
                ContactsHelper.getContactStructuredName(
                    context.contentResolver,
                    contactLookupKey
                )?.lastName
            }
            Constants.NICKNAME_PLACEHOLDER -> {
                ContactsHelper.getContactNickname(
                    context.contentResolver,
                    contactLookupKey
                )
            }
            else -> throw IllegalArgumentException("Unrecognised engine text for dynamic data")
        }
        return textToSynthesize?.let {
            val file = getPartFileFor(context, textToSynthesize)
            val synthResult = synthesizeToFile(
                textToSynthesize,
                getPartFileFor(context, textToSynthesize)
            )
            require(synthResult == Result.SUCCESS)
            file
        }
    }

    private suspend fun generateRingtoneFor(
        contactLookupKey: String,
        structure: List<StructureItem>
    ): File {
        val parts = mutableListOf<File>()
        context.withTextToSpeech {
            structure.forEach { item ->
                val file = when (item) {
                    is ContactDataItem -> {
                        synthesizeContactName(contactLookupKey, item.data!!)
                    }
                    is CustomAudioItem,
                    is CustomTextItem -> {
                        getPartFileFor(context, item.data!!)
                    }
                }
                if (file != null) {
                    parts.add(file)
                } else {
                    Timber.w("Failed to generate part")
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
