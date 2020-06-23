package com.boswelja.contactringtonegenerator

import org.junit.Assert.*
import org.junit.Test

class StringJoinerCompatTest {
    @Test
    fun testOutput() {
        val joiner = StringJoinerCompat(" ")
        joiner.add("Hello")
        joiner.add("World!")
        val result = joiner.toString()
        assertEquals(result, "Hello World!")
    }

    @Test
    fun testLength() {
        val joiner = StringJoinerCompat(" ")
        joiner.add("Hello")
        joiner.add("World!")
        val length = joiner.length
        assertEquals(length, "Hello World!".length)
    }
}