package com.vnteam.dronecontroller

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    private fun <T> LiveData<T>.safeObserve(owner: LifecycleOwner, observer: (t: T) -> Unit) {
        this.observe(owner) {
            it?.let(observer)
        }
    }

    fun <T> MutableLiveData<T>.safeSingleObserve(owner: LifecycleOwner, observer: (t: T) -> Unit) {
        safeObserve(owner, observer)
        value = null
    }
}