package com.vnteam.dronecontroller.main

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.vnteam.dronecontroller.base.BaseViewModel
import dji.v5.common.error.IDJIError
import dji.v5.common.register.DJISDKInitEvent
import dji.v5.manager.SDKManager
import dji.v5.manager.interfaces.SDKManagerCallback

class MainViewModel(private val application: Application) : BaseViewModel(application) {

    val registrationLD: MutableLiveData<Pair<String, String>> = MutableLiveData()

    fun startSDKRegistration() {
        SDKManager.getInstance().init(application, object : SDKManagerCallback {
            override fun onRegisterSuccess() {
                registrationLD.postValue(Pair("Registered", "Undefined"))
            }

            override fun onRegisterFailure(error: IDJIError) {
                exceptionLiveData.postValue("Register sdk fails, please check the bundle id and network connection!")
            }

            override fun onProductDisconnect(productId: Int) {
                registrationLD.postValue(Pair("Product Disconnected", "Undefined"))
            }

            override fun onProductConnect(productId: Int) {
                registrationLD.postValue(Pair("Product Connected", productId.toString()))
            }

            override fun onProductChanged(productId: Int) {
                registrationLD.postValue(Pair("Product Changed", productId.toString()))
            }

            override fun onInitProcess(event: DJISDKInitEvent, totalProcess: Int) {
                registrationLD.postValue(Pair(event.name, "Undefined"))

                if (event == DJISDKInitEvent.INITIALIZE_COMPLETE) {
                    SDKManager.getInstance().registerApp()
                }
            }

            override fun onDatabaseDownloadProgress(current: Long, total: Long) {
                registrationLD.postValue(Pair("Database Download Progress", "Undefined"))
            }
        })
    }
}