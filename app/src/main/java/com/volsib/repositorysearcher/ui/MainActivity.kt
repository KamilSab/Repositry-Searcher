package com.volsib.repositorysearcher.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.volsib.repositorysearcher.R
import com.volsib.repositorysearcher.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        const val NUMBER_OF_REQUEST = 23401
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Для api ниже 31
        installSplashScreen()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val minSplashScreenAnimationDuration = 2000L
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                Handler(Looper.getMainLooper()).postDelayed({
                    splashScreenView.remove()
                }, minSplashScreenAnimationDuration)
            }
        }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        // Скрываем нижнюю навигацию на экранах логина и регистрации
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment -> {
                    binding.bottomNav.visibility = android.view.View.GONE
                }
                else -> {
                    binding.bottomNav.visibility = android.view.View.VISIBLE
                }
            }
        }

        // Проверяем разрешение на чтение и запись в External Storage для Android 6 и меньше
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            checkExternalStoragePermission()
        }
    }

    private fun checkExternalStoragePermission() {
        val canRead = ContextCompat.checkSelfPermission(this , Manifest.permission.READ_EXTERNAL_STORAGE)
        val canWrite = ContextCompat.checkSelfPermission(this , Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (canRead != PackageManager.PERMISSION_GRANTED || canWrite != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE), NUMBER_OF_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            NUMBER_OF_REQUEST -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // Пользователь отклонил разрешение - выходим из приложения
                    finishAndRemoveTask()
                }
                return
            }
        }
    }
}