package com.boswelja.contactringtonegenerator.ringtonegen

import android.content.Context
import android.os.Build
import android.os.Environment
import android.speech.tts.Voice
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.tts.TtsManager
import com.boswelja.contactringtonegenerator.tts.SynthesisJob
import com.boswelja.contactringtonegenerator.tts.SynthesisResult
import java.io.File
import java.util.Locale
import kotlin.collections.ArrayList

class RingtoneGenerator(context: Context) :
    TtsManager.TtsJobProgressListener,
    TtsManager.TtsEngineEventListener,
    AudioJoiner.JoinProgressListener {

    private val ringtoneDirectory: File = context.cacheDir

    override fun onInitialised(success: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onJobStarted(synthesisJob: SynthesisJob) {
        TODO("Not yet implemented")
    }

    override fun onJobCompleted(success: Boolean, synthesisResult: SynthesisResult) {
        TODO("Not yet implemented")
    }

    override fun onJoinStarted(jobId: String) {
        TODO("Not yet implemented")
    }

    override fun onJoinCompleted(jobId: String) {
        TODO("Not yet implemented")
    }

    override fun onJoinFailed(jobId: String) {
        TODO("Not yet implemented")
    }
}