package com.vnteam.dronecontroller.main

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.vnteam.dronecontroller.Extensions.productName
import com.vnteam.dronecontroller.base.BaseViewModel
import dji.common.error.DJIError
import dji.common.error.DJISDKError
import dji.sdk.base.BaseComponent
import dji.sdk.base.BaseProduct
import dji.sdk.sdkmanager.DJISDKInitEvent
import dji.sdk.sdkmanager.DJISDKManager
import dji.thirdparty.afinal.core.AsyncTask
import java.util.concurrent.atomic.AtomicBoolean

class MainViewModel(private val application: Application) : BaseViewModel(application) {

    val registrationLD: MutableLiveData<Pair<String, String>> = MutableLiveData()

    private val isRegistrationInProgress = AtomicBoolean(false)
    fun startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute {
                DJISDKManager.getInstance()
                    .registerApp(application, object :
                        DJISDKManager.SDKManagerCallback {
                        override fun onRegister(djiError: DJIError) {
                            if (djiError === DJISDKError.REGISTRATION_SUCCESS) {
                                DJISDKManager.getInstance().startConnectionToProduct()
                                registrationLD.postValue(Pair("Registered", ""))
                            } else {
                                exceptionLiveData.postValue("Register sdk fails, please check the bundle id and network connection!")
                            }
                        }

                        override fun onProductDisconnect() {
                            registrationLD.postValue(Pair("Product Disconnected", "Undefined"))
                        }

                        override fun onProductConnect(baseProduct: BaseProduct?) {
                            registrationLD.postValue(Pair("Product Connected", baseProduct.productName()))
                        }

                        override fun onProductChanged(baseProduct: BaseProduct?) {
                            registrationLD.postValue(Pair("Product Changed", baseProduct.productName()))
                        }

                        override fun onComponentChange(
                            componentKey: BaseProduct.ComponentKey?, oldComponent: BaseComponent?,
                            newComponent: BaseComponent?,
                        ) {
                            newComponent?.setComponentListener {
                                registrationLD.postValue(Pair("Product Changed", "Undefined"))
                            }
                        }

                        override fun onInitProcess(djisdkInitEvent: DJISDKInitEvent?, i: Int) {
                            registrationLD.postValue(Pair("Init Process", "Undefined"))
                        }

                        override fun onDatabaseDownloadProgress(l: Long, l1: Long) {
                            registrationLD.postValue(Pair("Database Download Progress", "Undefined"))
                        }
                    })
            }
        } else {
            exceptionLiveData.postValue("Registration is in progress...")
        }
    }
}