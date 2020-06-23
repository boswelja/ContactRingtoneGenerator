package com.boswelja.contactringtonegenerator.ui

import android.animation.LayoutTransition
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.contacts.Contact
import com.boswelja.contactringtonegenerator.databinding.ActivityMainBinding
import com.boswelja.contactringtonegenerator.ringtonegen.RingtoneGenerator
import com.boswelja.contactringtonegenerator.tts.TtsManager
import com.boswelja.contactringtonegenerator.ringtonegen.item.BaseItem

class MainActivity : AppCompatActivity() {

    val selectedContacts = ArrayList<Contact>()
    val ringtoneItems = ArrayList<BaseItem>()

    var ringtoneGenerator: RingtoneGenerator? = null
        private set
    private lateinit var binding: ActivityMainBinding
    lateinit var ttsManager: TtsManager
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                    R.id.generatingFlow))

            binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        }
    }

    override fun onStart() {
        super.onStart()
        ttsManager = TtsManager(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ringtoneGenerator?.destroy()
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

    fun createRingtoneGenerator(): RingtoneGenerator {
        ringtoneGenerator?.destroy()
        ringtoneGenerator = RingtoneGenerator(this, ttsManager, ringtoneItems, selectedContacts)
        return ringtoneGenerator!!
    }
}
