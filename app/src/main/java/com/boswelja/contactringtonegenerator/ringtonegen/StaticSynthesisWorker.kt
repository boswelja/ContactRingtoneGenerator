package com.boswelja.contactringtonegenerator.ringtonegen

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.boswelja.contactringtonegenerator.ringtonegen.item.StructureItem
import com.boswelja.tts.TextToSpeech
import java.io.File

class StaticSynthesisWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val tts by lazy { TextToSpeech(context) }

    override suspend fun doWork(): Result {
        // Collect static items to generate
        val targets = inputData.getStringArray(DATA_STATIC_SYNTHESIS_ITEMS)
        if (targets.isNullOrEmpty()) return Result.success() // No work to do, return success
        targets.forEach { target ->
            // Get file and start synthesis
            val file = getFileFor(applicationContext, target)
            val result = tts.synthesizeToFile(target, file)
            // If even one synthesis fails, fail the job. We need all these to generate one ringtone
            if (result != com.boswelja.tts.Result.SUCCESS) {
                return Result.failure()
            }
        }

        return Result.success(inputData)
    }

    companion object {
        const val DATA_STATIC_SYNTHESIS_ITEMS = "static-synth-items"

        fun Array<StructureItem<*>>.toStringArray() = map { it.engineRepresentation }

        fun getFileFor(context: Context, engineRepresentation: String): File {
            val fileName = engineRepresentation.replace(" ", "_") + ".ogg"
            return File(context.cacheDir, fileName)
        }
    }
}
