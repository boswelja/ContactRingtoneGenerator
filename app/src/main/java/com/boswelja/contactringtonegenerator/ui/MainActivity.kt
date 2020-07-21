package com.boswelja.contactringtonegenerator.ui

import android.animation.LayoutTransition
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
        }

        setupNavController()
    }

    private fun setupNavController() {
        val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container)?.findNavController()
        if (navController != null) {
            val appBarConfiguration = AppBarConfiguration(
                setOf(R.id.getStartedFragment, R.id.loadingFragment, R.id.resultsFragment)
            )
            binding.toolbar.apply {
                setupWithNavController(navController, appBarConfiguration)
                navController.addOnDestinationChangedListener { _, destination, _ ->
                    title = destination.label
                }
            }
        }
    }

    fun setSubtitle(subtitle: String?) {
        binding.toolbar.apply {
            this.subtitle = subtitle
            layoutTransition = LayoutTransition()
        }
    }
}
