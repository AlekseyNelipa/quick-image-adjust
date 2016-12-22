package com.example.aleksey.quickimageadjust

import android.graphics.PointF
import java.io.Serializable


data class VectorD(val x: Double, val y: Double) : Serializable {

    constructor(x: Number, y: Number) : this(x.toDouble(), y.toDouble())

    fun normalize(): VectorD {
        val len = this.length()
        return VectorD(this.x / len, this.y / len)
    }

    fun length(): Double {
        return Math.sqrt(this.x * this.x + this.y * this.y)
    }

    fun onePerpendicular(): VectorD {
        if (this.y > 0)
            return VectorD(this.y, -this.x)
        else
            return VectorD(-this.y, this.x)
    }

    infix operator fun plus(v: VectorD): VectorD {
        return VectorD(this.x + v.x, this.y + v.y)
    }

    infix operator fun minus(v: VectorD): VectorD {
        return VectorD(this.x - v.x, this.y - v.y)
    }

    fun dot(v: VectorD): Double {
        return this.x * v.x + this.y * v.y
    }

    fun lengthSquared(): Double {
        return this.x*this.x + this.y*this.y
    }

    fun distanceSquared(v: VectorD): Double {
        return (this.x - v.x) * (this.x - v.x) + (this.y - v.y) * (this.y - v.y)
    }


    infix operator fun times(n: Number): VectorD {
        val d = n.toDouble()
        return VectorD(this.x * d, this.y * d)
    }

    infix operator fun div(n: Number): VectorD {
        val d = n.toDouble()
        return VectorD(this.x / d, this.y / d)
    }

    operator fun unaryMinus(): VectorD {
        return VectorD(-this.x, -this.y)
    }

    override fun toString(): String {
        return "V(${this.x}, ${this.y})"
    }
}

infix operator fun PointF.plus(v: VectorD): PointF {
    return PointF((this.x + v.x).toFloat(), (this.y + v.y).toFloat())
}