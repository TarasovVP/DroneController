package com.vnteam.dronecontroller.login

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.vnteam.dronecontroller.base.BaseViewModel
import dji.v5.manager.account.UserAccountManager

class LoginViewModel(private val application: Application) : BaseViewModel(application) {

    val resultLiveData: MutableLiveData<String> = MutableLiveData()

    fun loginAccount() {
        exceptionLiveData.postValue("UserAccountManager " + UserAccountManager.getInstance())
        /*UserAccountManager.getInstance().logIntoDJIUserAccount(
            application,
            object : CommonCallbacks.CompletionCallbackWith<UserAccountState> {
                override fun onSuccess(userAccountState: UserAccountState) {
                    resultLiveData.postValue("Logged in " + userAccountState.name)
                }

                override fun onFailure(error: DJIError) {
                    exceptionLiveData.postValue(
                        "Login Error:"
                                + error.description
                    )
                }
            })*/
    }

    fun logoutAccount() {
        /*UserAccountManager.getInstance().logoutOfDJIUserAccount { error: DJIError? ->
            if (null == error) {
                resultLiveData.postValue("Logout ")
            } else {
                exceptionLiveData.postValue(
                    "Logout Error:"
                            + error.description
                )
            }
        }*/
    }

    fun getProductInfo() {
        /*launch {
            val baseProduct = (application as DroneControllerApp).getProductInstance()
            *//*if (baseProduct?.model?.equals(Model.UNKNOWN_AIRCRAFT) != true) {
                VideoFeeder.getInstance().primaryVideoFeed.addVideoDataListener(mReceivedVideoDataListener);
            }*//*
            exceptionLiveData.postValue(baseProduct.toString())
            delay(2000)
            exceptionLiveData.postValue((baseProduct is Aircraft).toString())
            delay(2000)
            val aircraft = baseProduct as? Aircraft
            val camera = aircraft?.camera
            exceptionLiveData.postValue("camera ${camera.toString()}")
            delay(2000)
            val gimbal = aircraft?.gimbal
            exceptionLiveData.postValue("gimbal ${gimbal.toString()}")
            delay(2000)
            val flightController = aircraft?.flightController
            exceptionLiveData.postValue("flightController ${flightController.toString()}")
            delay(2000)
            val baseStation = aircraft?.baseStation
            exceptionLiveData.postValue("baseStation ${baseStation.toString()}")
            delay(2000)
            val remoteController = aircraft?.remoteController
            exceptionLiveData.postValue("remoteController ${remoteController.toString()}")
            delay(2000)
            exceptionLiveData.postValue("isConnected ${remoteController?.isConnected.toString()}")
            delay(2000)
            exceptionLiveData.postValue("isCustomizableButtonSupported ${remoteController?.isCustomizableButtonSupported.toString()}")
            delay(2000)
            exceptionLiveData.postValue("isFocusControllerSupported ${remoteController?.isFocusControllerSupported.toString()}")
            delay(2000)
            exceptionLiveData.postValue("isMasterSlaveModeSupported ${remoteController?.isMasterSlaveModeSupported.toString()}")
            delay(2000)
            exceptionLiveData.postValue("isMultiDevicePairingSupported ${remoteController?.isMultiDevicePairingSupported.toString()}")
            delay(2000)
            exceptionLiveData.postValue("isSecondaryVideoOutputSupported ${remoteController?.isSecondaryVideoOutputSupported.toString()}")
        }
*/
    }

}