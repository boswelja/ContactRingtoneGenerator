package com.boswelja.contactringtonegenerator.ui

import com.boswelja.contactringtonegenerator.ui.FinishedFragment.Companion.getState
import org.junit.Assert.*
import org.junit.Test

class FinishedFragmentTest {

    @Test
    fun testStateGetter() {
        assertEquals(FinishedFragment.State.SUCCESSFUL, getState(1, 0))
        assertEquals(FinishedFragment.State.SUCCESSFUL, getState(10, -1))
        assertEquals(FinishedFragment.State.FAILED, getState(0, 1))
        assertEquals(FinishedFragment.State.FAILED, getState(-1, 10))
        assertEquals(FinishedFragment.State.MIXED, getState(1, 1))
        assertEquals(FinishedFragment.State.MIXED, getState(10, 10))
        assertEquals(FinishedFragment.State.UNKNOWN, getState(0, 0))
    }
}