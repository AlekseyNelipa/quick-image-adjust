package com.example.aleksey.testapp002

import android.databinding.BaseObservable
import android.databinding.Bindable

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
}