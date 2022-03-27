package com.trackmap.gps.login.viewmodel

import androidx.lifecycle.MutableLiveData
import com.trackmap.gps.base.BaseViewModel


class LoginViewModel : BaseViewModel() {
    enum class SplashNavigate {
        LANG, DEMO, HOME, INIT, LOGIN
    }

    internal val navigateTo = MutableLiveData<SplashNavigate?>()
    internal val msg = MutableLiveData<String>()


    init {
        navigateTo.value = SplashNavigate.INIT
    }
}


