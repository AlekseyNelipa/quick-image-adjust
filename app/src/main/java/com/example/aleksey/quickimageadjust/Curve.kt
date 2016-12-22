package com.example.aleksey.quickimageadjust

import java.util.*

class Curve {
    private val _k = 4
    val max = 1000.0
    val defaultPoints = arrayListOf(
            VectorD(0, max),
            VectorD(max/3, 2*max/3),
            VectorD(2*max/3, max/3),
            VectorD(max, 0)
    )
    val points: ArrayList<VectorD>
    lateinit var controlPoints: List<VectorD>
    lateinit var curvePoints: List<VectorD>

    init {
        points = ArrayList(defaultPoints)
        recalculateCurvePoints()
    }

    fun reset() {
        points.clear()
        points.addAll(defaultPoints)
        recalculateCurvePoints()
    }

    fun recalculateCurvePoints() {
        controlPoints = getAllControlPoints()
        curvePoints = getCurvePoints()
    }


    fun insertPoint(newPoint: VectorD) {
        val index = points.indexOfFirst { it.x > newPoint.x }
        if (index <= 0 || index >= points.size - 1)
            return
        points.add(index, newPoint)
        recalculateCurvePoints()

    }

    fun removePoint(index: Int) {
        points.removeAt(index)
        recalculateCurvePoints()
    }

    fun movePoint(index: Int, newPoint: VectorD) {
        points[index] = newPoint
        recalculateCurvePoints()
    }

    fun findPointOnCurve(x: Float, y: Float): VectorD? {
        val clickPoint = VectorD(x, y)

        var closestIndex = -1
        var minDistSq = max * max

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
        val vPoint = curvePoints[closestIndex] + vDir * (clickPoint - curvePoints[closestIndex]).dot(vDir)

        if (vPoint.x <= 0 || vPoint.y >= max)
            return null


        return VectorD(vPoint.x, vPoint.y.restrict(0.0, max))
    }

    private fun getCurvePoints(): ArrayList<VectorD> {
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

            val numSteps = Math.max(5, (50 * (points[i].x - points[i - 1].x) / max).toInt())

            for (tt in 1..numSteps) {
                val t = tt / numSteps.toDouble()

                val x = fx(t).restrict(0.0, max)
                val y = fy(t).restrict(0.0, max)

                curvePoints.add(VectorD(x, y))
            }
        }
        return curvePoints
    }

    private fun getAllControlPoints(): List<VectorD> {
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
            8 * dx > 7 * dy && 7 * dx < 8 * dy -> VectorD(dx / _k + p0.x, -dy / _k + p0.y)
            dx > dy -> VectorD(dx / _k + p0.x, p0.y)
            else -> VectorD(p0.x, -dy / _k + p0.y)
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
        val vDir = v1.normalize() + v2.normalize()
        val vp = when {
            vDir.lengthSquared()>0.0001 -> vDir.onePerpendicular()
            else -> v1
        }
        val vp1 = vp * (v1.x / (_k * vp.x))
        val vp2 = vp * (v2.x / (_k * vp.x))
        return Pair(p1 + vp1, p1 + vp2)
    }


}