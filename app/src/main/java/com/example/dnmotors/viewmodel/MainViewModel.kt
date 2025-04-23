package com.example.dnmotors.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainViewModel : ViewModel() {
    private val _isBottomBarVisible = mutableStateOf(true)
    val isBottomBarVisible: State<Boolean> = _isBottomBarVisible

    fun setBottomBarVisible(visible: Boolean) {
        _isBottomBarVisible.value = visible
    }
}
