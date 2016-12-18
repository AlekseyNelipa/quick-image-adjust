package com.example.aleksey.testapp002

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }

    fun onAddPoint(view: View) {
        val curveView = findViewById(R.id.surface_view) as CurveView
        curveView.startAddPoint()
    }

    fun onRemovePoint(view: View) {
        val curveView = findViewById(R.id.surface_view) as CurveView
        curveView.startRemovePoint()
    }
}



