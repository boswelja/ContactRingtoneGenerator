package com.boswelja.contactringtonegenerator.ringtonegen

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.boswelja.contactringtonegenerator.ringtonegen.item.StructureItem
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class RingtoneGeneratorWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        setProgress(
            workDataOf(
                Outputs.Progress to 0f,
                Outputs.FailedContactLookupKeys to emptyArray<String>()
            )
        )
        // Get input structure
        val structure = inputData.getStringArray(Inputs.RingtoneStructure)?.map {
            Json.decodeFromString<StructureItem>(it)
        } ?: return Result.failure()
        // Get input contacts
        val contacts = inputData.getStringArray(Inputs.ContactLookupKeys)
            ?: return Result.failure()

        val generator = RingtoneGenerator(
            applicationContext,
            contacts,
            structure
        )

        generator.generate()

        return Result.success(
            workDataOf(
                Outputs.Result to GeneratorResult.SUCCESSFUL.name,
                Outputs.Progress to 1f,
                Outputs.FailedContactLookupKeys to emptyArray<String>()
            )
        )
    }

    object Inputs {
        const val RingtoneStructure = "ringtone-structure"
        const val ContactLookupKeys = "contact-keys"
    }

    object Outputs {
        const val Progress = "progress"
        const val FailedContactLookupKeys = "failed-keys"
        const val Result = "result"
    }
}
