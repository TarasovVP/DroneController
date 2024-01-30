package com.vnteam.dronecontroller.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.vnteam.dronecontroller.base.BaseFragment
import com.vnteam.dronecontroller.databinding.FragmentMainBinding

class MainFragment : BaseFragment<FragmentMainBinding, MainViewModel>() {

    override val viewModelClass = MainViewModel::class.java
    override fun getViewBinding() = FragmentMainBinding.inflate(layoutInflater)

    private val missingPermission: MutableList<String> = ArrayList()

    override fun observeLiveData() {
        viewModel.registrationLD.observe(viewLifecycleOwner) { status ->
            notifyStatusChange(status.first, status.second)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListeners()
    }

    private fun setClickListeners() {
        binding?.btnStart?.setOnClickListener {
            checkAndRequestPermissions()
        }
        binding?.btnCamera?.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.startVideoChannelFragment())
        }
    }

    private fun notifyStatusChange(registerStatus: String, productName: String) {
        binding?.tvRegisterStateInfo?.text = registerStatus
        binding?.btnCamera?.isEnabled = registerStatus == "Product Connected"
        binding?.textProductInfo?.text = productName
    }

    private fun checkAndRequestPermissions() {
        for (eachPermission in REQUIRED_PERMISSION_LIST) {
            if (activity?.let {
                    ContextCompat.checkSelfPermission(
                        it,
                        eachPermission
                    )
                } != PackageManager.PERMISSION_GRANTED
            ) {
                missingPermission.add(eachPermission)
            }
        }
        if (missingPermission.isEmpty()) {
            viewModel.startSDKRegistration()
        } else {
            requestPermissionLauncher.launch(missingPermission.toTypedArray())
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted: Map<String, @JvmSuppressWildcards Boolean>? ->
            if (isGranted?.values?.contains(false) == true) {
                viewModel.startSDKRegistration()
            } else {
                showMessage("Missing permissions!!!", true)
            }
        }


    companion object {
        private val REQUIRED_PERMISSION_LIST = arrayOf(
            Manifest.permission.VIBRATE,  // Gimbal rotation
            Manifest.permission.INTERNET,  // API requests
            Manifest.permission.ACCESS_WIFI_STATE,  // WIFI connected products
            Manifest.permission.ACCESS_COARSE_LOCATION,  // Maps
            Manifest.permission.ACCESS_NETWORK_STATE,  // WIFI connected products
            Manifest.permission.ACCESS_FINE_LOCATION,  // Maps
            Manifest.permission.CHANGE_WIFI_STATE,  // Changing between WIFI and USB connection
            // TODO Manifest.permission.WRITE_EXTERNAL_STORAGE, // Log files
            Manifest.permission.BLUETOOTH,  // Bluetooth connected products
            Manifest.permission.BLUETOOTH_ADMIN,  // Bluetooth connected products
            // TODO Manifest.permission.READ_EXTERNAL_STORAGE, // Log files
            Manifest.permission.READ_PHONE_STATE,  // Device UUID accessed upon registration
            Manifest.permission.RECORD_AUDIO // Speaker accessory
        ).apply {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                plus(Manifest.permission.BLUETOOTH_SCAN)
            }
        }
    }
}