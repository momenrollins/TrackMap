package com.houseofdevelopment.gps.base

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.houseofdevelopment.gps.MainActivity
import com.houseofdevelopment.gps.network.ApiClientForBrainvire
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.utils.DebugLog

class AppBase : Application(), LifecycleObserver {
    private var currentActivity: Activity? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: AppBase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        MyPreference.init(this)
        ApiClientForBrainvire.initRetrofit()
        AppCenter.start(
            this, "a83c8be3-6f50-4c0c-99ee-e71d96211ec9",
            Analytics::class.java, Crashes::class.java
        )
        instance = this
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun setCurrentActivity(activity: Activity) {
        currentActivity = activity
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        DebugLog.e("App in background")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        DebugLog.e("App in foreground")
        if (currentActivity is MainActivity) {
            (currentActivity as MainActivity).callApi()
        }
    }
}
