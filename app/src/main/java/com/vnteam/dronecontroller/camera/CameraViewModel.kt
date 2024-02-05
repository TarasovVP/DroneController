package com.vnteam.dronecontroller.camera

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.YuvImage
import androidx.lifecycle.MutableLiveData
import com.vnteam.dronecontroller.base.BaseViewModel
import com.vnteam.dronecontroller.utils.ObjectDetectorHelper
import dji.sdk.keyvalue.key.CameraKey
import dji.sdk.keyvalue.key.FlightControllerKey
import dji.v5.common.video.channel.VideoChannelState
import dji.v5.common.video.channel.VideoChannelType
import dji.v5.common.video.interfaces.DecoderStateChangeListener
import dji.v5.common.video.interfaces.IVideoChannel
import dji.v5.common.video.interfaces.StreamDataListener
import dji.v5.common.video.interfaces.VideoChannelStateChangeListener
import dji.v5.common.video.interfaces.YuvDataListener
import dji.v5.et.action
import dji.v5.et.cancelListen
import dji.v5.et.create
import dji.v5.et.listen
import dji.v5.manager.datacenter.MediaDataCenter
import java.io.ByteArrayOutputStream

class CameraViewModel(private val application: Application) : BaseViewModel(application) {

    val videoChannelInfo = MutableLiveData<VideoChannelInfo>()
    var curVideoChannel = MutableLiveData<IVideoChannel>()
    private var videoChannelStateListener: VideoChannelStateChangeListener? = null
    private var curChannelType: VideoChannelType? = null
    private var fcHasInit = false
    var objectDetectorHelper: ObjectDetectorHelper? = null
    var objectDetectionLV = MutableLiveData<ObjectDetection>()
    var biteArrayLV = MutableLiveData<ByteArray>()
    var yuvDataListener: YuvDataListener? = null

    fun initVideoStream() {
        MediaDataCenter.getInstance().videoStreamManager.getAvailableVideoChannel(VideoChannelType.PRIMARY_STREAM_CHANNEL)
            ?.let { videoChannel ->
                curVideoChannel.postValue(videoChannel)
                videoChannelInfo.value = VideoChannelInfo(
                    videoChannelState = videoChannel.videoChannelStatus,
                    streamSource = videoChannel.streamSource,
                    videoChannelType = videoChannel.videoChannelType,
                    format = videoChannel.videoStreamFormat.name
                )
                videoChannelStateListener = VideoChannelStateChangeListener { _, to ->
                    if (videoChannelInfo.value == null) {
                        videoChannelInfo.value = VideoChannelInfo(to)
                    } else {
                        videoChannelInfo.value?.videoChannelState = to
                    }
                    videoChannelInfo.value?.format = videoChannel.videoStreamFormat.name
                    if (to == VideoChannelState.ON || to == VideoChannelState.SOCKET_ON) {
                        videoChannelInfo.value?.streamSource = videoChannel.streamSource
                    } else {
                        videoChannelInfo.value?.streamSource = null
                    }
                    refreshVideoChannelInfo()
                }
                initListeners()
            }

        addConnectionListener()

        CameraKey.KeyExitPlayback.create(0).action()
    }

    override fun onCleared() {
        removeListeners()
        removeConnectionListener()
    }

    private fun initListeners() {
        curVideoChannel.value?.addVideoChannelStateChangeListener(videoChannelStateListener)
    }

    private fun removeListeners() {
        curVideoChannel.value?.removeVideoChannelStateChangeListener(videoChannelStateListener)
    }

    private fun addConnectionListener() {
        FlightControllerKey.KeyConnection.create().listen(this) {
            it?.let {
                if (it and fcHasInit) {
                    curChannelType?.let { it1 ->
                        MediaDataCenter.getInstance().videoStreamManager.getAvailableVideoChannel(
                            it1
                        )?.let { iVideoChannel ->
                            curVideoChannel.postValue(iVideoChannel)
                        }
                    }
                    removeListeners()
                    initListeners()
                }
                fcHasInit = true
            }
        }
    }

    private fun removeConnectionListener() {
        FlightControllerKey.KeyConnection.create().cancelListen(this)
    }

    private fun refreshVideoChannelInfo() {
        videoChannelInfo.postValue(videoChannelInfo.value)
    }

    val decoderStateChangeListener =
        DecoderStateChangeListener { _, newState ->
            launch {
                videoChannelInfo.value?.decoderState = newState
                refreshVideoChannelInfo()
            }
        }

    private var videoWidth: Int = -1
    private var videoHeight: Int = -1
    private var widthChanged = false
    private var heightChanged = false
    private var fps: Int = -1

    val streamDataListener =
        StreamDataListener { iVideoFrame ->
            if (fps != iVideoFrame.fps) {
                fps = iVideoFrame.fps
                launch {
                    videoChannelInfo.value?.fps = fps
                    refreshVideoChannelInfo()
                }
            }
            if (videoWidth != iVideoFrame.width) {
                videoWidth = iVideoFrame.width
                widthChanged = true
            }
            if (videoHeight != iVideoFrame.height) {
                videoHeight = iVideoFrame.height
                heightChanged = true
            }
            if (widthChanged || heightChanged) {
                widthChanged = false
                heightChanged = false
                launch {
                    videoChannelInfo.value?.resolution =
                        "${videoWidth}*${videoHeight}"
                    refreshVideoChannelInfo()
                }
            }
        }

    fun initObjectDetector() {
        objectDetectorHelper = ObjectDetectorHelper(
            context = application,
            objectDetectorListener = object : ObjectDetectorHelper.DetectorListener {
                override fun onError(error: String) {
                    exceptionLiveData.postValue(error)
                }

                override fun onResults(objectDetection: ObjectDetection) {
                    objectDetectionLV.postValue(objectDetection)
                }

            })
    }

    var count = 0
    fun initYuvDataListener(): YuvDataListener? {
        yuvDataListener = yuvDataListener ?: YuvDataListener { _, data, width, height ->
            launch {
                if (++count == 10) {
                    count = 0
                    val yuvImage = YuvImage(data, ImageFormat.NV21, width, height, null)
                    val out = ByteArrayOutputStream()
                    yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 100, out)
                    val imageBytes = out.toByteArray()
                    biteArrayLV.postValue(imageBytes)
                    objectDetectorHelper?.detect(imageBytes, 0)
                }
            }
        }
        return yuvDataListener
    }

    fun removeYuvDataListener() {
        yuvDataListener = null
        objectDetectorHelper?.clearObjectDetector()
    }
}
