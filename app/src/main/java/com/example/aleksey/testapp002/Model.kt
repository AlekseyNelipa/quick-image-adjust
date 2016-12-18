package com.example.aleksey.testapp002

import android.databinding.BaseObservable
import android.databinding.Bindable

public class Model : BaseObservable() {
    @Bindable
    var mode: String = ""
        set(mode) {
            field = mode
            notifyPropertyChanged(com.example.aleksey.testapp002.BR.mode)
        }
}