package com.houseofdevelopment.gps.login.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.houseofdevelopment.gps.DataValues
import com.houseofdevelopment.gps.MainActivity
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.base.BaseActivity
import com.houseofdevelopment.gps.databinding.ActivityLoginBinding
import com.houseofdevelopment.gps.login.model.SignInRequest
import com.houseofdevelopment.gps.login.viewmodel.LoginViewModel
import com.houseofdevelopment.gps.network.ApiClientForBrainvire
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey
import com.houseofdevelopment.gps.utils.Utils

class LoginActivity : BaseActivity() {

    private lateinit var loginViewModel: LoginViewModel
    lateinit var binding: ActivityLoginBinding
    private lateinit var accountList: List<String>

    private lateinit var accountList1: List<String>
    private var serverName: String = ""
    var signInRequest: SignInRequest? = null
    private var isFirstTimeSignIn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        loginViewModel =
            ViewModelProvider(this@LoginActivity).get(LoginViewModel::class.java)
        accountList = arrayOf(
            getString(R.string.choose_srvr),
            getString(R.string.server1),
            getString(R.string.server2),
        ).toList()
        accountList1 = arrayOf(
            getString(R.string.choose_srvr),
            "http://gps.hod.sa/",
            "http://gps3.tawasolmap.com/"
        ).toList()
        val arrayAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line, accountList
        )
//        arrayAdapter.setDropDownViewResource(R.layout.simple_spinner_item)

        val materialDesignSpinner =
            findViewById<Spinner>(R.id.account_spinner)
        materialDesignSpinner.setAdapter(arrayAdapter)
        materialDesignSpinner?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    serverName = accountList1[position]

                }

            }
        val isLogout = intent.getBooleanExtra("logoutFromApp", false)
        if (isLogout) {
            Utils.deleteFile(this)
            MyPreference.setValueBoolean(PrefKey.IS_SIGNIN, false)
            MyPreference.setValueString(PrefKey.FCM_TOKEN, "")
            MyPreference.setValueBoolean(PrefKey.ISLOGIN, false)
        }

        if (intent.hasExtra("errorMessage") && intent.hasExtra("errorCode")) {
            val errorMessage = intent.getStringExtra("errorMessage")
            var errorCode = intent.getIntExtra("errorCode", 0)
            errorMessage?.let { Utils.showSnackbar(it, binding.root) }
        }

        isFirstTimeSignIn = MyPreference.getValueBoolean(PrefKey.ISLOGIN, false)

        if (isFirstTimeSignIn) {


            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            binding.btnGo.setOnClickListener {
                if (serverName.isNotEmpty()) {
                    if (serverName == getString(R.string.choose_srvr))
                        Toast.makeText(this, getString(R.string.pls_chs_srvr), Toast.LENGTH_SHORT)
                            .show()
                    else {
                        binding.constSignIn.visibility = View.GONE
                        MyPreference.setValueString(PrefKey.SELECTED_SERVER_DATA, serverName)
                        DataValues.serverData = MyPreference.getValueString(
                            PrefKey.SELECTED_SERVER_DATA, ""
                        )!!
                        ApiClientForBrainvire.getRetrofit()
                        if (serverName.contains("3")) {
                            intent = Intent(this@LoginActivity, LoginActivityGps3::class.java)
                            MyPreference.setValueString(
                                PrefKey.ACCESS_TOKEN,
                                "BB462D7262762046F4727D8C012F7509"
                            )
                        } else
                            intent = Intent(this@LoginActivity, AuthorizationActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    //callSignInMethod()

                } else {
                    Toast.makeText(this, "Please select account", Toast.LENGTH_SHORT).show()
//                  StyleableToast.makeText(this,"please Select Account", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnLogIn.setOnClickListener {
            val intent = Intent(this@LoginActivity, AuthorizationActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        serverName = accountList1[0]
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener {
            if (it.isSuccessful.not()) {
                return@OnCompleteListener
            }
            // Get new Instance ID token
            val token = it.result
            MyPreference.setValueString(PrefKey.FCM_TOKEN, token.toString())
            Log.d("TAG", "onResume: TOKEN::0 $token")
        })
    }
}
