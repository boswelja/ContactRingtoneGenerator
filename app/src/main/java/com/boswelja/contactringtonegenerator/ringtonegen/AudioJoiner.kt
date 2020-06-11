package com.boswelja.contactringtonegenerator.ringtonegen

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AudioJoiner {

    private val jobs = ArrayList<AudioJoinJob>()
    private val coroutineScope = MainScope()

    private var isJoining: Boolean = false

    var progressListener: JoinProgressListener? = null

    private fun startJoining() {
        if (!isJoining) {
            isJoining = true
            coroutineScope.launch(Dispatchers.IO) {
                jobs.forEach { job ->
                    val jobId = job.id
                    notifyJobStarted(jobId)
                    // TODO Join files
                    val success = job.outFile.exists()
                    if (success) {
                        notifyJobCompleted(jobId)
                    } else {
                        notifyJobFailed(jobId)
                    }
                }
                isJoining = false
            }
        }
    }

    private suspend fun notifyJobStarted(id: String) {
        withContext(Dispatchers.Main) {
            progressListener?.onJoinStarted(id)
        }
    }

    private suspend fun notifyJobCompleted(id: String) {
        withContext(Dispatchers.Main) {
            progressListener?.onJoinCompleted(true, id)
        }
    }

    private suspend fun notifyJobFailed(id: String) {
        withContext(Dispatchers.Main) {
            progressListener?.onJoinCompleted(false, id)
        }
    }

    fun enqueue(job: AudioJoinJob) {
        jobs.add(job)
        startJoining()
    }

    interface JoinProgressListener {
        fun onJoinStarted(jobId: String)
        fun onJoinCompleted(isSuccessful: Boolean, jobId: String)
    }
}