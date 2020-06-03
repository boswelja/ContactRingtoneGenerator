package com.boswelja.contactringtonegenerator.ui

import android.animation.LayoutTransition
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.databinding.ActivityMainBinding
import com.boswelja.contactringtonegenerator.tts.TtsManager
import com.boswelja.contactringtonegenerator.tts.TtsUtterance

class MainActivity : AppCompatActivity(), TtsManager.TtsManagerInterface {

    private lateinit var binding: ActivityMainBinding
    lateinit var ttsManager: TtsManager

    override fun onTtsReady() {
    }

    override fun onSynthesisComplete() {
    }

    override fun onStartSynthesizing(jobCount: Int) {
    }

    override fun onJobStart(ttsUtterance: TtsUtterance) {
    }

    override fun onJobFinished(ttsUtterance: TtsUtterance) {
    }

    override fun onJobError(ttsUtterance: TtsUtterance) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ttsManager = TtsManager(this).also {
            it.addTtsManagerInterface(this)
            it.initTts()
        }
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
        }

        val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container)?.findNavController()
        if (navController != null) {
            val appBarConfiguration = AppBarConfiguration(setOf(
                    R.id.getStartedFragment,
                    R.id.loadingFragment
            ))

            binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        }
    }

    fun removeTitle() {
        binding.toolbar.title = null
    }

    fun setSubtitle(subtitle: String?) {
        binding.toolbar.apply {
            this.subtitle = subtitle
            layoutTransition = LayoutTransition()
        }
    }
}
