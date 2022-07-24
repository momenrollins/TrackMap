package com.houseofdevelopment.gps.splash.viewmodel

import androidx.lifecycle.MutableLiveData
import com.houseofdevelopment.gps.base.BaseViewModel


class SplashViewModel : BaseViewModel() {
    enum class SplashNavigate {
        LANG, DEMO, HOME, INIT, LOGIN
    }

    internal val navigateTo = MutableLiveData<SplashNavigate?>()
    internal val msg = MutableLiveData<String>()


    init {
        navigateTo.value = SplashNavigate.INIT
    }
}


