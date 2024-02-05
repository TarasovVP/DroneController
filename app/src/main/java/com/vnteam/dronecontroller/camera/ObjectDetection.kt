package com.vnteam.dronecontroller.camera

import org.tensorflow.lite.task.vision.detector.Detection

data class ObjectDetection(
    var sourceImage: ByteArray?,
    var results: MutableList<Detection>?,
    var inferenceTime: Long,
    var imageHeight: Int,
    var imageWidth: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ObjectDetection

        if (!sourceImage.contentEquals(other.sourceImage)) return false
        if (results != other.results) return false
        if (inferenceTime != other.inferenceTime) return false
        if (imageHeight != other.imageHeight) return false
        return imageWidth == other.imageWidth
    }

    override fun hashCode(): Int {
        var result = sourceImage.contentHashCode()
        result = 31 * result + (results?.hashCode() ?: 0)
        result = 31 * result + inferenceTime.hashCode()
        result = 31 * result + imageHeight
        result = 31 * result + imageWidth
        return result
    }
}
