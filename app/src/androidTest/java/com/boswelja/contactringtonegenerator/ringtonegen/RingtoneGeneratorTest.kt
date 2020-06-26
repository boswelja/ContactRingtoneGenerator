package com.boswelja.contactringtonegenerator.ringtonegen

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.boswelja.contactringtonegenerator.MockitoHelper
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.ringtonegen.item.ContactName
import com.boswelja.contactringtonegenerator.ringtonegen.item.TextItem
import org.awaitility.kotlin.await
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.junit.Assert.* // ktlint-disable
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.util.concurrent.TimeUnit

class RingtoneGeneratorTest {

    private val testRingtoneStructure = listOf(
        ContactName(),
        TextItem().apply { text = "is calling" }
    )
    private val testContacts: List<Contact> = ArrayList<Contact>().apply {
        (0 until TEST_CONTACTS_COUNT).forEach {
            add(Contact(it.toLong(), "none", "Name $it"))
        }
    }

    @Mock
    lateinit var progressListener: RingtoneGenerator.ProgressListener
    @Mock
    lateinit var stateListener: RingtoneGenerator.StateListener

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun testTotalJobCount() {
        val ringtoneGenerator = RingtoneGenerator(context, testRingtoneStructure, testContacts)
        await.atMost(10, TimeUnit.SECONDS).until(ringtoneGenerator::state, stateMatcher(RingtoneGenerator.State.READY))
        assertEquals(TRUE_JOB_COUNT, ringtoneGenerator.totalJobCount)
        ringtoneGenerator.destroy()
    }

    @Test
    fun testStateListener() {
        val ringtoneGenerator = RingtoneGenerator(context, testRingtoneStructure, testContacts)
        ringtoneGenerator.stateListener = stateListener

        assertEquals(RingtoneGenerator.State.NOT_READY, ringtoneGenerator.state)

        await.atMost(10, TimeUnit.SECONDS).until(ringtoneGenerator::state, stateMatcher(RingtoneGenerator.State.READY))
        verify(stateListener, times(1)).onStateChanged(RingtoneGenerator.State.READY)

        ringtoneGenerator.start()
        verify(stateListener, times(1)).onStateChanged(RingtoneGenerator.State.GENERATING)

        await.atMost(30, TimeUnit.SECONDS).until(ringtoneGenerator::state, stateMatcher(RingtoneGenerator.State.FINISHED))
        verify(stateListener, times(1)).onStateChanged(RingtoneGenerator.State.FINISHED)
    }

    @Test
    fun testProgressListener() {
        val ringtoneGenerator = RingtoneGenerator(context, testRingtoneStructure, testContacts)
        ringtoneGenerator.progressListener = progressListener

        await.atMost(10, TimeUnit.SECONDS).until(ringtoneGenerator::state, stateMatcher(RingtoneGenerator.State.READY))
        ringtoneGenerator.start()

        await.atMost(30, TimeUnit.SECONDS).until(ringtoneGenerator::state, stateMatcher(RingtoneGenerator.State.FINISHED))
        testContacts.forEach {
            verify(progressListener, times(1)).onJobStarted(it)
        }
        verify(progressListener, times(TRUE_JOB_COUNT)).onJobCompleted(ArgumentMatchers.anyBoolean(), MockitoHelper.anyObject())
    }

    /**
     * Creates a [BaseMatcher] for matching [RingtoneGenerator.State].
     * @param state The state to match.
     * @return The matcher.
     */
    private fun stateMatcher(state: RingtoneGenerator.State) = object : BaseMatcher<RingtoneGenerator.State>() {
        override fun matches(actual: Any?): Boolean {
            return (actual is RingtoneGenerator.State) && (actual == state)
        }

        override fun describeTo(description: Description?) {}
    }

    companion object {
        private const val TEST_CONTACTS_COUNT = 3
        private const val TRUE_JOB_COUNT = TEST_CONTACTS_COUNT
    }
}
