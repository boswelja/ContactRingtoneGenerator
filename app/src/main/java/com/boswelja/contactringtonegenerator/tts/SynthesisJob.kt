package com.boswelja.contactringtonegenerator.tts

class SynthesisJob(val id: String, val text: String) {

    override fun equals(other: Any?): Boolean {
        if (other is SynthesisJob) {
            return other.id == id
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + text.hashCode()
        return result
    }
}
