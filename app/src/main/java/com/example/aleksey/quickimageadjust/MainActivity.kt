package com.example.aleksey.quickimageadjust

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.example.aleksey.quickimageadjust.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    val _model: Model

    init {
        _model = Model()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState!=null) {
            _model.curve.reset(savedInstanceState.getSerializable("CURVE_KEY") as List<VectorD>)
            val uriStr = savedInstanceState.getSerializable("IMAGE_URI") as String?
            if(uriStr!=null)
                loadImage(Uri.parse(uriStr))
        }
        setContentView(R.layout.activity_main)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.model = _model
        val curveView = findViewById(R.id.surface_view) as CurveView
        curveView.alterBitmap()

    }


    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putSerializable("CURVE_KEY", _model.curve.points)
        if(_model.imageUri!=null)
            outState?.putSerializable("IMAGE_URI", _model.imageUri!!.toString())

        super.onSaveInstanceState(outState)
    }


    fun openImage(view: View) {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==0 && resultCode == Activity.RESULT_OK && data!=null) {
            _model.imageUri = data.data
            loadImage(data.data)

            val curveView = findViewById(R.id.surface_view) as CurveView
            curveView.reset()
        }
    }

    private fun loadImage(targetUri: Uri) {
        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(targetUri))
        _model.imageData = ImageData(bitmap)
    }

    fun onReset(view: View) {
        val curveView = findViewById(R.id.surface_view) as CurveView
        curveView.reset()
    }
}





