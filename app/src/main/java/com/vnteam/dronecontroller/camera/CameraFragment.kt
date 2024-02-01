package com.vnteam.dronecontroller.camera

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vnteam.dronecontroller.base.BaseFragment
import com.vnteam.dronecontroller.databinding.CameraInfoBinding
import com.vnteam.dronecontroller.databinding.FragmentCameraBinding
import dji.v5.common.video.decoder.DecoderOutputMode
import dji.v5.common.video.decoder.VideoDecoder
import dji.v5.common.video.interfaces.DecoderStateChangeListener
import dji.v5.common.video.interfaces.IVideoDecoder
import dji.v5.common.video.interfaces.StreamDataListener

class CameraFragment : BaseFragment<FragmentCameraBinding, CameraViewModel>() {

    override val viewModelClass = CameraViewModel::class.java
    override fun getViewBinding() = FragmentCameraBinding.inflate(layoutInflater)
    override fun observeLiveData() = Unit

    protected var mainHandler = Handler(Looper.getMainLooper())

    private var videoDecoder: IVideoDecoder? = null

    private var videoWidth: Int = -1
    private var videoHeight: Int = -1
    private var widthChanged = false
    private var heightChanged = false
    private var fps: Int = -1

    private val streamDataListener =
        StreamDataListener {
            it.let {
                if (fps != it.fps) {
                    fps = it.fps
                    mainHandler.post {
                        viewModel.videoChannelInfo.value?.fps = fps
                        viewModel.refreshVideoChannelInfo()
                    }
                }
                if (videoWidth != it.width) {
                    videoWidth = it.width
                    widthChanged = true
                }
                if (videoHeight != it.height) {
                    videoHeight = it.height
                    heightChanged = true
                }
                if (widthChanged || heightChanged) {
                    widthChanged = false
                    heightChanged = false
                    mainHandler.post {
                        viewModel.videoChannelInfo.value?.resolution =
                            "${videoWidth}*${videoHeight}"
                        viewModel.refreshVideoChannelInfo()
                    }
                }
            }
        }

    private val decoderStateChangeListener =
        DecoderStateChangeListener { _, newState ->
            mainHandler.post {
                viewModel.videoChannelInfo.value?.decoderState = newState
                viewModel.refreshVideoChannelInfo()
            }

        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.infoButton?.setOnClickListener {
            showCameraInfo()
        }
        setSurfaceCallback()
        viewModel.initVideoStream()
    }

    private fun setSurfaceCallback() {
        binding?.surfaceView?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) = Unit

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int,
            ) {
                if (videoDecoder == null) {
                    viewModel.curVideoChannel?.let {
                        videoDecoder = VideoDecoder(
                            activity,
                            it.videoChannelType,
                            DecoderOutputMode.SURFACE_MODE,
                            holder,
                            width,
                            height
                        )
                        videoDecoder?.addDecoderStateChangeListener(decoderStateChangeListener)
                        decoderStateChangeListener.onUpdate(
                            videoDecoder?.decoderStatus,
                            videoDecoder?.decoderStatus
                        )
                    }
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                viewModel.curVideoChannel?.removeStreamDataListener(streamDataListener)
                videoDecoder.takeIf { it != null }?.let {
                    it.destroy()
                    videoDecoder = null
                }
            }
        })
    }

    private fun showCameraInfo() {
        binding?.let {
            val bottomSheetDialog = activity?.let { it1 -> BottomSheetDialog(it1) }
            val binding = CameraInfoBinding.inflate(layoutInflater)
            bottomSheetDialog?.setContentView(binding.root)
            viewModel.videoChannelInfo.value?.let {
                binding.streamSourceValue.text =
                    "[${it.streamSource?.physicalDeviceCategory} : ${it.streamSource?.physicalDeviceType?.deviceType} : ${it.streamSource?.physicalDevicePosition}]"
                binding.channelTypeValue.text = it.videoChannelType?.name
                binding.decoderStateValue.text = it.decoderState.name
                binding.resolutionValue.text = it.resolution
                binding.formatValue.text = it.format
                binding.fpsValue.text = it.fps.toString()
                binding.bitRateValue.text = it.bitRate.toString()
            }
            bottomSheetDialog?.show()
        }
    }
}