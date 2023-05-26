package com.example.financemanagerandroid

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SecondFragmentViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is Second Fragment"
    }
    val text: LiveData<String> = _text
}