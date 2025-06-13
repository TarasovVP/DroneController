package com.vnteam.dronecontroller.video

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

class VideoChannelVM(channelType: VideoChannelType) : ViewModel() {
    val videoChannelInfo = MutableLiveData<VideoChannelInfo>()
    var videoChannel: IVideoChannel? = null
    var videoChannelStateListener: VideoChannelStateChangeListener? = null
    var curChannelType: VideoChannelType? = null
    var fcHasInit = false
    init {
        MediaDataCenter.getInstance().videoStreamManager.getAvailableVideoChannel(channelType)
            ?.let {
                videoChannel = it
                curChannelType = channelType
                if (videoChannelInfo.value == null) {
                    videoChannelInfo.value = VideoChannelInfo(it.videoChannelStatus)
                }
                videoChannelInfo.value?.streamSource = videoChannel!!.streamSource
                videoChannelInfo.value?.videoChannelType = videoChannel!!.videoChannelType
                videoChannelInfo.value?.format = videoChannel!!.videoStreamFormat.name
                videoChannelStateListener = VideoChannelStateChangeListener { _, to ->

                    if (videoChannelInfo.value == null) {
                        videoChannelInfo.value = VideoChannelInfo(to)
                    } else {
                        videoChannelInfo.value?.videoChannelState = to
                    }
                    videoChannelInfo.value?.format = videoChannel!!.videoStreamFormat.name
                    if (to == VideoChannelState.ON || to == VideoChannelState.SOCKET_ON) {
                        videoChannelInfo.value?.streamSource = videoChannel!!.streamSource
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
        videoChannel?.let {
            it.addVideoChannelStateChangeListener(videoChannelStateListener)
        }
    }

    private fun removeListeners() {
        videoChannel?.let {
            it.removeVideoChannelStateChangeListener(videoChannelStateListener)
        }
    }

    private fun addConnectionListener() {
        FlightControllerKey.KeyConnection.create().listen(this) {
            it?.let {
                if (it and fcHasInit) {
                    curChannelType?.let { it1 ->
                        MediaDataCenter.getInstance().videoStreamManager.getAvailableVideoChannel(
                            it1
                        ).let {
                            videoChannel = it
                        }
                    }
                    removeListeners()
                    initListeners()
                }
                fcHasInit = true
            }
        }
    }

    private fun removeConnectionListener(){
        FlightControllerKey.KeyConnection.create().cancelListen(this)
    }

    fun refreshVideoChannelInfo() {
        videoChannelInfo.postValue(videoChannelInfo.value)
    }
}