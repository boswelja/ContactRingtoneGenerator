package com.boswelja.contactringtonegenerator

class StringJoinerCompat(private val delimiter: CharSequence) {

    private var value: StringBuilder = StringBuilder()

    override fun toString(): String {
        return value.toString()
    }

    fun add(newElement: CharSequence?): StringJoinerCompat {
        if (value.isNotEmpty()) value.append(delimiter)
        value.append(newElement)
        return this
    }

    fun length(): Int {
        // Remember that we never actually append the suffix unless we return
        // the full (present) value or some sub-string or length of it, so that
        // we can add on more if we need to.
        return value.length
    }
}