package com.houseofdevelopment.gps.login.ui

import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.houseofdevelopment.gps.DataValues
import com.houseofdevelopment.gps.MainActivity
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.homemap.viewmodel.HomeMapViewModel
import com.houseofdevelopment.gps.network.client.ApiClient.getApiClient
import com.houseofdevelopment.gps.network.exeception.NoConnectionException
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey
import kotlinx.android.synthetic.main.activity_login_gps3.*
import kotlinx.android.synthetic.main.activity_login_gps3.et_password
import kotlinx.android.synthetic.main.activity_login_gps3.et_username
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.*
import java.util.*

class LoginActivityGps3 : AppCompatActivity() {
    var username: String? = null
    var password: String? = null
    lateinit var homeMapViewModel: HomeMapViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_gps3)
        homeMapViewModel = ViewModelProvider(this)[HomeMapViewModel::class.java]

        et_username.addTextChangedListener(textWatcher)
        et_password.addTextChangedListener(textWatcher)
        if (Locale.getDefault().displayLanguage.equals("العربية"))
            button_back.rotation = 180F


        button_back!!.setOnClickListener {
           onBackPressed()
        }
        button_enter!!.setOnClickListener {
            closeKeyboard()
            if (!isInternetConnected()) {
                Toast.makeText(
                    this,
                    getString(R.string.internet_connection_slow_or_disconnected),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            CoroutineScope(Job() + Dispatchers.Main).launch {
                button_enter!!.visibility = View.GONE
                login_pb!!.visibility = View.VISIBLE
                password = et_password!!.text.toString().trim { it <= ' ' }
                try {
                    val call = getApiClient().login(
                        "{\"service\":\"login\"," +
                                "\"password\":\"" + password +
                                "\",\"username\":\"" + username + "\"}"
                    ).awaitResponse()
                    if (call.isSuccessful) {
                        val loginModel = call.body()!!
                        if (loginModel.status) {
                            MyPreference.setValueBoolean(PrefKey.ISLOGIN, true)
                            MyPreference.setValueBoolean(PrefKey.IS_SIGNIN, false)
                            MyPreference.setValueString(PrefKey.USERNAME, username!!)
                            MyPreference.setValueString(
                                PrefKey.ACCESS_TOKEN, loginModel.data!!.api_key
                            )
                            MyPreference.setValueBoolean(PrefKey.IS_CEO_DATA, false)


                            DataValues.apiKey = loginModel.data!!.api_key
                            DataValues.username = username!!
                            DataValues.serverData = MyPreference.getValueString(
                                PrefKey.SELECTED_SERVER_DATA, ""
                            )!!


                            Toast.makeText(
                                this@LoginActivityGps3,
                                getString(R.string.you_have_logged_in_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                            sendToken(username!!)
                            startActivity(
                                Intent(
                                    this@LoginActivityGps3,
                                    MainActivity::class.java
                                )
                            )
                            finish()
                        } else {
                            Toast.makeText(
                                this@LoginActivityGps3,
                                getString(R.string.username_or_password_is_invalid),
                                Toast.LENGTH_SHORT
                            ).show()
                            button_enter!!.visibility = View.VISIBLE
                            login_pb!!.visibility = View.GONE
                        }
                    } else {
                        Toast.makeText(
                            this@LoginActivityGps3,
                            call.message(),
                            Toast.LENGTH_SHORT
                        ).show()
                        button_enter!!.visibility = View.VISIBLE
                        login_pb!!.visibility = View.GONE
                    }
                } catch (e: NoConnectionException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun isInternetConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private val TAG = "LoginActivityGps3"
    private fun sendToken(username: String) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.d(TAG, "sendToken: $token")
                homeMapViewModel.sendTokenGps3(username, token)
            })
    }

    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            username = et_username!!.text.toString().trim { it <= ' ' }
            password = et_password!!.text.toString().trim { it <= ' ' }
            button_enter!!.isEnabled = !username!!.isEmpty() && !password!!.isEmpty()
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(
            Intent(this, LoginActivity::class.java)
        )
        finish()
        overridePendingTransition(0, R.anim.activity_slide_finish_exit)
    }
}