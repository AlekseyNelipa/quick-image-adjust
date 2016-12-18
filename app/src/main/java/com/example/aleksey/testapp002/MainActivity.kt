package com.example.aleksey.testapp002

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.aleksey.testapp002.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    val _model: Model

    init {
        _model = Model()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.model = _model
        //val curveView = findViewById(R.id.surface_view) as CurveView
        //curveView.model = _model

    }

}



