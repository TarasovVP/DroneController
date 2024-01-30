package com.vnteam.dronecontroller.bluetooth

import android.app.Service
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import com.vnteam.dronecontroller.R
import com.vnteam.dronecontroller.base.BaseFragment
import com.vnteam.dronecontroller.databinding.FragmentBluetoothBinding


class BluetoothFragment : BaseFragment<FragmentBluetoothBinding, BluetoothViewModel>() {

    override val viewModelClass = BluetoothViewModel::class.java
    override fun getViewBinding() = FragmentBluetoothBinding.inflate(layoutInflater)
    override fun observeLiveData() = Unit

  /*  private val strDevicesList: MutableList<String> = ArrayList()
    private var adapter: ArrayAdapter<String>? = null
    private var connector: BluetoothProductConnector? = null
    private var devicesList: List<BluetoothDevice>? = null
    private val bluetoothProductCallback =
        BluetoothDevicesListCallback { list ->
            if (devicesList == null) {
                devicesList = list
                updateTextTV(list)
                updateList(devicesList)
            } else if (!compareDevice(devicesList.orEmpty(), list)) {
                devicesList = list
                updateTextTV(list)
                updateList(devicesList)
            }
        }

    override fun onStop() {
        super.onStop()
        connector?.setBluetoothDevicesListCallback(null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        binding?.btnSearchBluetooth?.setOnClickListener {
            connector?.searchBluetoothProducts { error ->
                if (error != null) {
                    showMessage(error.description)
                } else {
                    showMessage("Searching...")
                }
            }
        }
        binding?.btnConnect?.setOnClickListener {
            val chosen = binding?.spinConnect?.selectedItemPosition
            val runSetDevice = Runnable {
                if (chosen != -1 && chosen != 0) {
                    if (devicesList != null) {
                        connector?.connect(
                            devicesList?.get((chosen ?: 0) - 1)
                        ) { error ->
                            if (error == null) {
                                showMessage("Connected")
                            } else {
                                showMessage(error.description)
                            }
                        }
                    } else {
                        showMessage("device list has expired")
                    }
                }
            }
            if (devicesList != null) {
                if (devicesList?.isNotEmpty() == true) {
                    runSetDevice.run()
                } else {
                    showMessage("devices list is empty.")
                }
            } else {
                showMessage("devices list is null")
            }
        }
        binding?.btnDisconnect?.setOnClickListener {
            connector?.disconnect { error ->
                if (error == null) {
                    showMessage("Disconnected")
                } else {
                    showMessage(error.description)
                }
            }
        }
    }

    private fun initUI() {
        connector = DJISDKManager.getInstance().bluetoothProductConnector
        if (connector == null) {
            showMessage("connect is null!")
            return
        } else {
            connector?.setBluetoothDevicesListCallback(bluetoothProductCallback)
        }
        adapter = context?.let { ArrayAdapter<String>(it, android.R.layout.simple_list_item_1, strDevicesList) }
        binding?.spinConnect?.adapter = adapter
        adapter?.notifyDataSetChanged()
    }

    private fun addLineToSB(sb: StringBuilder, name: String, value: Any?) {
        sb.append("$name: ").append(if (value == null) "" else value.toString() + "").append("\n")
    }

    private fun updateTextTV(devices: List<BluetoothDevice>?) {
        if (devices == null) {
            return
        }
        val sb = StringBuilder()
        addLineToSB(sb, "Devices", null)
        for (i in devices.indices) {
            addLineToSB(sb, "Device Name", devices[i].name)
            addLineToSB(sb, "Address", devices[i].address)
            addLineToSB(sb, "Status", devices[i].status)
            addLineToSB(sb, "RSSI", devices[i].rssi)
        }
        sb.append("\n")
        activity?.runOnUiThread {
            binding?.textDevicesInformation?.text = sb.toString()
        }
    }

    private fun updateList(devices: List<BluetoothDevice>?) {
        if (devices == null) {
            return
        }
        strDevicesList.clear()
        strDevicesList.add("Select Devices")
        for (i in devices.indices) {
            strDevicesList.add(devices[i].name)
        }
        binding?.root?.post {
            adapter?.notifyDataSetChanged()
            binding?.spinConnect?.setSelection(0)
        }
    }

    private fun compareDevice(ar1: List<BluetoothDevice>, ar2: List<BluetoothDevice>): Boolean {
        if (ar1.size != ar2.size) {
            return false
        }
        for (i in ar1.indices) {
            if (ar1[i] != ar2[i]) {
                return false
            }
        }
        return true
    }*/
}