package com.example.aleksey.testapp002

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
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

    fun openImage(view: View) {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            val targetUri = data.data
            val bitmap: Bitmap

            bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(targetUri))

            val curveView = findViewById(R.id.surface_view) as CurveView
            curveView.setImage(bitmap)

        }
    }
}





