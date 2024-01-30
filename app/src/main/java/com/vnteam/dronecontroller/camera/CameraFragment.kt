package com.vnteam.dronecontroller.camera

import android.os.Bundle
import android.view.View
import com.vnteam.dronecontroller.base.BaseFragment
import com.vnteam.dronecontroller.databinding.FragmentCameraBinding
import dji.v5.common.video.channel.VideoChannelType
import dji.v5.common.video.decoder.DecoderOutputMode
import dji.v5.common.video.decoder.VideoDecoder
import dji.v5.manager.datacenter.video.VideoStreamManager

class CameraFragment : BaseFragment<FragmentCameraBinding, CameraViewModel>() {

    override val viewModelClass = CameraViewModel::class.java
    override fun getViewBinding() = FragmentCameraBinding.inflate(layoutInflater)
    override fun observeLiveData() = Unit

    private var videoDecoder: VideoDecoder? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVideoDecoder()
        initVideoStream()
    }

    private fun initVideoDecoder() {
        videoDecoder = VideoDecoder(
            this@CameraFragment.context,
            VideoChannelType.PRIMARY_STREAM_CHANNEL,
            DecoderOutputMode.SURFACE_MODE,
            binding?.surfaceView?.holder,
            200,
            200
        )
        videoDecoder?.addDecoderStateChangeListener { decoder, state ->
            showMessage("videoDecoder state: $state")

        }
    }

    private fun initVideoStream() {
        VideoStreamManager.getInstance().addStreamSourcesListener {
            if (it.isNullOrEmpty().not()) {
                val videoChannel = VideoStreamManager.getInstance().getAvailableVideoChannel(VideoChannelType.PRIMARY_STREAM_CHANNEL)
                videoChannel?.addStreamDataListener { streamData ->
                    videoDecoder?.queueInFrame(streamData)
                }

            }
        }
    }
}