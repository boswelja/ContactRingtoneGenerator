package com.boswelja.contactringtonegenerator.tts

import androidx.test.platform.app.InstrumentationRegistry
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.awaitility.kotlin.await
import org.junit.Assert.* // ktlint-disable
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

class TtsManagerTest {

    private val testJobs = arrayOf(
        SynthesisJob("id1", "text"),
        SynthesisJob("id2", "text"),
        SynthesisJob("id3", "text")
    )

    @MockK
    lateinit var engineEventListener: TtsManager.EngineEventListener
    @MockK
    lateinit var progressListener: TtsManager.JobProgressListener

    private val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun jobQueue() {
        val ttsManager = TtsManager(context)
        assertEquals(ttsManager.synthesisJobCount, 0)
        testJobs.forEach {
            ttsManager.enqueueJob(it)
        }
        assertEquals(ttsManager.synthesisJobCount, 3)
        ttsManager.destroy()
    }

    @Test
    fun engineEventListener() {
        val ttsManager = TtsManager(context).apply {
            engineEventListener = this@TtsManagerTest.engineEventListener
        }
        await.atMost(5, TimeUnit.SECONDS).until(ttsManager::isEngineReady)
        verify(exactly = 1) { engineEventListener.onInitialised(any())}

        confirmVerified(engineEventListener)
        ttsManager.destroy()
    }

    @Test
    fun progressListener() {
        val ttsManager = TtsManager(context).apply {
            jobProgressListener = progressListener
        }
        await.atMost(5, TimeUnit.SECONDS).until(ttsManager::isEngineReady)

        testJobs.forEach {
            ttsManager.enqueueJob(it)
        }

        ttsManager.startSynthesis()

        val jobCountRef = ttsManager::synthesisJobCount

        val callable = Callable { jobCountRef.get() == 0 }
        await.atMost(30, TimeUnit.SECONDS).until(callable)

        testJobs.forEach {
            verify(exactly = 1) { progressListener.onJobStarted(it) }
        }
        verify(exactly = 3) { progressListener.onJobCompleted(any(), any()) }

        confirmVerified(progressListener)
        ttsManager.destroy()
    }
}
