package com.boswelja.contactringtonegenerator.ringtonegen

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.ringtonegen.item.TextItem
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Assert.* // ktlint-disable
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.reflect.KFunction0

class RingtoneGeneratorTest {

    private val testRingtoneStructure = listOf(
        TextItem.FirstName(),
        TextItem.Custom().apply { text = "is calling" }
    )
    private val testContacts: List<Contact> = ArrayList<Contact>().apply {
        (0 until TEST_CONTACTS_COUNT).forEach {
            add(Contact(it.toLong(), "none", "Name $it"))
        }
    }

    @MockK
    lateinit var progressListener: RingtoneGenerator.ProgressListener

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun testTotalJobCount() {
        val ringtoneGenerator = createRingtoneGenerator()

        awaitState(TimeUnit.SECONDS.toMillis(10), ringtoneGenerator.state::getValue, RingtoneGenerator.State.READY)
        assertEquals(TRUE_JOB_COUNT, ringtoneGenerator.totalJobCount)
        ringtoneGenerator.destroy()
    }

    @Test
    fun testProgressListener() {
        val ringtoneGenerator = createRingtoneGenerator()
        ringtoneGenerator.progressListener = progressListener

        awaitState(TimeUnit.SECONDS.toMillis(10), ringtoneGenerator.state::getValue, RingtoneGenerator.State.READY)
        ringtoneGenerator.start()

        awaitState(TimeUnit.SECONDS.toMillis(30), ringtoneGenerator.state::getValue, RingtoneGenerator.State.FINISHED)
        testContacts.forEach {
            verify(exactly = 1) { progressListener.onJobStarted(it) }
        }
        verify(exactly = TRUE_JOB_COUNT) { progressListener.onJobCompleted(any(), any()) }

        confirmVerified(progressListener)
        ringtoneGenerator.destroy()
    }

    private fun awaitState(atMost: Long, actual: KFunction0<RingtoneGenerator.State?>, expected: RingtoneGenerator.State) {
        val startTime = System.currentTimeMillis()
        var currentTime = System.currentTimeMillis()
        while (actual.invoke() != expected && (currentTime - startTime) < atMost) {
            currentTime = System.currentTimeMillis()
            continue
        }
    }

    private fun createRingtoneGenerator(): RingtoneGenerator =
        RingtoneGenerator(context).apply {
            contacts = testContacts
            ringtoneStructure = testRingtoneStructure
        }

    companion object {
        private const val TEST_CONTACTS_COUNT = 3
        private const val TRUE_JOB_COUNT = TEST_CONTACTS_COUNT
    }
}
