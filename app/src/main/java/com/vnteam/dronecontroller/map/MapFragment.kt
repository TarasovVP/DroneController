package com.vnteam.dronecontroller.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.vnteam.dronecontroller.base.BaseFragment
import com.vnteam.dronecontroller.databinding.FragmentMainBinding
import com.vnteam.dronecontroller.databinding.FragmentMapBinding
import com.vnteam.dronecontroller.gimbal.Gimbal
import dji.sdk.keyvalue.key.FlightControllerKey
import dji.sdk.keyvalue.key.GimbalKey
import dji.sdk.keyvalue.key.KeyTools
import dji.sdk.keyvalue.value.common.EmptyMsg
import dji.sdk.keyvalue.value.gimbal.CtrlInfo
import dji.sdk.keyvalue.value.gimbal.GimbalSpeedRotation
import dji.v5.common.callback.CommonCallbacks
import dji.v5.common.error.IDJIError
import dji.v5.manager.KeyManager

class MapFragment : BaseFragment<FragmentMapBinding, MapViewModel>() {

    override val viewModelClass = MapViewModel::class.java
    override fun getViewBinding() = FragmentMapBinding.inflate(layoutInflater)

    private val missingPermission: MutableList<String> = ArrayList()

    override fun observeLiveData() {
        viewModel.registrationLD.observe(viewLifecycleOwner) { status ->

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        KeyManager.getInstance().listen(
            KeyTools.createKey(FlightControllerKey.KeyAreMotorsOn), this
        ) { oldValue, newValue ->
            showMessage("Motors on ${newValue.toString()}")
        }
        /*KeyManager.getInstance()
            .performAction(
                KeyTools.createKey(GimbalKey.KeyRotateBySpeed, 0),
                GimbalSpeedRotation(rangedPitch, yaw, 0.0, CtrlInfo()),
                object : CommonCallbacks.CompletionCallbackWithParam<EmptyMsg> {
                    override fun onSuccess(t: EmptyMsg?) {
                        //showMessage("Gimbal rotate by speed performAction success bottom Click yaw ${gimbal?.yaw} pitch ${gimbal?.pitch}")
                    }

                    override fun onFailure(error: IDJIError) {
                        showMessage("calibrate error ${error.description()}")
                    }


                })*/
        setClickListeners()
    }

    private fun setClickListeners() {
        binding?.btnStart?.setOnClickListener {

        }
    }
}