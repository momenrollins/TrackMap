package com.houseofdevelopment.gps.changepassword

import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.base.AppBase
import com.houseofdevelopment.gps.base.BaseViewModel
import com.houseofdevelopment.gps.network.client.ApiStatus
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey
import com.houseofdevelopment.gps.utils.Constants
import com.houseofdevelopment.gps.utils.DebugLog
import com.houseofdevelopment.gps.utils.NetworkUtil
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.*

class ChangePasswordViewModel : BaseViewModel() {

    private lateinit var _jsonObject: JSONObject

    fun callApiForChangePassword(newPass: String, oldPass: String) {

        coroutineScope.launch {
            try {
                _jsonObject = JSONObject()
                _jsonObject.put("newPassword", newPass)
                _jsonObject.put("oldPassword", oldPass)
                _jsonObject.put("userId", MyPreference.getValueString(Constants.USER_ID, ""))

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "user/update_password"
            body["params"] = _jsonObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()
            _status.value = ApiStatus.LOADING
            DebugLog.d("RequestBody search_items=$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getChangePasswordData(body)
                    DebugLog.d("ResponseBody search_items = $response")
                    val jsonObject = JSONObject(response.string())
                    try {
                        val error = jsonObject.getInt("error")
                        DebugLog.d("metaObj = $error")
                        errorMsg = AppBase.instance.getString(R.string.pass_incorrect)
                        _status.value = ApiStatus.ERROR
                    } catch (e: Exception) {
                        _status.value = ApiStatus.DONE
                    }
                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    fun callApiForChangePasswordGp3(newPass: String, oldPass: String) {
        coroutineScope.launch {
            _status.value = ApiStatus.LOADING
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getChangePasswordDataGps3(
                        "{\"service\":\"change_password\",\"api_key\":\"" + MyPreference.getValueString(
                            PrefKey.ACCESS_TOKEN, ""
                        )!! + "\",\"old_password\":\"" + oldPass + "\",\"new_password\":" + newPass + "}"
                    ).awaitResponse()
                    if (!response.isSuccessful) {
                        errorMsg = response.code().toString()
                        _status.value = ApiStatus.ERROR
                    } else {
                        val changePasswordModel = response.body()!!
                        if (changePasswordModel.status) {
                            MyPreference.setValueString(
                                PrefKey.ACCESS_TOKEN,
                                changePasswordModel.api_key
                            )
                            _status.value = ApiStatus.SUCCESSFUL
                        } else {
                            errorMsg = changePasswordModel.msg
                            _status.value = ApiStatus.ERROR
                        }
                    }
                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }
}