package com.vnteam.dronecontroller.camera

import android.os.Bundle
import android.view.SurfaceHolder
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vnteam.dronecontroller.utils.Extensions.safeSingleObserve
import com.vnteam.dronecontroller.base.BaseFragment
import com.vnteam.dronecontroller.databinding.CameraInfoBinding
import com.vnteam.dronecontroller.databinding.FragmentCameraBinding
import dji.v5.common.video.decoder.DecoderOutputMode
import dji.v5.common.video.decoder.VideoDecoder
import dji.v5.common.video.interfaces.IVideoDecoder

class CameraFragment : BaseFragment<FragmentCameraBinding, CameraViewModel>() {

    override val viewModelClass = CameraViewModel::class.java
    override fun getViewBinding() = FragmentCameraBinding.inflate(layoutInflater)

    private var videoDecoder: IVideoDecoder? = null
    private var bottomSheetDialog: BottomSheetDialog? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSurfaceCallback()
        viewModel.initObjectDetector()
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
                    viewModel.curVideoChannel.let {
                        videoDecoder = VideoDecoder(
                            activity,
                            it.value?.videoChannelType,
                            DecoderOutputMode.SURFACE_MODE,
                            holder,
                            width,
                            height
                        )
                        videoDecoder?.addDecoderStateChangeListener(viewModel.decoderStateChangeListener)
                        videoDecoder?.addYuvDataListener(viewModel.yuvDataListener)
                        viewModel.decoderStateChangeListener.onUpdate(
                            videoDecoder?.decoderStatus,
                            videoDecoder?.decoderStatus
                        )
                    }
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                viewModel.curVideoChannel.value?.removeStreamDataListener(viewModel.streamDataListener)
                videoDecoder?.removeYuvDataListener(viewModel.yuvDataListener)
                videoDecoder.takeIf { it != null }?.let {
                    it.destroy()
                    videoDecoder = null
                }
            }
        })
    }

    override fun observeLiveData() {
        with(viewModel) {
            curVideoChannel.safeSingleObserve(viewLifecycleOwner) {
                it.addStreamDataListener(viewModel.streamDataListener)
                binding?.infoButton?.setOnClickListener {
                    showCameraInfo()
                }
                objectDetectionLV.safeSingleObserve(viewLifecycleOwner) { results ->
                    binding?.overlay?.setResults(
                        results.results.orEmpty().toMutableList(),
                        results.imageHeight,
                        results.imageWidth
                    )
                    binding?.overlay?.invalidate()
                }
            }
        }
    }

    private fun showCameraInfo() {
        if (bottomSheetDialog?.isShowing == true) {
            return
        }
        bottomSheetDialog = activity?.let { it1 -> BottomSheetDialog(it1) }
        val binding = CameraInfoBinding.inflate(layoutInflater)
        bottomSheetDialog?.setContentView(binding.root)
        viewModel.videoChannelInfo.value?.let { info ->
            binding.streamSourceValue.text = String.format(
                "%s : %s : %s",
                info.streamSource?.physicalDeviceCategory,
                info.streamSource?.physicalDeviceType?.deviceType,
                info.streamSource?.physicalDevicePosition
            )
            binding.channelTypeValue.text = info.videoChannelType?.name
            binding.channelStateValue.text = info.videoChannelState.name
            binding.decoderStateValue.text = info.decoderState.name
            binding.resolutionValue.text = info.resolution
            binding.formatValue.text = info.format
            binding.fpsValue.text = info.fps.toString()
            binding.bitRateValue.text = info.bitRate.toString()
        }
        binding.closeButton.setOnClickListener {
            bottomSheetDialog?.dismiss()
        }
        bottomSheetDialog?.show()
    }
}