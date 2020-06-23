package com.boswelja.contactringtonegenerator

class StringJoinerCompat(private val delimiter: CharSequence) {

    private var value: StringBuilder = StringBuilder()

    val length: Int get() = value.length

    override fun toString(): String {
        return value.toString()
    }

    fun add(newElement: CharSequence?): StringJoinerCompat {
        if (value.isNotEmpty()) value.append(delimiter)
        value.append(newElement)
        return this
    }
}