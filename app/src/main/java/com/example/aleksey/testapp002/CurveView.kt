package com.example.aleksey.testapp002

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


internal class CurveView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val _pointRadius = 30F

    private val _paint: Paint
    private val _paintControlPoint: Paint
    private val _paintFill: Paint
    private var _imageData: ImageData


    private var _selectedIndex: Int = -1

    private val _model: Model

    private val _curve: Curve

    private var _width: Int? = null
    private var _height: Int? = null
    private var _scale: Float? = null


    init {
        _model = (context as MainActivity)._model
        _curve = Curve()

        _paint = Paint()
        _paint.style = Paint.Style.STROKE
        _paint.color = Color.RED
        _paint.strokeWidth = 5F

        _paintFill = Paint(_paint)
        _paintFill.style = Paint.Style.FILL

        _paintControlPoint = Paint(_paint)
        _paintControlPoint.strokeWidth = 3F

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.chariot)
        _imageData = ImageData(bitmap)
        alterBitmap()

    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (_scale == null) {
            return
        }

        val bitmap = _imageData.bitmapAltered
        canvas!!.drawBitmap(
                bitmap,
                Rect(0, 0, bitmap.width, bitmap.height),
                Rect(100, 100, _width!! - 100, bitmap.height * (_width!! - 200) / bitmap.width),
                _paint)

        canvas.scale(_scale!!, _scale!!)

        val path = createPath(_curve.curvePoints)

        canvas.drawPath(path, _paint)
        for ((x, y) in _curve.points) {
            canvas.drawCircle(x.toFloat(), y.toFloat(), _pointRadius, _paint)
        }
        if (_selectedIndex != -1) {
            val point = _curve.points[_selectedIndex]
            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), 30F, _paintFill)
        }

        for ((x, y) in _curve.controlPoints) {
            canvas.drawCircle(x.toFloat(), y.toFloat(), 10F, _paintControlPoint)
        }
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        _width = w
        _height = h
        _scale = Math.min(w, h) / 1000F
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (_scale == null || event==null)
            return false

        val x = event.x / _scale!!
        val y = event.y / _scale!!

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                when (_model.mode) {
                    EditMode.Add -> {
                        val newPoint = _curve.findPointOnCurve(x, y)
                        if (newPoint != null) {

                            _curve.insertPoint(newPoint)

                            _model.mode = EditMode.Move
                            alterBitmap()
                            invalidate()
                        }
                    }
                    EditMode.Remove -> {
                        val index = getPointNumber(x, y)
                        if (index > 0 && index < _curve.points.size - 1) {
                            if (_selectedIndex == index)
                                _selectedIndex = -1
                            _curve.removePoint(index)
                            _model.mode = EditMode.Move
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
                        _curve.points.size - 1 -> 1000F
                        else -> x.restrict(
                                _curve.points[_selectedIndex - 1].x.toFloat() + 1,
                                _curve.points[_selectedIndex + 1].x.toFloat() - 1)
                    }
                    val yNew = y.restrict(0F, 1000F)
                    _curve.movePoint(_selectedIndex, VectorD(xNew, yNew))
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

    private fun alterBitmap() {
        _imageData.alterBitmap(_curve.curvePoints)

    }


    private fun getPointNumber(x: Float, y: Float): Int {
        val radiusSq = _pointRadius * _pointRadius
        return _curve.points.indexOfFirst {
            val dx = it.x - x
            val dy = it.y - y
            dx * dx < radiusSq && dy * dy < radiusSq
        }
    }


    private fun createPath(curvePoints: List<VectorD>): Path {
        val path = Path()

        path.moveTo(curvePoints[0].x.toFloat(), curvePoints[0].y.toFloat())
        for (i in 1..curvePoints.size - 1) {
            val (x, y) = curvePoints[i]
            path.lineTo(x.toFloat(), y.toFloat())
        }

        return path
    }


    fun setImage(bitmap: Bitmap) {
        _imageData = ImageData(bitmap)
        alterBitmap()
        invalidate()
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