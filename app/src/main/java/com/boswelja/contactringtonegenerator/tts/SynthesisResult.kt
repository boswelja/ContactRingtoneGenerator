package com.boswelja.contactringtonegenerator.tts

import java.io.File

data class SynthesisResult(val synthesisId: String, val file: File) {

    override fun equals(other: Any?): Boolean {
        if (other is SynthesisJob) {
            return other.synthesisId == synthesisId
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = synthesisId.hashCode()
        result = 31 * result + file.hashCode()
        return result
    }
}