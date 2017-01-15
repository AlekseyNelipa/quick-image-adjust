package com.example.aleksey.quickimageadjust

import android.content.Context
import android.databinding.Observable
import android.databinding.Observable.OnPropertyChangedCallback
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


internal class CurveView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val _pointRadius = 50F

    private val _paint: Paint
    private val _paintControlPoint: Paint
    private val _paintFill: Paint


    private var _selectedIndex: Int = -1

    private val _model: Model

    private var _width: Int? = null
    private var _height: Int? = null
    private var _scale: Float? = null


    init {

        if (isInEditMode) {
            _model = Model()
        } else {
            _model = (context as MainActivity)._model
            _model.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
                override fun onPropertyChanged(p0: Observable?, propertyId: Int) {
                    if (propertyId == BR.preview)
                        invalidate()
                }
            })
        }

        _paint = Paint()
        _paint.style = Paint.Style.STROKE
        _paint.color = Color.RED
        _paint.strokeWidth = 10F

        _paintFill = Paint(_paint)
        _paintFill.style = Paint.Style.FILL

        _paintControlPoint = Paint(_paint)
        _paintControlPoint.strokeWidth = 3F


    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (_scale == null || canvas == null) {
            return
        }

        if (_model.imageData != null) {
            val bitmap = when {
                _model.preview -> _model.imageData!!.bitmapAltered
                else -> _model.imageData!!.bitmap
            }
            canvas.drawBitmap(
                    bitmap,
                    Rect(0, 0, bitmap.width, bitmap.height),
                    getImageRect(bitmap, _width!!, _height!!),
                    _paint)
        }

        canvas.scale(_scale!!, _scale!!)

        val path = createPath(_model.curve.curvePoints)

        canvas.drawPath(path, _paint)
        for ((x, y) in _model.curve.points) {
            canvas.drawCircle(x.toFloat(), y.toFloat(), _pointRadius, _paint)
        }
        if (_selectedIndex != -1) {
            val point = _model.curve.points[_selectedIndex]
            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), 30F, _paintFill)
        }

        for ((x, y) in _model.curve.controlPoints) {
            canvas.drawCircle(x.toFloat(), y.toFloat(), 10F, _paintControlPoint)
        }
    }



    fun getImageRect(bitmap: Bitmap, canvasWidth: Int, canvasHeight: Int): Rect {
        val imageScale = Math.min(canvasWidth.toFloat() / bitmap.width, canvasWidth.toFloat() / bitmap.height)
        val xMargin = (canvasWidth - bitmap.width * imageScale).toInt() / 2
        val yMargin = (canvasHeight - bitmap.height * imageScale).toInt() / 2
        return Rect(xMargin, yMargin, canvasWidth - xMargin, canvasHeight - yMargin)
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        _width = w
        _height = h
        _scale = Math.min(w, h) / _model.curve.max.toFloat()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (_scale == null || event == null)
            return false

        val x = event.x / _scale!!
        val y = event.y / _scale!!

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                when (_model.mode) {
                    EditMode.Add -> {
                        val newPoint = _model.curve.findPointOnCurve(x, y)
                        if (newPoint != null) {

                            _model.curve.insertPoint(newPoint)

                            _model.mode = EditMode.Move
                            alterBitmap()
                        }
                    }
                    EditMode.Remove -> {
                        val index = getPointNumber(x, y)
                        if (index > 0 && index < _model.curve.points.size - 1) {
                            if (_selectedIndex == index)
                                _selectedIndex = -1
                            _model.curve.removePoint(index)
                            _model.mode = EditMode.Move
                            alterBitmap()
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
                        0 -> 0.0
                        _model.curve.points.size - 1 -> _model.curve.max
                        else -> x.toDouble().restrict(
                                _model.curve.points[_selectedIndex - 1].x + 1,
                                _model.curve.points[_selectedIndex + 1].x - 1)
                    }
                    val yNew = y.toDouble().restrict(0.0, _model.curve.max)
                    _model.curve.movePoint(_selectedIndex, VectorD(xNew, yNew))
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                _selectedIndex = -1
                alterBitmap()
            }
        }

        super.onTouchEvent(event)
        return _selectedIndex != -1

    }

    fun alterBitmap() {
        _model.imageData?.alterBitmap(_model.curve.curvePoints, _model.curve.max)
        invalidate()
    }


    private fun getPointNumber(x: Float, y: Float): Int {
        val radiusSq = _pointRadius * _pointRadius
        return _model.curve.points.indexOfFirst {
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


    fun reset() {
        _model.curve.reset()
        alterBitmap()
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