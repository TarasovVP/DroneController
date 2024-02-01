package com.vnteam.dronecontroller.camera

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.vnteam.dronecontroller.base.BaseViewModel
import dji.sdk.keyvalue.key.CameraKey
import dji.sdk.keyvalue.key.FlightControllerKey
import dji.v5.common.video.channel.VideoChannelState
import dji.v5.common.video.channel.VideoChannelType
import dji.v5.common.video.interfaces.IVideoChannel
import dji.v5.common.video.interfaces.VideoChannelStateChangeListener
import dji.v5.et.action
import dji.v5.et.cancelListen
import dji.v5.et.create
import dji.v5.et.listen
import dji.v5.manager.datacenter.MediaDataCenter

class CameraViewModel(application: Application) : BaseViewModel(application) {

    val videoChannelInfo = MutableLiveData<VideoChannelInfo>()
    var curVideoChannel: IVideoChannel? = null
    var videoChannelStateListener: VideoChannelStateChangeListener? = null
    var curChannelType: VideoChannelType? = null
    var fcHasInit = false

    fun initVideoStream() {
        MediaDataCenter.getInstance().videoStreamManager.getAvailableVideoChannel(VideoChannelType.PRIMARY_STREAM_CHANNEL)
            ?.let { videoChannel ->
                curVideoChannel = videoChannel
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
        curVideoChannel?.addVideoChannelStateChangeListener(videoChannelStateListener)
    }

    private fun removeListeners() {
        curVideoChannel?.removeVideoChannelStateChangeListener(videoChannelStateListener)
    }

    private fun addConnectionListener() {
        FlightControllerKey.KeyConnection.create().listen(this) {
            it?.let {
                if (it and fcHasInit) {
                    curChannelType?.let { it1 ->
                        MediaDataCenter.getInstance().videoStreamManager.getAvailableVideoChannel(
                            it1
                        ).let {
                            curVideoChannel = it
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

    fun refreshVideoChannelInfo() {
        videoChannelInfo.postValue(videoChannelInfo.value)
    }

}