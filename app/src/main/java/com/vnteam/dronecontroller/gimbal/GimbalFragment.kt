package com.vnteam.dronecontroller.gimbal

import android.R
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.google.gson.Gson
import com.vnteam.dronecontroller.base.BaseFragment
import com.vnteam.dronecontroller.databinding.FragmentGimbalBinding
import dji.sdk.keyvalue.key.GimbalKey
import dji.sdk.keyvalue.key.KeyTools
import dji.sdk.keyvalue.value.common.EmptyMsg
import dji.sdk.keyvalue.value.gimbal.CtrlInfo
import dji.sdk.keyvalue.value.gimbal.GimbalAttitudeRange
import dji.sdk.keyvalue.value.gimbal.GimbalSpeedRotation
import dji.v5.common.callback.CommonCallbacks.CompletionCallbackWithParam
import dji.v5.common.error.IDJIError
import dji.v5.manager.KeyManager

class GimbalFragment : BaseFragment<FragmentGimbalBinding, GimbalViewModel>() {

    override val viewModelClass = GimbalViewModel::class.java
    override fun observeLiveData() = Unit

    override fun getViewBinding() = FragmentGimbalBinding.inflate(layoutInflater)

    private var gimbal: Gimbal? = null
    private var gimbalAttitudeRange: GimbalAttitudeRange? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.start?.setOnClickListener {
            startGimbal()
            setAttitudeListener()
        }
        binding?.top?.setOnClickListener {
            rotateGimbalBySpeed(10.0, 0.0)
        }
        binding?.bottom?.setOnClickListener {
            rotateGimbalBySpeed(-10.0, 0.0)
        }
        binding?.left?.setOnClickListener {
            rotateGimbalBySpeed(0.0, -10.0)
        }
        binding?.right?.setOnClickListener {
            rotateGimbalBySpeed(0.0, 10.0)
        }
        binding?.stop?.setOnClickListener {
            rotateGimbalBySpeed(0.0, 0.0)
        }
    }

    private fun setAttitudeListener() {
        val itemList = ArrayList<String>()
        val adapter = context?.let { ArrayAdapter(it, R.layout.simple_list_item_1, itemList) }
        binding?.listView?.adapter = adapter
        KeyManager.getInstance().listen(
            KeyTools.createKey(GimbalKey.KeyGimbalAttitude), this
        ) { oldValue, newValue ->
            gimbal = Gson().fromJson(newValue.toString(), Gimbal::class.java)
            itemList.add("Gimbal gimbal: $gimbal")
            adapter?.notifyDataSetChanged()
            binding?.listView?.setSelection(itemList.size - 1)
        }
    }

    private fun rotateGimbalBySpeed(pitch: Double, yaw: Double) {
        val gimbalPitch = gimbal?.pitch ?: 0.0
        var rangedPitch = 0.00
        gimbalAttitudeRange?.pitch?.apply {
            rangedPitch = when {
                pitch + gimbalPitch > max -> max - gimbalPitch - 1
                pitch + gimbalPitch < min -> min - gimbalPitch + 1
                else -> pitch
            }
        }
        showMessage("Gimbal rotate by speed gimbal?.pitch $gimbalPitch rangedPitch $rangedPitch gimbal?.yaw ${gimbal?.yaw}")
        KeyManager.getInstance()
            .performAction(
                KeyTools.createKey(GimbalKey.KeyRotateBySpeed, 0),
                GimbalSpeedRotation(rangedPitch, yaw, 0.0, CtrlInfo()),
                object : CompletionCallbackWithParam<EmptyMsg> {
                    override fun onSuccess(t: EmptyMsg?) {
                        //showMessage("Gimbal rotate by speed performAction success bottom Click yaw ${gimbal?.yaw} pitch ${gimbal?.pitch}")
                    }

                    override fun onFailure(error: IDJIError) {
                        showMessage("calibrate error ${error.description()}")
                    }


                })
        /*KeyManager.getInstance()
            .setValue(KeyTools.createKey(GimbalKey.KeyGimbalCalibrate, 0), EmptyMsg(), object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    showMessage("Gimbal calibrate setValue success")
                }

                override fun onFailure(error: IDJIError) {
                    showMessage("Gimbal calibrate setValue error ${error.description()}")
                }

            })*/
        /*KeyManager.getInstance()
            .performAction(KeyTools.createKey(GimbalKey.KeyGimbalCalibrate, 0), EmptyMsg(), object : CommonCallbacks.CompletionCallbackWithParam<EmptyMsg> {
                override fun onSuccess(t: EmptyMsg?) {
                    showMessage("Gimbal calibrate performAction success EmptyMsg $t")
                }

                override fun onFailure(error: IDJIError) {
                    showMessage("calibrate error ${error.description()}")
                }


            })
*/
    }

    private fun startGimbal() {
        KeyManager.getInstance().listen(
            KeyTools.createKey(GimbalKey.KeyConnection), this
        ) { oldValue, newValue ->
            binding?.title?.text = "Gimbal connection: $newValue"
        }

        /*KeyManager.getInstance().listen(
            KeyTools.createKey(GimbalKey.KeyRotateBySpeed, 0), this
        ) { oldValue, newValue ->
            binding?.subtitle?.text =
                "${binding?.subtitle?.text}\nGimbal rotate by speed: $newValue"
        }*/

        /*KeyManager.getInstance().getValue(
            KeyTools.createKey(GimbalKey.KeyRotateBySpeed, 0),
            object : CompletionCallbackWithParam<GimbalSpeedRotation> {
                override fun onSuccess(p0: GimbalSpeedRotation?) {
                    binding?.subtitle?.text =
                        "${binding?.subtitle?.text}\nGimbal rotate by speed: $p0"
                }

                override fun onFailure(error: IDJIError) {
                    binding?.subtitle?.text =
                        "${binding?.subtitle?.text}\nGimbal rotate by speed error: ${error.description()}"
                }

            })*/

        /*KeyManager.getInstance().getValue(
            KeyTools.createKey(GimbalKey.KeyYawAdjustSupported, 0),
            object : CompletionCallbackWithParam<Boolean> {
                override fun onSuccess(p0: Boolean?) {
                    binding?.subtitle?.text =
                        "Gimbal YawAdjustSupported: $p0"
                }

                override fun onFailure(error: IDJIError) {
                    binding?.subtitle?.text =
                        "${binding?.subtitle?.text}\nGimbal rotate by speed error: ${error.description()}"
                }

            })*/

        /*KeyManager.getInstance().listen(
            KeyTools.createKey(GimbalKey.KeyRotateByAngle), this
        ) { oldValue, newValue ->
            binding?.subtitle?.text =
                "${binding?.subtitle?.text}\nGimbal rotate by angle: $newValue"
        }*/

        KeyManager.getInstance().getValue(
            KeyTools.createKey(GimbalKey.KeyGimbalAttitudeRange, 0),
            object : CompletionCallbackWithParam<GimbalAttitudeRange> {
                override fun onSuccess(range: GimbalAttitudeRange?) {
                    gimbalAttitudeRange = range
                    binding?.subtitle?.text =
                        "GimbalAttitudeRange : $range"
                }

                override fun onFailure(error: IDJIError) {
                    binding?.subtitle?.text =
                        "GimbalAttitudeRange error: ${error.description()}"
                }

            })
    }
}