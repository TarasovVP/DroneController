package com.vnteam.dronecontroller.video

import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.*
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.vnteam.dronecontroller.utils.Extensions.DEFAULT_STR
import com.vnteam.dronecontroller.R
import com.vnteam.dronecontroller.base.BaseBindingFragment
import com.vnteam.dronecontroller.databinding.VideoChannelPageBinding
import dji.v5.common.callback.CommonCallbacks
import dji.v5.common.error.IDJIError
import dji.v5.common.video.channel.VideoChannelState
import dji.v5.common.video.channel.VideoChannelType
import dji.v5.common.video.decoder.*
import dji.v5.common.video.interfaces.*
import dji.v5.common.video.stream.StreamSource
import dji.v5.utils.common.*
import java.io.*

class VideoChannelFragment : BaseBindingFragment<VideoChannelPageBinding>(), SurfaceHolder.Callback,
    YuvDataListener {

    override fun getViewBinding() = VideoChannelPageBinding.inflate(layoutInflater)

    protected var mainHandler = Handler(Looper.getMainLooper())

    private val TAG = LogUtils.getTag("VideoChannelFragment")
    private val PATH: String = "/DJI_ScreenShot"
    private val multiVideoChannelVM: MultiVideoChannelVM by activityViewModels()
    private lateinit var channelVM: VideoChannelVM
    private lateinit var surfaceView: SurfaceView
    private lateinit var dialog: AlertDialog
    private var streamSources: List<StreamSource>? = null
    private var videoDecoder: IVideoDecoder? = null

    private var videoWidth: Int = -1
    private var videoHeight: Int = -1
    private var widthChanged = false
    private var heightChanged = false
    private var fps: Int = -1

    private var checkedItem: Int = -1
    private var count: Int = 0
    private var stringBuilder: StringBuilder? = StringBuilder()
    private val DISPLAY = 100
    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                DISPLAY -> {
                    display(msg.obj.toString())
                }
            }
        }
    }

    private val streamDataListener =
        StreamDataListener {
            it.let {
                if (fps != it.fps) {
                    fps = it.fps
                    mainHandler.post {
                        channelVM.videoChannelInfo.value?.fps = fps
                        channelVM.refreshVideoChannelInfo()
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
                        channelVM.videoChannelInfo.value?.resolution =
                            "${videoWidth}*${videoHeight}"
                        channelVM.refreshVideoChannelInfo()
                    }
                }
            }
        }

    private val decoderStateChangeListener =
        DecoderStateChangeListener { _, newState ->
            mainHandler.post {
                channelVM.videoChannelInfo.value?.decoderState = newState
                channelVM.refreshVideoChannelInfo()
            }

        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        surfaceView = view.findViewById(R.id.surface_view)
        surfaceView.holder.addCallback(this)
        setClickListeners()
        val videoChannelType: VideoChannelType = when (tag) {
            VideoChannelType.SECONDARY_STREAM_CHANNEL.name -> VideoChannelType.SECONDARY_STREAM_CHANNEL
            VideoChannelType.EXTENDED_STREAM_CHANNEL.name -> VideoChannelType.EXTENDED_STREAM_CHANNEL
            else -> VideoChannelType.PRIMARY_STREAM_CHANNEL
        }
        val factory = VideoChannelVMFactory(videoChannelType)
        channelVM = ViewModelProvider(this, factory).get(VideoChannelVM::class.java)
        init()
    }

    private fun setClickListeners() {
        binding?.horizontalScrollView?.startChannel?.setOnClickListener {
            channelVM.videoChannel?.let {
                if (channelVM.videoChannel?.videoChannelStatus == VideoChannelState.CLOSE) {
                    showStartChannelDialog()
                }
            } ?: showDisconnectToast()
        }
        binding?.horizontalScrollView?.closeChannel?.setOnClickListener {
            channelVM.videoChannel?.let {
                channelVM.videoChannel!!.closeChannel(object :
                    CommonCallbacks.CompletionCallback {
                    /**
                     * Invoked when the asynchronous operation completes successfully. Override to
                     * handheld in your own code.
                     */
                    override fun onSuccess() {
                        mainHandler.post {
                            channelVM.videoChannelInfo.value?.videoChannelState =
                                VideoChannelState.CLOSE
                            channelVM.videoChannelInfo.value?.socket = DEFAULT_STR
                            channelVM.refreshVideoChannelInfo()
                        }
                        showMessage("Close Channel Success")

                    }

                    /**
                     * Invoked when the asynchronous operation completes. If the operation  completes
                     * successfully, `error` will be `null`. Override to handheld in your own code.
                     *
                     * @param error The DJI error result
                     */
                    override fun onFailure(error: IDJIError) {
                        showMessage(
                            "Close Channel Failed: (errorCode ${error.errorCode()}, description: ${error.description()})"
                        )

                    }
                })
            } ?: showDisconnectToast()
        }
        binding?.horizontalScrollView?.yuvScreenShot?.setOnClickListener {
            channelVM.videoChannel?.let {
                handlerYUV()
            } ?: showDisconnectToast()
        }
        binding?.horizontalScrollView?.startSocket?.setOnClickListener {

        }
        binding?.horizontalScrollView?.closeSocket?.setOnClickListener {

        }
        binding?.horizontalScrollView?.startBroadcast?.setOnClickListener {

        }
        binding?.horizontalScrollView?.stopBroadcast?.setOnClickListener {

        }
    }

    private fun init() {
        multiVideoChannelVM.videoStreamSources.observe(viewLifecycleOwner) {
            streamSources = it
        }
        initVideoChannelInfo()
    }

    private fun initVideoChannelInfo() {
        streamSources = multiVideoChannelVM.videoStreamSources.value
        channelVM.videoChannelInfo.observe(viewLifecycleOwner) {
            it?.let {
                val videoStreamInfo =
                    "\n StreamSource: [${it.streamSource?.physicalDeviceCategory} : ${it.streamSource?.physicalDeviceType?.deviceType} : ${it.streamSource?.physicalDevicePosition}] \n " +
                            "ChannelType: [${it.videoChannelType.name}] State: [${it.videoChannelState}] \n " +
                            "DecoderState: [${it.decoderState}] Resolution: [${it.resolution}] \n " +
                            "FPS: [${it.fps}] Format: [${it.format}] BitRate: [${it.bitRate} Kb/s] \n " +
                            "Socket: [${it.socket}]"
                binding?.videoStreamInfo?.text = videoStreamInfo
            }
        }
        channelVM.videoChannel?.addStreamDataListener(streamDataListener) ?: showDisconnectToast()
        this@VideoChannelFragment.setFragmentResultListener("ResetAllVideoChannel") { requestKey, _ ->
            if ("ResetAllVideoChannel" == requestKey) {
                mainHandler.post {
                    channelVM.videoChannelInfo.value?.streamSource = null
                    channelVM.videoChannelInfo.value?.videoChannelState = VideoChannelState.CLOSE
                    channelVM.refreshVideoChannelInfo()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        channelVM.videoChannel?.removeStreamDataListener(streamDataListener)
        if (videoDecoder != null) {
            videoDecoder?.destroy()
            videoDecoder = null
        }
    }

    private fun showStartChannelDialog() {
        val length = streamSources?.size
        if (length == 0) {
            return
        }
        val items = length?.let { arrayOfNulls<String>(it) }
        streamSources?.let {
            for (i in 0 until length!!) {
                items?.set(i, streamSources!![i].toString())
            }
            if (!items.isNullOrEmpty()) {
                dialog = this@VideoChannelFragment.requireContext().let { context ->
                    AlertDialog.Builder(context, dji.v5.core.R.style.AlertDialog_AppCompat)
                        .setIcon(android.R.drawable.ic_menu_camera)
                        .setTitle(R.string.select_stream_source)
                        .setCancelable(false)
                        .setSingleChoiceItems(
                            items, checkedItem
                        ) { _, i ->
                            checkedItem = i
                            showMessage(
                                "选择的码流源为： " + (items[i] ?: "空"),
                            )
                        }.setPositiveButton(R.string.confirm) { dialog, _ ->
                            run {
                                val streamSource = streamSources?.getOrNull(checkedItem)
                                streamSource?.let { startChannel(it) }
                                dialog.dismiss()
                            }
                        }
                        .setNegativeButton(R.string.cancel) { dialog, _ ->
                            run {
                                dialog.dismiss()
                            }
                        }
                        .create()
                }
                dialog.show()
            }
        }
    }


    private fun startChannel(streamSource: StreamSource) {
        channelVM.videoChannel?.let {
            channelVM.videoChannel!!.startChannel(
                streamSource,
                object : CommonCallbacks.CompletionCallback {
                    /**
                     * Invoked when the asynchronous operation completes successfully. Override to
                     * handheld in your own code.
                     */
                    override fun onSuccess() {
                        mainHandler.post {
                            channelVM.videoChannelInfo.value?.streamSource =
                                streamSource
                            channelVM.refreshVideoChannelInfo()
                        }

                        showMessage("Start Channel Success")

                    }

                    /**
                     * Invoked when the asynchronous operation completes. If the operation  completes
                     * successfully, `error` will be `null`. Override to handheld in your own code.
                     *
                     * @param error The DJI error result
                     */
                    override fun onFailure(error: IDJIError) {
                        showMessage("Start Channel Failed: (errorCode ${error.errorCode()}, description: ${error.description()})")
                    }
                })
        } ?: showDisconnectToast()
    }

    override fun surfaceCreated(holder: SurfaceHolder) = Unit

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (videoDecoder == null) {
            channelVM.videoChannel?.let {
                videoDecoder = VideoDecoder(
                    this@VideoChannelFragment.context,
                    channelVM.videoChannel?.videoChannelType,
                    DecoderOutputMode.SURFACE_MODE,
                    surfaceView.holder,
                    width,
                    height
                )
                videoDecoder?.addDecoderStateChangeListener(decoderStateChangeListener)
                decoderStateChangeListener.onUpdate(videoDecoder?.decoderStatus,videoDecoder?.decoderStatus)
            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        videoDecoder?.destroy()
    }

    override fun onReceive(mediaFormat: MediaFormat?, data: ByteArray?, width: Int, height: Int) {
        if (++count == 30) {
            count = 0
            data?.let {
                AsyncTask.execute {
                    var path: String =
                        DiskUtil.getExternalCacheDirPath(ContextUtil.getContext(), PATH)
                    val dir = File(path)
                    if (!dir.exists() || !dir.isDirectory) {
                        dir.mkdirs()
                    }
                    path = path + "/YUV_" + System.currentTimeMillis() + ".yuv"
                    var fos: FileOutputStream? = null
                    try {
                        val file = File(path)
                        if (file.exists()) {
                            file.delete()
                        }
                        fos = FileOutputStream(file)
                        fos.write(it, 0, it.size)
                    } catch (e: Exception) {
                        LogUtils.e(TAG, e.message)
                    } finally {
                        fos?.let {
                            fos.flush()
                            fos.close()
                        }
                    }
                    saveYuvData(mediaFormat, data, width, height)
                }
            }
        }
    }

    private fun saveYuvData(mediaFormat: MediaFormat?, data: ByteArray?, width: Int, height: Int) {
        data?.let {
            mediaFormat?.let {
                when (it.getInteger(MediaFormat.KEY_COLOR_FORMAT)) {
                    0, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar -> {
                        newSaveYuvDataToJPEG(data, width, height)
                    }

                    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar -> {
                        newSaveYuvDataToJPEG420P(data, width, height)
                    }
                }
            }
        }
    }

    private fun handlerYUV() {
        if (binding?.horizontalScrollView?.yuvScreenShot?.isSelected != true) {
            binding?.horizontalScrollView?.yuvScreenShot?.setText(R.string.btn_resume_video)
            binding?.horizontalScrollView?.yuvScreenShot?.isSelected = true
            binding?.yuvScreenSavePath?.text = ""
            binding?.yuvScreenSavePath?.visibility = View.VISIBLE
            videoDecoder?.let {
                videoDecoder?.onPause()
                videoDecoder?.destroy()
                videoDecoder = null
            }
            videoDecoder = VideoDecoder(
                this@VideoChannelFragment.context,
                channelVM.videoChannel!!.videoChannelType
            )
            videoDecoder?.addDecoderStateChangeListener(decoderStateChangeListener)
            decoderStateChangeListener.onUpdate(videoDecoder?.decoderStatus,videoDecoder?.decoderStatus)
            videoDecoder?.addYuvDataListener(this)
            videoDecoder?.onResume()
        } else {
            stringBuilder?.let {
                stringBuilder?.clear()
            }

            binding?.horizontalScrollView?.yuvScreenShot?.setText(R.string.btn_yuv_screen_shot)
            binding?.horizontalScrollView?.yuvScreenShot?.isSelected = false
            binding?.yuvScreenSavePath?.text = ""
            binding?.yuvScreenSavePath?.visibility = View.INVISIBLE
            videoDecoder?.let {
                videoDecoder?.onPause()
                videoDecoder?.destroy()
                videoDecoder = null
            }
            videoDecoder = VideoDecoder(
                this@VideoChannelFragment.context,
                channelVM.videoChannel?.videoChannelType,
                DecoderOutputMode.SURFACE_MODE,
                surfaceView.holder,
                surfaceView.width,
                surfaceView.height
            )
            videoDecoder?.addDecoderStateChangeListener(decoderStateChangeListener)
            decoderStateChangeListener.onUpdate(videoDecoder?.decoderStatus,videoDecoder?.decoderStatus)
            videoDecoder?.removeYuvDataListener(this)
            videoDecoder?.onResume()

        }
    }

    private fun newSaveYuvDataToJPEG420P(yuvFrame: ByteArray, width: Int, height: Int) {
        if (yuvFrame.size < width * height) {
            return
        }
        val length = width * height
        val u = ByteArray(width * height / 4)
        val v = ByteArray(width * height / 4)
        for (i in u.indices) {
            u[i] = yuvFrame[length + i]
            v[i] = yuvFrame[length + u.size + i]
        }
        for (i in u.indices) {
            yuvFrame[length + 2 * i] = v[i]
            yuvFrame[length + 2 * i + 1] = u[i]
        }
        screenShot(
            yuvFrame,
            DiskUtil.getExternalCacheDirPath(ContextUtil.getContext(), PATH),
            width,
            height
        )
    }

    private fun newSaveYuvDataToJPEG(yuvFrame: ByteArray, width: Int, height: Int) {
        if (yuvFrame.size < width * height) {
            return
        }
        val length = width * height
        val u = ByteArray(width * height / 4)
        val v = ByteArray(width * height / 4)
        for (i in u.indices) {
            v[i] = yuvFrame[length + 2 * i]
            u[i] = yuvFrame[length + 2 * i + 1]
        }
        for (i in u.indices) {
            yuvFrame[length + 2 * i] = u[i]
            yuvFrame[length + 2 * i + 1] = v[i]
        }
        screenShot(
            yuvFrame,
            DiskUtil.getExternalCacheDirPath(ContextUtil.getContext(), PATH),
            width,
            height
        )
    }

    private fun screenShot(buf: ByteArray, shotDir: String, width: Int, height: Int) {
        val dir = File(shotDir)
        if (!dir.exists() || !dir.isDirectory) {
            dir.mkdirs()
        }
        val yuvImage = YuvImage(
            buf,
            ImageFormat.NV21,
            width,
            height,
            null
        )
        val outputFile: OutputStream
        val path = dir.toString() + "/ScreenShot_" + System.currentTimeMillis() + ".jpeg"
        outputFile = try {
            FileOutputStream(File(path))
        } catch (e: FileNotFoundException) {
            showMessage("screenShot: new bitmap output file error: $e")
            return
        }
        yuvImage.compressToJpeg(
            Rect(
                0,
                0,
                width,
                height
            ), 100, outputFile
        )
        try {
            outputFile.close()
        } catch (e: IOException) {
            showMessage("test screenShot: compress yuv image error: ${e.message}")
        }
        Message.obtain(mHandler, DISPLAY, path).sendToTarget()
    }

    private fun display(path: String) {
        stringBuilder?.let {
            it.insert(0, path)
            it.insert(0, "\n")
            binding?.yuvScreenSavePath?.text = it
        }
    }

    private fun showDisconnectToast() {
        showMessage("video stream disconnect")
    }
}