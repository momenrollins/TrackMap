package com.trackmap.gps.login.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.trackmap.gps.DataValues.apiKey
import com.trackmap.gps.DataValues.serverData
import com.trackmap.gps.MainActivity
import com.trackmap.gps.R
import com.trackmap.gps.base.AppBase
import com.trackmap.gps.base.BaseActivity
import com.trackmap.gps.base.BaseViewModel
import com.trackmap.gps.login.ui.LoginActivity
import com.trackmap.gps.network.client.ApiStatus
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.MyPreference.getValueString
import com.trackmap.gps.preference.PrefKey
import com.trackmap.gps.utils.Constants
import com.trackmap.gps.utils.DebugLog
import com.trackmap.gps.utils.NetworkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject


class AuthorizationViewModel : BaseViewModel() {


    var tokenExpiredCall = MutableLiveData<Boolean>()

    private val TAG = "AuthorizationViewModel"
    fun callsetToken(requireContext: Context) {


        coroutineScope.launch {
            val jsonObj = JSONObject()
            try {

                jsonObj.put(
                    "token", getValueString(PrefKey.ACCESS_TOKEN, "").toString()
                )
                jsonObj.put("operateAs", "")
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "token/login"
            body["params"] = jsonObj.toString()
            _status.value = ApiStatus.LOADING
            DebugLog.d("RequestBody =$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.callSetToken(body)
                    if (response.eid != null && response.eid!!.isNotEmpty()) {
                        DebugLog.d("e_id" + response.eid)
                        launch(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext,
                                requireContext.getString(R.string.you_have_logged_in_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        apiKey = getValueString(PrefKey.ACCESS_TOKEN, "")!!
                        serverData = getValueString(PrefKey.SELECTED_SERVER_DATA, "")!!

                        MyPreference.setValueString(Constants.E_ID, response.eid)
                        // store value as constance globally accessed
                        MyPreference.setValueString(
                            Constants.USER_NAME,
                            response.user.nm
                        )
                        MyPreference.setValueString(
                            Constants.USER_ID,
                            response.user.id.toString()
                        )
                        Log.d(TAG, "callsetToken: UID ${response.user.id}")
                        MyPreference.setValueInt(
                            Constants.USER_BACT_ID,
                            response.user.bact
                        )
                        MyPreference.setValueBoolean(PrefKey.IS_SIGNIN, false)
                        val intent = Intent(requireContext, MainActivity::class.java)
                        intent.putExtra("IS_FROM_LOGIN", true)
                        requireContext.startActivity(intent)
                        (requireContext as BaseActivity).finish()
                    } else {
                        MyPreference.clearAllData()
                        val myIntent = Intent(requireContext, LoginActivity::class.java)
                        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        val errorMessage = "Access denied"
                        myIntent.putExtra("logoutFromApp", true)
                        myIntent.putExtra("errorMessage", errorMessage)
                        myIntent.putExtra("errorCode", 7)
                        requireContext.startActivity(myIntent)
                        (requireContext as BaseActivity).finish()
                    }
                } catch (error: Throwable) {
                    _status.value = ApiStatus.ERROR
                    DebugLog.d("error" + error.printStackTrace())
                    tokenExpiredCall.postValue(true)
                }
            }
        }
    }
}