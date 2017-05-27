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
            notifyPropertyChanged(BR.canSave)
        }

    @Bindable
    fun getCanSave() : Boolean {
        return preview && imageData!=null
    }

    val curve = Curve()

    var imageData: ImageData? = null
        set(imageData) {
            field = imageData
            notifyPropertyChanged(BR.canSave)
        }

    var imageUri: Uri? = null
}