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
import com.boswelja.contactringtonegenerator.tts.TtsManager
import com.boswelja.contactringtonegenerator.ringtonegen.item.BaseItem

class MainActivity : AppCompatActivity() {

    val ttsManager by lazy { TtsManager(this) }
    val selectedContacts = ArrayList<Contact>()
    val ringtoneItems = ArrayList<BaseItem>()

    var ttsEngine: String? = null
        set(value) {
            if (field != value) {
                field = value
                ttsManager.setEngine(field)
            }
        }

    private lateinit var binding: ActivityMainBinding

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
