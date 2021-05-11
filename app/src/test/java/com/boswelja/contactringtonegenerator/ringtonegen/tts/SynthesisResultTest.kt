package com.boswelja.contactringtonegenerator.ringtonegen.tts

import org.junit.Assert.* // ktlint-disable
import org.junit.Test
import java.io.File

class SynthesisResultTest {

    @Test
    fun testEquals() {
        val rootFile = File.listRoots().first()
        val job1 = SynthesisResult("id1", rootFile)
        val job2 = SynthesisResult("id1", rootFile)
        val job3 = SynthesisResult("id1", File(rootFile, "child"))
        val job4 = SynthesisResult("id2", rootFile)
        assertEquals(job1, job2)
        assertEquals(job1, job3)
        assertNotEquals(job1, job4)
    }
}
