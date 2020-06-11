package com.boswelja.contactringtonegenerator.tts

import com.boswelja.contactringtonegenerator.ui.ringtonecreator.item.ID
import java.util.concurrent.atomic.AtomicInteger

class SynthesisJob private constructor(val synthesisId: String, val message: String) {

    override fun equals(other: Any?): Boolean {
        if (other is SynthesisJob) {
            return other.synthesisId == synthesisId
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = synthesisId.hashCode()
        result = 31 * result + message.hashCode()
        return result
    }

    companion object {
        private val utteranceIdCounter = AtomicInteger(ID.values().last().id.toInt() + 1)

        fun create(message: String): SynthesisJob =
                create(message, utteranceIdCounter.incrementAndGet().toString())

        fun create(message: String, id: String) = SynthesisJob(id, message)
    }
}