package com.boswelja.contactringtonegenerator.tts

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.boswelja.contactringtonegenerator.MockitoHelper
import org.awaitility.kotlin.await
import org.junit.Test

import org.junit.Assert.* // ktlint-disable
import org.junit.Before
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

class TtsManagerTest {

    @Mock
    lateinit var engineEventListener: TtsManager.TtsEngineEventListener
    @Mock
    lateinit var progressListener: TtsManager.TtsJobProgressListener

    lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun jobQueue() {
        val ttsManager = TtsManager(context)
        assertEquals(ttsManager.synthesisJobCount, 0)
        ttsManager.enqueueJob(SynthesisJob("id1", "text"))
        ttsManager.enqueueJob(SynthesisJob("id2", "text"))
        ttsManager.enqueueJob(SynthesisJob("id3", "text"))
        assertEquals(ttsManager.synthesisJobCount, 3)
        ttsManager.destroy()
    }

    @Test
    fun engineEventListener() {
        val ttsManager = TtsManager(context).apply {
            engineEventListener = this@TtsManagerTest.engineEventListener
        }
        await.atMost(5, TimeUnit.SECONDS).until(ttsManager::isEngineReady)
        verify(engineEventListener, times(1)).onInitialised(ttsManager.isEngineReady)
        ttsManager.destroy()
    }

    @Test
    fun progressListener() {
        val ttsManager = TtsManager(context).apply {
            jobProgressListener = progressListener
        }
        await.atMost(5, TimeUnit.SECONDS).until(ttsManager::isEngineReady)

        val jobs = arrayOf(
                SynthesisJob("id1", "text"),
                SynthesisJob("id2", "text"),
                SynthesisJob("id3", "text")
        )
        jobs.forEach {
            ttsManager.enqueueJob(it)
        }

        ttsManager.startSynthesis()

        val jobCountRef = ttsManager::synthesisJobCount

        val callable = Callable { jobCountRef.get() == 0 }
        await.atMost(30, TimeUnit.SECONDS).until(callable)

        jobs.forEach {
            verify(progressListener, times(1)).onJobStarted(it)
        }
        verify(progressListener, times(3)).onJobCompleted(ArgumentMatchers.anyBoolean(), MockitoHelper.anyObject())

        ttsManager.destroy()
    }
}