package com.example.aleksey.testapp002

import android.graphics.Bitmap

internal class ImageData (val bitmap: Bitmap) {
    val bitmapAltered: Bitmap
    private val pixelsOrigR: IntArray
    private val pixelsOrigG: IntArray
    private val pixelsOrigB: IntArray
    private val pixelsAltered: IntArray
    init {
        bitmapAltered = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val pixelsOrig = IntArray(bitmap.height * bitmap.width)
        pixelsOrigR = IntArray(bitmap.height * bitmap.width)
        pixelsOrigG = IntArray(bitmap.height * bitmap.width)
        pixelsOrigB = IntArray(bitmap.height * bitmap.width)
        bitmap.getPixels(pixelsOrig, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        pixelsAltered = pixelsOrig.copyOf()
        for (i in 0..pixelsOrig.size - 1) {
            pixelsOrigR[i] = pixelsOrig[i] shr 16 and 0xff
            pixelsOrigG[i] = pixelsOrig[i] shr 8 and 0xff
            pixelsOrigB[i] = pixelsOrig[i] and 0xff
        }
    }

    public fun alterBitmap(curvePoints: List<VectorD>) {
        val valueMap = buildColorMap(curvePoints)
        for (i in 0..pixelsAltered.size - 1) {
            pixelsAltered[i] = (0xff000000.toInt()
                    or (valueMap[pixelsOrigR[i]] shl 16)
                    or (valueMap[pixelsOrigG[i]] shl 8)
                    or valueMap[pixelsOrigB[i]])

        }

        bitmapAltered.setPixels(pixelsAltered, 0, bitmapAltered.width, 0, 0, bitmapAltered.width, bitmapAltered.height)
    }

    private fun buildColorMap(curvePoints: List<VectorD>): IntArray {
        val valueMap = IntArray(256)


        var xInt = 0
        for (i in 1..curvePoints.size - 1) {
            val (x, y) = curvePoints[i] * 255 / 1000
            val (x0, y0) = curvePoints[i - 1] * 255 / 1000

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