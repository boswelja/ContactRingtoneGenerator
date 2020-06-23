package com.boswelja.contactringtonegenerator.tts

import java.io.File

data class SynthesisResult(val id: String, val result: File) {

    override fun equals(other: Any?): Boolean {
        if (other is SynthesisJob) {
            return other.id == id
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + this.result.hashCode()
        return result
    }
}
