package com.humotron.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.domain.repository.SleepRepository
import com.humotron.app.util.PrefUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val prefUtils: PrefUtils,
    private val sleepRepository: SleepRepository
) : ViewModel() {

    // Using a Channel guarantees that the event is buffered and not lost 
    // if emitted while the Activity is in the background.
    private val _navigationEvent = Channel<NavigationDestination>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        // load user device data earlier so user don't have blank screen or loading.
        if (prefUtils.isLogin()) {
            sleepRepository.getUserDeviceData()
        }

        viewModelScope.launch {
            delay(2000L * 3)
            if (prefUtils.isLogin()) {
                _navigationEvent.send(NavigationDestination.MAIN)
            } else {
                _navigationEvent.send(NavigationDestination.ONBOARDING)
            }
        }
    }

    enum class NavigationDestination {
        MAIN, ONBOARDING
    }
}