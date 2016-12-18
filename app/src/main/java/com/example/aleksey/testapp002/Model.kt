package com.example.aleksey.testapp002

import android.databinding.BaseObservable
import android.databinding.Bindable

enum class EditMode { Move, Add, Remove }

public class Model : BaseObservable() {
    @Bindable
    var mode: EditMode = EditMode.Move
        set(mode) {
            field = mode
            notifyPropertyChanged(BR.mode)
        }
}