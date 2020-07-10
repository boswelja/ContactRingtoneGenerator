package com.boswelja.contactringtonegenerator.tts

import androidx.test.core.app.ApplicationProvider
import com.boswelja.contactringtonegenerator.MainApplication
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.awaitility.kotlin.await
import org.junit.Assert.* // ktlint-disable
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class TtsManagerTest {

    private val testJobs = arrayOf(
        SynthesisJob("id1", "text"),
        SynthesisJob("id2", "text"),
        SynthesisJob("id3", "text")
    )

    @MockK
    lateinit var engineEventListener: TtsManager.EngineEventListener

    private val context = ApplicationProvider.getApplicationContext<MainApplication>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun engineEventListener() {
        val ttsManager = TtsManager(context).apply {
            engineEventListener = this@TtsManagerTest.engineEventListener
        }
        await.atMost(5, TimeUnit.SECONDS).until(ttsManager::isEngineReady)
        verify(exactly = 1) { engineEventListener.onInitialised(any()) }

        confirmVerified(engineEventListener)
        ttsManager.destroy()
    }

    @Test
    fun synthesizeToFile() {
        val ttsManager = TtsManager(context)
        await.atMost(5, TimeUnit.SECONDS).until(ttsManager::isEngineReady)
        testJobs.forEach {
            runBlocking {
                val result = ttsManager.synthesizeToFile(it)
                assertTrue(result.result.exists())
                assertTrue(result.result.isFile)
            }
        }
    }

}
