package com.example.aleksey.testapp002

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.*


internal class CurveView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    val _k = 2
    val _points = arrayOf(
            PointF(0F, 1000F),
            PointF(333.333F, 666.666F),
            PointF(666.666F, 333.333F),
            PointF(1000F, 0F)
    )
    val _paint: Paint
    val _paintControlPoint: Paint
    val _paintFill: Paint
    private val _bitmap: Bitmap
    private val _bitmapAltered: Bitmap
    val _pixelsOrig: IntArray
    val _pixelsAltered: IntArray

    var _selectedIndex: Int = -1

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
        _pixelsOrig = IntArray(_bitmap.height * _bitmap.width)
        _bitmap.getPixels(_pixelsOrig, 0, _bitmap.width, 0, 0, _bitmap.width, _bitmap.height)
        _pixelsAltered = _pixelsOrig.copyOf()
        alterBitmap()

    }

    private fun alterBitmap() {
        val valueMap = buildColorMap()
        for (i in 0.._pixelsOrig.size - 1) {
            val r = _pixelsOrig[i] shr 16 and 0xff
            val g = _pixelsOrig[i] shr 8 and 0xff
            val b = _pixelsOrig[i] and 0xff

            _pixelsAltered[i] = 0xff000000.toInt() or (valueMap[r] shl 16) or (valueMap[g] shl 8) or valueMap[b]

        }

        _bitmapAltered.setPixels(_pixelsAltered, 0, _bitmapAltered.width, 0, 0, _bitmapAltered.width, _bitmapAltered.height)
    }

    private fun buildColorMap(): IntArray {
        val valueMap = IntArray(256)
        val controlPoints = getControlPoints(_points)
        var xLast = 0

        for (i in 1.._points.size - 1) {
            val fx = { t: Double ->
                (_points[i].x * t * t * t
                +3 * controlPoints[i * 2 - 1].x * t * t * (1 - t)
                +3 * controlPoints[i * 2 - 2].x * t * (1 - t) * (1 - t)
                +_points[i - 1].x * (1 - t) * (1 - t) * (1 - t))
            }
            val fy = { t: Double ->
                (_points[i].y * t * t * t
                +3 * controlPoints[i * 2 - 1].y * t * t * (1 - t)
                +3 * controlPoints[i * 2 - 2].y * t * (1 - t) * (1 - t)
                +_points[i - 1].y * (1 - t) * (1 - t) * (1 - t))
            }

            for (tt in 0..20) {
                val t = tt / 20.0

                val x = fx(t) * 255.0 / 1000.0
                val y = fy(t) * 255.0 / 1000.0


                while (xLast <= x.toInt()) {
                    valueMap[xLast] = 255 - y.toInt()
                    xLast++
                }

            }
        }
        return valueMap
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
        val path = createPath(_points, controlPoints)

        canvas.drawPath(path, _paint)
        for (point in _points) {
            canvas.drawCircle(point.x, point.y, 20F, _paint)
        }
        if (_selectedIndex != -1)
            canvas.drawCircle(_points[_selectedIndex].x, _points[_selectedIndex].y, 20F, _paintFill)

        for (point in controlPoints) {
            canvas.drawCircle(point.x, point.y, 10F, _paintControlPoint)
        }
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        _width = w
        _height = h
        _scale = Math.min(w, h) / 1000F
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (_scale == null)
            return false

        //val curveView = findViewById(R.id.surface_view) as CurveView
        val radiusSq = 20 * 20

        val x = event!!.x / _scale!!
        val y = event.y / _scale!!

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                _selectedIndex = _points.indexOfFirst {
                    val dx = it.x - x
                    val dy = it.y - y
                    dx * dx < radiusSq && dy * dy < radiusSq
                }
                if (_selectedIndex != -1) invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                if (_selectedIndex != -1) {
                    val currentPoint = _points[_selectedIndex]
                    currentPoint.x = when (_selectedIndex) {
                        0 -> 0F
                        _points.size-1 -> 1000F
                        else -> Math.max(0F, Math.min(1000F, x))
                    }
                    currentPoint.y = Math.max(0F, Math.min(1000F, y))
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


    private fun createPath(points: Array<PointF>, controlPoints: List<PointF>): Path {
        val path = Path()

        path.moveTo(points[0].x, points[0].y)
        for (i in 1..points.size - 1) {
            val cp0 = controlPoints[i * 2 - 2]
            val cp1 = controlPoints[i * 2 - 1]
            val p = points[i]
            path.cubicTo(cp0.x, cp0.y, cp1.x, cp1.y, p.x, p.y)
        }

        return path
    }

    private fun getControlPoints(points: Array<PointF>): Vector<PointF> {
        val controlPoints = Vector<PointF>()

        controlPoints.addElement(getFirstControlPoint(points[0], points[1]))
        for (i in 1..points.size - 2) {
            val (p1, p2) = getControlPoints(points[i - 1], points[i], points[i + 1])
            controlPoints.addElement(p1)
            controlPoints.addElement(p2)
        }
        controlPoints.addElement(getLastControlPoint(points[points.size - 2], points.last()))
        return controlPoints
    }


    private fun getFirstControlPoint(p0: PointF, p1: PointF): PointF {
        val dx = p1.x - p0.x
        val dy = -(p1.y - p0.y)
        return when {
            8 * dx > 7 * dy && 7 * dx < 8 * dy -> PointF(dx / 3 + p0.x, -dy / 3 + p0.y)
            dx > dy -> PointF(dx / 3 + p0.x, p0.y)
            else -> PointF(p0.x, -dy / 3 + p0.y)
        }
    }

    private fun getLastControlPoint(pNextToLast: PointF, pLast: PointF): PointF {
        val dx = pLast.x - pNextToLast.x
        val dy = -(pLast.y - pNextToLast.y)
        return when {
            8 * dx > 7 * dy && 7 * dx < 8 * dy -> PointF(pLast.x - dx / _k, pLast.y + dy / _k)
            dx > dy -> PointF(pLast.x - dx / _k, pLast.y)
            else -> PointF(pLast.x, pLast.y + dy / _k)
        }
    }

    private fun getControlPoints(p0: PointF, p1: PointF, p2: PointF): Pair<PointF, PointF> {
        val v1 = VectorD(p1, p0)
        val v2 = VectorD(p1, p2)
        val vp = (v1.normalize() + v2.normalize()).onePerpendicular().normalize()
        //val vp1 = vp * v1.dot(vp) / _k
        //val vp2 = vp * v2.dot(vp) / _k
        val vp1 = vp * VectorD(v1.x, 0.0).dot(vp) / _k
        val vp2 = vp * VectorD(v2.x, 0.0).dot(vp) / _k
        return Pair(p1 + vp1, p1 + vp2)
    }
}