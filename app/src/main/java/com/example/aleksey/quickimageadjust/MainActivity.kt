package com.example.aleksey.quickimageadjust

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.aleksey.quickimageadjust.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


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


    fun saveImage(view: View) {
        val file = getNewFile()

        try {
            FileOutputStream(file).use { outputStream ->
                _model.imageData!!.bitmapAltered.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
            }
            Toast.makeText(applicationContext, "Saved image as ${file.absolutePath}", Toast.LENGTH_SHORT).show();
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Failed to save image! ${e.message}", Toast.LENGTH_LONG).show();
        }

        MediaScannerConnection.scanFile(this, arrayOf(file.absolutePath), null) { path, uri ->
            Log.d("d", "$path -- $uri")
            //Toast.makeText(applicationContext, "$path -- $uri", Toast.LENGTH_LONG).show();
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OPEN_IMAGE_ACTIVITY_ID) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                _model.imageUri = data.data
                loadImage(data.data)

                val curveView = findViewById<CurveView>(R.id.surface_view)
                curveView.reset()
                Toast.makeText(applicationContext, "Loaded ${_model.imageUri}", Toast.LENGTH_LONG).show();
            }
        }
    }

    private fun loadImage(targetUri: Uri) {
        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(targetUri))
        _model.imageData = ImageData(bitmap)
    }

    private fun getNewFile(): File {
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
        val saveDir = File(root + "/QuickImageAdjust")

        if (!saveDir.exists()) {
            saveDir.mkdirs()
        }

        for (i: Int in 0..1000) {
            val pattern = if (i == 0) "yyyyMMddHHmmss'.jpg'" else "yyyyMMddHHmmss'.${i}.jpg'"
            val file = File(saveDir, SimpleDateFormat(pattern, Locale.ROOT).format(Date()))
            if (!file.exists())
                return file
        }
        throw Exception("Failed to generate new file")
    }

    fun onReset(view: View) {
        val curveView = findViewById<CurveView>(R.id.surface_view)
        curveView.reset()
    }
}





