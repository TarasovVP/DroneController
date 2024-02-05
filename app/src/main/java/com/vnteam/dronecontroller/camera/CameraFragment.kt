package com.vnteam.dronecontroller.camera

import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vnteam.dronecontroller.base.BaseFragment
import com.vnteam.dronecontroller.databinding.FragmentCameraBinding
import com.vnteam.dronecontroller.utils.Extensions.safeSingleObserve
import com.vnteam.dronecontroller.utils.Extensions.showCameraInfo
import dji.v5.common.video.channel.VideoChannelType
import dji.v5.common.video.decoder.DecoderOutputMode
import dji.v5.common.video.decoder.VideoDecoder
import dji.v5.common.video.interfaces.IVideoDecoder

class CameraFragment : BaseFragment<FragmentCameraBinding, CameraViewModel>() {

    override val viewModelClass = CameraViewModel::class.java
    override fun getViewBinding() = FragmentCameraBinding.inflate(layoutInflater)

    private var videoDecoder: IVideoDecoder? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var surfaceControlCallback: SurfaceHolder.Callback? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSurfaceCallback()
        viewModel.initObjectDetector()
        viewModel.initVideoStream()
    }

    private fun setSurfaceCallback() {
        surfaceControlCallback = object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) = Unit

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int,
            ) {
                if (videoDecoder == null ) binding?.surfaceView?.initVideoDecoder(binding?.objectDetection?.isChecked == true)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                viewModel.curVideoChannel.value?.removeStreamDataListener(viewModel.streamDataListener)
                viewModel.yuvDataListener?.let { videoDecoder?.removeYuvDataListener(it) }
                videoDecoder.takeIf { it != null }?.let {
                    it.destroy()
                    videoDecoder = null
                }
            }
        }
        binding?.surfaceView?.holder?.addCallback(surfaceControlCallback)
    }

    override fun observeLiveData() {
        with(viewModel) {
            curVideoChannel.safeSingleObserve(viewLifecycleOwner) {
                it.addStreamDataListener(viewModel.streamDataListener)
                binding?.cameraInfo?.setOnCheckedChangeListener { _, isChecked ->
                    bottomSheetDialog = bottomSheetDialog ?: activity?.showCameraInfo(viewModel.videoChannelInfo.value) {
                        binding?.cameraInfo?.isChecked = binding?.cameraInfo?.isChecked != true
                    }
                    if (isChecked) {
                        bottomSheetDialog?.show()
                    } else {
                        bottomSheetDialog?.dismiss()
                    }
                }
                binding?.objectDetection?.setOnCheckedChangeListener { _, isChecked ->
                    binding?.surfaceView?.initVideoDecoder(isChecked)
                }
            }
            objectDetectionLV.safeSingleObserve(viewLifecycleOwner) { results ->
                binding?.overlay?.setResults(
                    results.results.orEmpty().toMutableList(),
                    results.imageHeight,
                    results.imageWidth
                )
                binding?.overlay?.invalidate()
            }
            biteArrayLV.safeSingleObserve(viewLifecycleOwner) { biteArray ->
                binding?.imageView?.load(biteArray)
            }
        }
    }

    private fun SurfaceView.initVideoDecoder(isYuvFormat: Boolean) {
        videoDecoder?.let {
            videoDecoder?.onPause()
            videoDecoder?.destroy()
            videoDecoder = null
        }
        videoDecoder = VideoDecoder(
            activity,
            viewModel.curVideoChannel.value?.videoChannelType ?: VideoChannelType.PRIMARY_STREAM_CHANNEL,
            if (isYuvFormat) DecoderOutputMode.YUV_MODE else DecoderOutputMode.SURFACE_MODE,
            holder,
            width,
            height
        )
        binding?.imageView?.isVisible = isYuvFormat
        binding?.overlay?.isVisible = isYuvFormat
        if (isYuvFormat) {
            viewModel.initYuvDataListener()?.apply {
                videoDecoder?.addYuvDataListener(this)
            }
        } else {
            viewModel.yuvDataListener?.let {
                videoDecoder?.removeYuvDataListener(it)
                viewModel.removeYuvDataListener()
                binding?.overlay?.clear()

            }
        }
        videoDecoder?.addDecoderStateChangeListener(viewModel.decoderStateChangeListener)
        viewModel.decoderStateChangeListener.onUpdate(videoDecoder?.decoderStatus,videoDecoder?.decoderStatus)
        videoDecoder?.onResume()
    }
}