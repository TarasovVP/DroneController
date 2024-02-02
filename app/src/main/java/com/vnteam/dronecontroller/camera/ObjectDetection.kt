package com.vnteam.dronecontroller.camera

import org.tensorflow.lite.task.vision.detector.Detection

data class ObjectDetection(
    var results: MutableList<Detection>?,
    var inferenceTime: Long,
    var imageHeight: Int,
    var imageWidth: Int
)
