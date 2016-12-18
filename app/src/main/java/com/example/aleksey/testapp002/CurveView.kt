package com.example.aleksey.testapp002

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.*


internal class CurveView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    val _k = 3
    val _pointRadius = 30F
    val _points = arrayListOf(
            VectorD(0, 1000),
            VectorD(333.333, 666.666),
            VectorD(666.666, 333.333),
            VectorD(1000, 0)
    )
    val _paint: Paint
    val _paintControlPoint: Paint
    val _paintFill: Paint
    private val _bitmap: Bitmap
    private val _bitmapAltered: Bitmap
    val _pixelsOrigR: IntArray
    val _pixelsOrigG: IntArray
    val _pixelsOrigB: IntArray
    val _pixelsAltered: IntArray

    var _selectedIndex: Int = -1

    public var model: Model? = null

    private var _width: Int? = null
    private var _height: Int? = null
    private var _scale: Float? = null


    init {
        _paint = Paint()
        _paint.style = Paint.Style.STROKE
        _paint.color = Color.RED
        _paint.strokeWidth = 5F

        _paintFill = Paint(_paint)
        _paintFill.style = Paint.Style.FILL

        _paintControlPoint = Paint(_paint)
        _paintControlPoint.strokeWidth = 3F


        _bitmap = BitmapFactory.decodeResource(resources, R.drawable.chariot)
        _bitmapAltered = Bitmap.createBitmap(_bitmap.width, _bitmap.height, Bitmap.Config.ARGB_8888)
        val pixelsOrig = IntArray(_bitmap.height * _bitmap.width)
        _pixelsOrigR = IntArray(_bitmap.height * _bitmap.width)
        _pixelsOrigG = IntArray(_bitmap.height * _bitmap.width)
        _pixelsOrigB = IntArray(_bitmap.height * _bitmap.width)
        _bitmap.getPixels(pixelsOrig, 0, _bitmap.width, 0, 0, _bitmap.width, _bitmap.height)
        _pixelsAltered = pixelsOrig.copyOf()
        for (i in 0..pixelsOrig.size - 1) {
            _pixelsOrigR[i] = pixelsOrig[i] shr 16 and 0xff
            _pixelsOrigG[i] = pixelsOrig[i] shr 8 and 0xff
            _pixelsOrigB[i] = pixelsOrig[i] and 0xff
        }
        alterBitmap()

    }

    private fun alterBitmap() {
        val valueMap = buildColorMap()
        for (i in 0.._pixelsAltered.size - 1) {
            _pixelsAltered[i] = (0xff000000.toInt()
                    or (valueMap[_pixelsOrigR[i]] shl 16)
                    or (valueMap[_pixelsOrigG[i]] shl 8)
                    or valueMap[_pixelsOrigB[i]])

        }

        _bitmapAltered.setPixels(_pixelsAltered, 0, _bitmapAltered.width, 0, 0, _bitmapAltered.width, _bitmapAltered.height)
    }

    private fun buildColorMap(): IntArray {
        val valueMap = IntArray(256)
        val controlPoints = getControlPoints(_points)
        val curvePoints = getCurvePoints(_points, controlPoints)

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

    private fun getCurvePoints(points: List<VectorD>, controlPoints: List<VectorD>): ArrayList<VectorD> {
        val curvePoints = ArrayList<VectorD>()

        curvePoints.add(points[0])
        for (i in 1..points.size - 1) {
            val fx = { t: Double ->
                (points[i].x * t * t * t
                        + 3 * controlPoints[i * 2 - 1].x * t * t * (1 - t)
                        + 3 * controlPoints[i * 2 - 2].x * t * (1 - t) * (1 - t)
                        + points[i - 1].x * (1 - t) * (1 - t) * (1 - t))
            }
            val fy = { t: Double ->
                (points[i].y * t * t * t
                        + 3 * controlPoints[i * 2 - 1].y * t * t * (1 - t)
                        + 3 * controlPoints[i * 2 - 2].y * t * (1 - t) * (1 - t)
                        + points[i - 1].y * (1 - t) * (1 - t) * (1 - t))
            }

            val numSteps = Math.max(5, (50 * (points[i].x - points[i - 1].x) / 1000).toInt())

            for (tt in 1..numSteps) {
                val t = tt / numSteps.toDouble()

                val x = fx(t).restrict(0.0, 1000.0)
                val y = fy(t).restrict(0.0, 1000.0)

                curvePoints.add(VectorD(x, y))
            }
        }
        return curvePoints
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (_scale == null)
            return

        canvas!!.drawBitmap(
                _bitmapAltered,
                Rect(0, 0, _bitmapAltered.width, _bitmapAltered.height),
                Rect(100, 100, _width!! - 100, _bitmapAltered.height * (_width!! - 200) / _bitmapAltered.width),
                _paint)

        canvas.scale(_scale!!, _scale!!)

        val controlPoints = getControlPoints(_points)
        val curvePoints = getCurvePoints(_points, controlPoints)
        val path = createPath(curvePoints)

        canvas.drawPath(path, _paint)
        for (point in _points) {
            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), _pointRadius, _paint)
        }
        if (_selectedIndex != -1)
            canvas.drawCircle(_points[_selectedIndex].x.toFloat(), _points[_selectedIndex].y.toFloat(), 30F, _paintFill)

        for (point in controlPoints) {
            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), 10F, _paintControlPoint)
        }
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        _width = w
        _height = h
        _scale = Math.min(w, h) / 1000F
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (_scale == null || model==null)
            return false

        val x = event!!.x / _scale!!
        val y = event.y / _scale!!

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                when (model!!.mode) {
                    EditMode.Add -> {
                        val newPoint = findPointOnCurve(x, y)
                        if (newPoint != null) {
                            _points.add(_points.indexOfFirst { it.x > newPoint.x }, newPoint)
                            model!!.mode = EditMode.Move
                            alterBitmap()
                            invalidate()
                        }
                    }
                    EditMode.Remove -> {
                        val index = getPointNumber(x, y)
                        if (index > 0 && index < _points.size - 1) {
                            if (_selectedIndex == index)
                                _selectedIndex = -1
                            _points.removeAt(index)
                            model!!.mode = EditMode.Move
                            alterBitmap()
                            invalidate()
                        }
                    }
                    EditMode.Move -> {
                        _selectedIndex = getPointNumber(x, y)
                        if (_selectedIndex != -1)
                            invalidate()
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (_selectedIndex != -1) {
                    val xNew = when (_selectedIndex) {
                        0 -> 0F
                        _points.size - 1 -> 1000F
                        else -> x.restrict(_points[_selectedIndex - 1].x.toFloat() + 1, _points[_selectedIndex + 1].x.toFloat() - 1)
                    }
                    val yNew = y.restrict(0F, 1000F)
                    _points[_selectedIndex] = VectorD(xNew, yNew)
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                _selectedIndex = -1
                alterBitmap()
                invalidate()
            }
        }

        super.onTouchEvent(event)
        return _selectedIndex != -1

    }

    private fun findPointOnCurve(x: Float, y: Float): VectorD? {
        val controlPoints = getControlPoints(_points)
        val curvePoints = getCurvePoints(_points, controlPoints)
        val clickPoint = VectorD(x, y)

        var closestIndex = -1
        var minDistSq = 1000.0 * 1000.0

        for (i in 0..curvePoints.size - 1) {
            val distSq = curvePoints[i].distanceSquared(clickPoint)
            if (distSq < minDistSq) {
                minDistSq = distSq
                closestIndex = i
            }
            if (distSq < 1)
                return curvePoints[closestIndex]
        }

        val otherIndex = if (closestIndex == 0) 1 else closestIndex - 1

        val vDir = (curvePoints[closestIndex] - curvePoints[otherIndex]).normalize()
        val vPoint = vDir * (clickPoint - curvePoints[closestIndex]).dot(vDir)
        return curvePoints[closestIndex] + vPoint
    }

    private fun getPointNumber(x: Float, y: Float): Int {
        val radiusSq = _pointRadius * _pointRadius
        return _points.indexOfFirst {
            val dx = it.x - x
            val dy = it.y - y
            dx * dx < radiusSq && dy * dy < radiusSq
        }
    }


    private fun createPath(curvePoints: List<VectorD>): Path {
        val path = Path()

        path.moveTo(curvePoints[0].x.toFloat(), curvePoints[0].y.toFloat())
        for (i in 1..curvePoints.size - 1) {
            path.lineTo(curvePoints[i].x.toFloat(), curvePoints[i].y.toFloat())
        }

        return path
    }

    private fun getControlPoints(points: List<VectorD>): List<VectorD> {
        val controlPoints = ArrayList<VectorD>()

        controlPoints.add(getFirstControlPoint(points[0], points[1]))
        for (i in 1..points.size - 2) {
            val (p1, p2) = getControlPoints(points[i - 1], points[i], points[i + 1])
            controlPoints.add(p1)
            controlPoints.add(p2)
        }
        controlPoints.add(getLastControlPoint(points[points.size - 2], points.last()))
        return controlPoints
    }


    private fun getFirstControlPoint(p0: VectorD, p1: VectorD): VectorD {
        val dx = p1.x - p0.x
        val dy = -(p1.y - p0.y)
        return when {
            8 * dx > 7 * dy && 7 * dx < 8 * dy -> VectorD(dx / 3 + p0.x, -dy / 3 + p0.y)
            dx > dy -> VectorD(dx / 3 + p0.x, p0.y)
            else -> VectorD(p0.x, -dy / 3 + p0.y)
        }
    }

    private fun getLastControlPoint(pNextToLast: VectorD, pLast: VectorD): VectorD {
        val dx = pLast.x - pNextToLast.x
        val dy = -(pLast.y - pNextToLast.y)
        return when {
            8 * dx > 7 * dy && 7 * dx < 8 * dy -> VectorD(pLast.x - dx / _k, pLast.y + dy / _k)
            dx > dy -> VectorD(pLast.x - dx / _k, pLast.y)
            else -> VectorD(pLast.x, pLast.y + dy / _k)
        }
    }

    private fun getControlPoints(p0: VectorD, p1: VectorD, p2: VectorD): Pair<VectorD, VectorD> {
        val v1 = p0 - p1
        val v2 = p2 - p1
        val vp = (v1.normalize() + v2.normalize()).onePerpendicular().normalize()
        val vp1 = vp * (v1.x * vp.x / _k)
        val vp2 = vp * (v2.x * vp.x / _k)
        return Pair(p1 + vp1, p1 + vp2)
    }


}

fun Int.restrict(min: Int, max: Int): Int = when {
    this < min -> min
    this > max -> max
    else -> this
}


fun Float.restrict(min: Float, max: Float): Float = when {
    this < min -> min
    this > max -> max
    else -> this
}

fun Double.restrict(min: Double, max: Double): Double = when {
    this < min -> min
    this > max -> max
    else -> this
}