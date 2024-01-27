package com.vnteam.dronecontroller

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vnteam.dronecontroller.main.MainFragment
import dji.sdk.base.BaseProduct
import dji.sdk.products.Aircraft
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object Extensions {

    fun CoroutineScope.launchIO(
        onError: (Throwable, suspend CoroutineScope.() -> Unit) -> Any?,
        block: suspend CoroutineScope.() -> Unit
    ): Job =
        launch(CoroutineExceptionHandler { _, exception ->
            onError(exception, block)
        }) {
            withContext(Dispatchers.IO) {
                block()
            }
        }

    fun <T> LiveData<T>.safeObserve(owner: LifecycleOwner, observer: (t: T) -> Unit) {
        this.observe(owner) {
            it?.let(observer)
        }
    }

    fun <T> MutableLiveData<T>.safeSingleObserve(owner: LifecycleOwner, observer: (t: T) -> Unit) {
        safeObserve(owner, observer)
        value = null
    }

    fun BaseProduct?.productName(): String {
        var productName = "Undefined"
            if (this?.isConnected == true) {
                if (null != this.model) {
                    productName = "" + this.model?.displayName
                }
            } else if (this is Aircraft) {
                val aircraft = this as Aircraft?
                if (aircraft?.remoteController != null && aircraft.remoteController.isConnected) {
                    productName = "" + aircraft.model?.displayName
                }
            }
        return productName
    }
}