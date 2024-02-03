package com.vnteam.dronecontroller.camera

import android.graphics.Matrix
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
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
                binding?.surfaceView?.initVideoDecoder(false)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                viewModel.curVideoChannel.value?.removeStreamDataListener(viewModel.streamDataListener)
                viewModel.yuvDataListener?.let { videoDecoder?.removeYuvDataListener(it) }
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
            bitmapLV.safeSingleObserve(viewLifecycleOwner) { bitmap ->
                val holderCanvas = binding?.surfaceView?.holder?.lockCanvas()
                holderCanvas?.let { canvas ->
                    val scaleX = canvas.width.toFloat() / bitmap.width
                    val scaleY = canvas.height.toFloat() / bitmap.height

                    val matrix = Matrix()
                    matrix.setScale(scaleX, scaleY)

                    canvas.drawBitmap(bitmap, matrix, null)
                    binding?.surfaceView?.holder?.unlockCanvasAndPost(canvas)
                }
            }
        }
    }

    private fun SurfaceView.initVideoDecoder(isYuvFormat: Boolean) {
        showMessage("objectDetectionLV videoDecoder $videoDecoder isYuvFormat $isYuvFormat")
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
        if (isYuvFormat) {
            viewModel.initYuvDataListener().apply {
                videoDecoder?.addYuvDataListener(this)
            }
        } else {
            viewModel.yuvDataListener?.let { videoDecoder?.removeYuvDataListener(it) }
            viewModel.removeYuvDataListener()
            binding?.overlay?.clear()
        }
        videoDecoder?.addDecoderStateChangeListener(viewModel.decoderStateChangeListener)
        viewModel.decoderStateChangeListener.onUpdate(videoDecoder?.decoderStatus,videoDecoder?.decoderStatus)
        videoDecoder?.onResume()
        binding?.surfaceView?.holder?.lockCanvas()?.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR)
    }
}