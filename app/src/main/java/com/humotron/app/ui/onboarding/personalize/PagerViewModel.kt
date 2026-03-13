package com.humotron.app.ui.onboarding.personalize

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PagerViewModel : ViewModel() {
    private val _navigateToPage = MutableLiveData<Int>()
    val navigateToPage: LiveData<Int> get() = _navigateToPage

    fun moveToPage(index: Int) {
        _navigateToPage.value = index
    }
}