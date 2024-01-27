package com.vnteam.dronecontroller.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vnteam.dronecontroller.Extensions.launchIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    val exceptionLiveData = MutableLiveData<String>()
    val isProgressProcessLiveData = MutableLiveData<Boolean>()

    fun showProgress() {
        isProgressProcessLiveData.postValue(true)
    }

    fun hideProgress() {
        isProgressProcessLiveData.postValue(false)
    }

    protected fun launch(
        onError: (Throwable, suspend CoroutineScope.() -> Unit) -> Any? = ::onError,
        block: suspend CoroutineScope.() -> Unit,
    ): Job = viewModelScope.launchIO(onError, block)

    protected open fun onError(throwable: Throwable, block: suspend CoroutineScope.() -> Unit) {
        hideProgress()
        throwable.printStackTrace()
        exceptionLiveData.postValue(throwable.localizedMessage)
    }
}