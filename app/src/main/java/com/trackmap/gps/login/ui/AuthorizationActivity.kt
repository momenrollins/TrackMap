package com.trackmap.gps.login.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.trackmap.gps.BuildConfig
import com.trackmap.gps.MainActivity
import com.trackmap.gps.R
import com.trackmap.gps.base.BaseActivity
import com.trackmap.gps.login.viewmodel.AuthorizationViewModel
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import com.trackmap.gps.utils.DebugLog
import kotlinx.android.synthetic.main.layout_authorization.*


class AuthorizationActivity : BaseActivity() {

    private var isFirstTime: Boolean = false
    private var url = BuildConfig.BaseURL + "login.html?client_id=trackingmaps&access_type=-1&activation_time=0&duration=0&flags=1"

    lateinit var authorizationViewModel: AuthorizationViewModel
    private var isApiCall = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authorizationViewModel = ViewModelProvider(this)[AuthorizationViewModel::class.java]
        setContentView(R.layout.layout_authorization)
        url = MyPreference.getValueString(PrefKey.SELECTED_SERVER_DATA, BuildConfig.BaseURL) + "login.html?client_id=trackingmaps&access_type=-1&activation_time=0&duration=0&flags=1"
        webView.loadUrl(url)
        webView.settings.setSupportZoom(true)
        webView.settings.javaScriptEnabled = true

        authorizationViewModel.tokenExpiredCall = MutableLiveData<Boolean>()
        observeData()

        isFirstTime = MyPreference.getValueBoolean(PrefKey.ISLOGIN, false)
        if (isFirstTime) {
            val intent = Intent(this@AuthorizationActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            webView.webViewClient = object : WebViewClient() {
                override fun onLoadResource(view: WebView?, url: String?) {
                    super.onLoadResource(view, url)
                     DebugLog.e("onLoadResource url : ${webView.url}")
                    if (webView.url!!.contains("&")) {
                        val requestUrl = webView.url
                        val subString = requestUrl?.split("=")?.toTypedArray()
                        val s2 = subString!![1].split("&").toTypedArray()
                        val finalToken = s2[0]
                        DebugLog.e("onLoadResource is : ${isApiCall}")
                        if (requestUrl.contains("access_token") && !isApiCall) {
                            MyPreference.setValueBoolean(PrefKey.ISLOGIN, true)
                            MyPreference.setValueBoolean(PrefKey.IS_SIGNIN, false)
                            MyPreference.setValueString(PrefKey.ACCESS_TOKEN, finalToken)
                            isApiCall = true

                            authorizationViewModel.callsetToken(this@AuthorizationActivity)
                        }
                    }
                }
            }
        }

    }


    private fun observeData() {
        authorizationViewModel.tokenExpiredCall.observe(this) {
            if (it) {
                MyPreference.clearAllData()
                val myIntent = Intent(this, LoginActivity::class.java)
                myIntent.putExtra("logoutFromApp", true)
                startActivity(myIntent)
                finishAffinity()
            }
        }
    }


}