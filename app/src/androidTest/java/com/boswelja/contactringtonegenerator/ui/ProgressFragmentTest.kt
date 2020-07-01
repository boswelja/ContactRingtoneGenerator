package com.boswelja.contactringtonegenerator.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.boswelja.contactringtonegenerator.R

import org.junit.Assert.* // ktlint-disable
import org.junit.Test

class ProgressFragmentTest {

    @Test
    fun testViewVisibility() {
        createScenario()
        onView(withId(R.id.loading_title)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.loading_status)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.progress_bar)).check(matches(isCompletelyDisplayed()))
    }

    private fun createScenario() =
            launchFragmentInContainer<ProgressFragment>(themeResId = R.style.AppTheme)
}