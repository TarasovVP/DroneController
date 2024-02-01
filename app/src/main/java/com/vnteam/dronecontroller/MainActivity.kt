package com.vnteam.dronecontroller

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import com.vnteam.dronecontroller.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setNavController()
    }

    private fun setNavController() {
        navController = (supportFragmentManager.findFragmentById(
            R.id.host_main_fragment
        ) as NavHostFragment).navController
        val navGraph = navController?.navInflater?.inflate(R.navigation.navigation)
        navGraph?.let { navController?.setGraph(it, intent.extras) }
    }

    fun showToast(toastMsg: String) {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            binding?.root?.let { Snackbar.make(it, toastMsg, Snackbar.LENGTH_LONG).show() }
        }
    }

    fun setProgressVisibility(visible: Boolean) {
        binding?.progressBar?.isVisible = visible
    }
}