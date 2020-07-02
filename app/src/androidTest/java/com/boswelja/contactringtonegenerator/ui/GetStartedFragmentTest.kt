package com.boswelja.contactringtonegenerator.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.boswelja.contactringtonegenerator.R
import org.junit.Assert.* // ktlint-disable
import org.junit.Test

class GetStartedFragmentTest {

    @Test
    fun testViewVisibility() {
        createScenario()
        onView(withId(R.id.app_icon_view)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.welcome_to_view)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.app_name_view)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.get_started_button)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.get_started_view)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.settings_button)).check(matches(isCompletelyDisplayed()))
    }

    @Test
    fun testNextNavigation() {
        val navController = createNavController()
        val scenario = createScenario()
        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }
        onView(withId(R.id.get_started_button)).perform(click())
        assertEquals(R.id.contactPickerFragment, navController.currentDestination?.id)
    }

    @Test
    fun testSettingsNavigation() {
        val navController = createNavController()
        val scenario = createScenario()
        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }
        onView(withId(R.id.settings_button)).perform(click())
        assertEquals(R.id.settingsFragment, navController.currentDestination?.id)
    }

    private fun createNavController() = TestNavHostController(ApplicationProvider.getApplicationContext()).apply {
        setGraph(R.navigation.main_navigation)
    }
    private fun createScenario() = launchFragmentInContainer<GetStartedFragment>(themeResId = R.style.AppTheme)
}
