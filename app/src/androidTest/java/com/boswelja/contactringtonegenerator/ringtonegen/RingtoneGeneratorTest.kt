package com.boswelja.contactringtonegenerator.ringtonegen

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.ringtonegen.item.FirstName
import com.boswelja.contactringtonegenerator.ringtonegen.item.CustomText
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.awaitility.kotlin.await
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.junit.Assert.* // ktlint-disable
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class RingtoneGeneratorTest {

    private val testRingtoneStructure = listOf(
        FirstName(),
        CustomText().apply { text = "is calling" }
    )
    private val testContacts: List<Contact> = ArrayList<Contact>().apply {
        (0 until TEST_CONTACTS_COUNT).forEach {
            add(Contact(it.toLong(), "none", "Name $it"))
        }
    }

    @MockK
    lateinit var progressListener: RingtoneGenerator.ProgressListener
    @MockK
    lateinit var stateListener: RingtoneGenerator.StateListener

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        MockKAnnotations.init(this, relaxed = true)
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
        verify(exactly = 1) { stateListener.onStateChanged(RingtoneGenerator.State.READY) }

        ringtoneGenerator.start()
        verify(exactly = 1) { stateListener.onStateChanged(RingtoneGenerator.State.GENERATING) }

        await.atMost(30, TimeUnit.SECONDS).until(ringtoneGenerator::state, stateMatcher(RingtoneGenerator.State.FINISHED))
        verify(exactly = 1) { stateListener.onStateChanged(RingtoneGenerator.State.FINISHED) }

        confirmVerified(stateListener)
        ringtoneGenerator.destroy()
    }

    @Test
    fun testProgressListener() {
        val ringtoneGenerator = RingtoneGenerator(context, testRingtoneStructure, testContacts)
        ringtoneGenerator.progressListener = progressListener

        await.atMost(10, TimeUnit.SECONDS).until(ringtoneGenerator::state, stateMatcher(RingtoneGenerator.State.READY))
        ringtoneGenerator.start()

        await.atMost(30, TimeUnit.SECONDS).until(ringtoneGenerator::state, stateMatcher(RingtoneGenerator.State.FINISHED))
        testContacts.forEach {
            verify(exactly = 1) { progressListener.onJobStarted(it) }
        }
        verify(exactly = TRUE_JOB_COUNT) { progressListener.onJobCompleted(any(), any()) }

        confirmVerified(progressListener)
        ringtoneGenerator.destroy()
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
