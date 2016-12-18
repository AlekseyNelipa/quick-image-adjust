package com.example.aleksey.testapp002

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.example.aleksey.testapp002.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    val _model : Model
    init{
        _model = Model()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val binding : ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.model = _model

    }


    fun onAddPoint(view: View) {
        _model.mode = "ADD_POINT"
        val curveView = findViewById(R.id.surface_view) as CurveView
        curveView.startAddPoint()
    }

    fun onRemovePoint(view: View) {
        val curveView = findViewById(R.id.surface_view) as CurveView
        curveView.startRemovePoint()
    }
    fun onCancelCurrent(view: View) {
        _model.mode = ""
        val curveView = findViewById(R.id.surface_view) as CurveView
        curveView.startRemovePoint()
    }
}



