package com.boswelja.contactringtonegenerator.ui

import com.boswelja.contactringtonegenerator.ui.results.ResultsFragment
import com.boswelja.contactringtonegenerator.ui.results.ResultsFragment.Companion.getState
import org.junit.Assert.* // ktlint-disable
import org.junit.Test

class ResultsFragmentTest {

    @Test
    fun testStateGetter() {
        assertEquals(ResultsFragment.State.SUCCESSFUL, getState(1, 0))
        assertEquals(ResultsFragment.State.SUCCESSFUL, getState(10, -1))
        assertEquals(ResultsFragment.State.FAILED, getState(0, 1))
        assertEquals(ResultsFragment.State.FAILED, getState(-1, 10))
        assertEquals(ResultsFragment.State.MIXED, getState(1, 1))
        assertEquals(ResultsFragment.State.MIXED, getState(10, 10))
        assertEquals(ResultsFragment.State.UNKNOWN, getState(0, 0))
    }
}
