package com.vnteam.dronecontroller.utils

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vnteam.dronecontroller.camera.VideoChannelInfo
import com.vnteam.dronecontroller.databinding.CameraInfoBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object Extensions {

    fun CoroutineScope.launchIO(
        onError: (Throwable, suspend CoroutineScope.() -> Unit) -> Any?,
        block: suspend CoroutineScope.() -> Unit,
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

    const val DEFAULT_STR = "N/A"

    fun Activity?.showCameraInfo(info: VideoChannelInfo?, onCloseClick: () -> Unit): BottomSheetDialog? {
        var bottomSheetDialog: BottomSheetDialog? = null
        this?.apply {
            bottomSheetDialog = BottomSheetDialog(this)
            bottomSheetDialog?.setCancelable(false)
            val binding = CameraInfoBinding.inflate(layoutInflater)
            bottomSheetDialog?.setContentView(binding.root)
            binding.streamSourceValue.text = String.format(
                "%s : %s : %s",
                info?.streamSource?.physicalDeviceCategory,
                info?.streamSource?.physicalDeviceType?.deviceType,
                info?.streamSource?.physicalDevicePosition
            )
            binding.channelTypeValue.text = info?.videoChannelType?.name
            binding.channelStateValue.text = info?.videoChannelState?.name
            binding.decoderStateValue.text = info?.decoderState?.name
            binding.resolutionValue.text = info?.resolution
            binding.formatValue.text = info?.format
            binding.fpsValue.text = info?.fps.toString()
            binding.bitRateValue.text = info?.bitRate.toString()
            binding.closeButton.setOnClickListener {
                onCloseClick.invoke()
            }
        }
        return bottomSheetDialog
    }
}