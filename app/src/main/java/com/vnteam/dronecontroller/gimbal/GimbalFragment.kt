package com.vnteam.dronecontroller.gimbal

import android.R
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.vnteam.dronecontroller.base.BaseFragment
import com.vnteam.dronecontroller.databinding.FragmentGimbalBinding
import dji.sdk.keyvalue.key.GimbalKey
import dji.sdk.keyvalue.key.KeyTools
import dji.v5.common.callback.CommonCallbacks
import dji.v5.manager.KeyManager

class GimbalFragment : BaseFragment<FragmentGimbalBinding, GimbalViewModel>() {

    override val viewModelClass = GimbalViewModel::class.java
    override fun observeLiveData() = Unit

    override fun getViewBinding() = FragmentGimbalBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.start?.setOnClickListener {
            startGimbal()
            setAttitudeListener()
        }
    }

    private fun setAttitudeListener() {
        val itemList = ArrayList<String>()
        val adapter = context?.let { ArrayAdapter(it, R.layout.simple_list_item_1, itemList) }
        binding?.listView?.adapter = adapter
        KeyManager.getInstance().listen(KeyTools.createKey(GimbalKey.KeyGimbalAttitude), this
        ) { oldValue, newValue ->
            itemList.add("Gimbal attitude: $newValue")
            adapter?.notifyDataSetChanged()
            binding?.listView?.setSelection(itemList.size - 1)
        }
    }

    private fun startGimbal() {
        KeyManager.getInstance().listen(KeyTools.createKey(GimbalKey.KeyConnection), this
        ) { oldValue, newValue ->
            binding?.title?.text = "Gimbal connection: $newValue"
        }

        KeyManager.getInstance().listen(KeyTools.createKey(GimbalKey.KeyRotateBySpeed), this
        ) { oldValue, newValue ->
            binding?.subtitle?.text = "${binding?.subtitle?.text}\nGimbal rotate by speed: $newValue"
        }

        KeyManager.getInstance().listen(KeyTools.createKey(GimbalKey.KeyRotateByAngle), this
        ) { oldValue, newValue ->
            binding?.subtitle?.text = "${binding?.subtitle?.text}\nGimbal rotate by angle: $newValue"
        }
    }
}