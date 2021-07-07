package com.boswelja.contactringtonegenerator

import org.junit.Assert.* // ktlint-disable
import org.junit.Test
import timber.log.Timber

class MainApplicationTest {

    @Test
    fun onCreate() {
        val expectedTreeCount = if (BuildConfig.DEBUG) 1 else 0
        assertEquals(Timber.treeCount(), expectedTreeCount)
    }
}
