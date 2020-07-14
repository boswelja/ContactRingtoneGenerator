package com.boswelja.contactringtonegenerator.tts

import androidx.test.core.app.ApplicationProvider
import com.boswelja.contactringtonegenerator.MainApplication
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.* // ktlint-disable
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.reflect.KProperty0

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
        awaitBoolean(TimeUnit.SECONDS.toMillis(5), ttsManager::isEngineReady)
        verify(exactly = 1) { engineEventListener.onInitialised(any()) }

        confirmVerified(engineEventListener)
        ttsManager.destroy()
    }

    @Test
    fun synthesizeToFile() {
        val ttsManager = TtsManager(context)
        awaitBoolean(TimeUnit.SECONDS.toMillis(5), ttsManager::isEngineReady)
        testJobs.forEach {
            runBlocking {
                val result = ttsManager.synthesizeToFile(it)
                assertTrue(result.result.exists())
                assertTrue(result.result.isFile)
            }
        }
    }

    private fun awaitBoolean(atMost: Long, actual: KProperty0<Boolean>) {
        val startTime = System.currentTimeMillis()
        var currentTime = System.currentTimeMillis()
        while (!actual.get() && (currentTime - startTime) < atMost) {
            currentTime = System.currentTimeMillis()
            continue
        }
    }
}
