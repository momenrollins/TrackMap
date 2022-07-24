package com.houseofdevelopment.gps.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.houseofdevelopment.gps.network.client.ApiClient
import com.houseofdevelopment.gps.network.client.ApiStatus
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.utils.Constants
import com.houseofdevelopment.gps.utils.DebugLog
import com.houseofdevelopment.gps.utils.NetworkUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject

abstract class BaseViewModel : ViewModel() {

    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()
    // the Coroutine runs using the Main (UI) dispatcher
    val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    // Instance on Retrofit
    var client = ApiClient.getApiClient()
    var clientMain = ApiClient.getApiClientAddress()
    var _status = MutableLiveData<ApiStatus>()
    // The external immutable LiveData for the request status
    val status: LiveData<ApiStatus>
        get() = _status
    var errorMsg: String = ""
    fun callApiForSetLocale(result: Long, flag : Int) {
        coroutineScope.launch {
           val  jsonObject = JSONObject()
            try {
                jsonObject.put("tzOffset", result)
                jsonObject.put("language", "en")
                jsonObject.put("flags", flag)
                jsonObject.put("formatDate", "%m-%E-%Y %H:%M:%S")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val body = HashMap<String, String>()
            body["svc"] = "render/set_locale"
            body["params"] = jsonObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()
            _status.value = ApiStatus.LOADING
            DebugLog.d("RequestBody set_locale=$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getSetLocale(body)
                    DebugLog.d("ResponseBody set_locale = $response")
                    _status.value = ApiStatus.DONE
                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }
}