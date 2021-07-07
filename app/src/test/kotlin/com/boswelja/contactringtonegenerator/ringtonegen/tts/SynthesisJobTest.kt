package com.boswelja.contactringtonegenerator.ringtonegen.tts

import org.junit.Assert.* // ktlint-disable
import org.junit.Test

class SynthesisJobTest {

    @Test
    fun testEquals() {
        val job1 = SynthesisJob("id1", "text")
        val job2 = SynthesisJob("id1", "text")
        val job3 = SynthesisJob("id1", "text2")
        val job4 = SynthesisJob("id2", "text")
        assertEquals(job1, job2)
        assertEquals(job1, job3)
        assertNotEquals(job1, job4)
    }
}
