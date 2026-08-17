package com.canvas.android.app

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.canvas.android.app.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_resolution,
                R.id.nav_frame_rate,
                R.id.nav_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        Shizuku.addRequestPermissionResultListener { _, grantResult ->
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                mainViewModel.shizukuPermissionGranted.value = true
                Snackbar.make(binding.root, R.string.shizuku_permission_granted, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkPermission()) {
            mainViewModel.shizukuPermissionGranted.value = true
        } else {
            Snackbar.make(binding.root, R.string.shizuku_not_available, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun checkPermission(): Boolean {
        return try {
            if (Shizuku.isPreV11()) return false
            when {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> true
                Shizuku.shouldShowRequestPermissionRationale() -> false
                else -> {
                    Shizuku.requestPermission(0)
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
