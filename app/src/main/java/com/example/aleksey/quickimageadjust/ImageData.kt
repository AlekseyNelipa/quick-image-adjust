package com.example.aleksey.quickimageadjust

import android.graphics.Bitmap

class ImageData(val bitmap: Bitmap) {
    val bitmapAltered: Bitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    private val pixelsOrigR: IntArray = IntArray(bitmap.height * bitmap.width)
    private val pixelsOrigG: IntArray = IntArray(bitmap.height * bitmap.width)
    private val pixelsOrigB: IntArray = IntArray(bitmap.height * bitmap.width)
    private val pixelsAltered: IntArray

    init {
        val pixelsOrig = IntArray(bitmap.height * bitmap.width)
        bitmap.getPixels(pixelsOrig, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        pixelsAltered = pixelsOrig.copyOf()
        for (i in 0 until pixelsOrig.size) {
            pixelsOrigR[i] = pixelsOrig[i] shr 16 and 0xff
            pixelsOrigG[i] = pixelsOrig[i] shr 8 and 0xff
            pixelsOrigB[i] = pixelsOrig[i] and 0xff
        }
    }

    fun alterBitmap(curvePoints: List<VectorD>, curveMax: Double) {
        val valueMap = buildColorMap(curvePoints, curveMax)
        for (i in 0 until pixelsAltered.size) {
            pixelsAltered[i] = (0xff000000.toInt()
                    or (valueMap[pixelsOrigR[i]] shl 16)
                    or (valueMap[pixelsOrigG[i]] shl 8)
                    or valueMap[pixelsOrigB[i]])

        }

        bitmapAltered.setPixels(pixelsAltered, 0, bitmapAltered.width, 0, 0, bitmapAltered.width, bitmapAltered.height)
    }

    private fun buildColorMap(curvePoints: List<VectorD>, curveMax: Double): IntArray {
        val valueMap = IntArray(256)


        var xInt = 0
        for (i in 1 until curvePoints.size) {
            val (x, y) = curvePoints[i] * 255 / curveMax
            val (x0, y0) = curvePoints[i - 1] * 255 / curveMax

            while (xInt <= x.toInt()) {
                if (x == x0)
                    valueMap[xInt] = 255 - y0.toInt().restrict(0, 255)
                else
                    valueMap[xInt] = 255 - (y0 + (y - y0) * (xInt - x0) / (x - x0)).toInt().restrict(0, 255)
                xInt++
            }
        }
        return valueMap
    }

}