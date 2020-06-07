package com.boswelja.contactringtonegenerator.tts

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
        private val utteranceIdCounter = AtomicInteger()

        fun create(message: String): SynthesisJob =
                SynthesisJob(utteranceIdCounter.incrementAndGet().toString(), message)
    }
}