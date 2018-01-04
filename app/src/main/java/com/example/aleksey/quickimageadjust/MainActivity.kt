package com.example.aleksey.quickimageadjust

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.example.aleksey.quickimageadjust.databinding.ActivityMainBinding
import android.os.Environment.DIRECTORY_PICTURES
import android.os.Environment.getExternalStoragePublicDirectory
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import android.graphics.Bitmap
import android.support.v4.app.NotificationCompat.getExtras
import java.io.ByteArrayOutputStream
import android.R.attr.path


const val OPEN_IMAGE_ACTIVITY_ID: Int = 1

class MainActivity : AppCompatActivity() {


    val _model: Model = Model()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            _model.preview = savedInstanceState.getBoolean("PREVIEW_KEY")
            _model.mode = EditMode.valueOf(savedInstanceState.getString("MODEL_KEY"))
            _model.curve.reset(savedInstanceState.getParcelableArrayList<VectorD>("CURVE_KEY"))
            _model.imageUri = savedInstanceState.getParcelable<Uri>("IMAGE_URI")
            if (_model.imageUri != null)
                loadImage(_model.imageUri!!)
        }
        setContentView(R.layout.activity_main)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.model = _model
        val curveView = findViewById<CurveView>(R.id.surface_view)
        curveView.alterBitmap()

    }


    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putBoolean("PREVIEW_KEY", _model.preview)
        outState?.putString("MODEL_KEY", _model.mode.name)
        outState?.putParcelableArrayList("CURVE_KEY", _model.curve.points)
        if (_model.imageUri != null)
            outState?.putParcelable("IMAGE_URI", _model.imageUri)

        super.onSaveInstanceState(outState)
    }


    fun openImage(view: View) {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, OPEN_IMAGE_ACTIVITY_ID)
    }

    private fun fixMediaDir() {
        val sdcard = Environment.getExternalStorageDirectory()
        if (sdcard != null) {
            val mediaDir = File(sdcard, "DCIM/Camera")
            if (!mediaDir.exists()) {
                mediaDir.mkdirs()
            }
        }
    }

    fun saveImage(view: View) {
        fixMediaDir()

        val imageData = _model.imageData

        if(imageData!=null)
            MediaStore.Images.Media.insertImage(contentResolver, imageData.bitmapAltered, "test", "test")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OPEN_IMAGE_ACTIVITY_ID) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                _model.imageUri = data.data
                loadImage(data.data)

                val curveView = findViewById<CurveView>(R.id.surface_view)
                curveView.reset()
            }
        }
    }

    private fun loadImage(targetUri: Uri) {
        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(targetUri))
        _model.imageData = ImageData(bitmap)
    }

    fun onReset(view: View) {
        val curveView = findViewById<CurveView>(R.id.surface_view)
        curveView.reset()
    }
}





