package com.example.aleksey.quickimageadjust

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.net.Uri

enum class EditMode { Move, Add, Remove }

class Model : BaseObservable() {
    @Bindable
    var mode: EditMode = EditMode.Move
        set(mode) {
            field = mode
            notifyPropertyChanged(BR.mode)
        }

    @Bindable
    var preview: Boolean = true
        set(preview) {
            field = preview
            notifyPropertyChanged(BR.preview)
        }

    val curve = Curve()

    var imageData: ImageData? = null
    var imageUri: Uri? = null
}