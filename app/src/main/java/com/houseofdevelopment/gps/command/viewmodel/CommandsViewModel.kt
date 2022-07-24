package com.houseofdevelopment.gps.command.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.houseofdevelopment.gps.DataValues
import com.houseofdevelopment.gps.base.AppBase
import com.houseofdevelopment.gps.base.BaseViewModel
import com.houseofdevelopment.gps.command.model.CommandDataRootGps3
import com.houseofdevelopment.gps.command.model.CommandResponseModel
import com.houseofdevelopment.gps.network.client.ApiStatus
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.utils.Constants
import com.houseofdevelopment.gps.utils.DebugLog
import com.houseofdevelopment.gps.utils.NetworkUtil
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.awaitResponse

class CommandsViewModel : BaseViewModel() {
    val commDataModel = MutableLiveData<CommandResponseModel>()
    val commDataModelGps3 = MutableLiveData<CommandDataRootGps3>()

    companion object {
        private const val TAG = "CommandsViewModel"
    }

    fun getCommands(carId: String) {
        viewModelScope.launch {
            val jsonObj = JSONObject()
            try {

                jsonObj.put("id", carId)
                jsonObj.put("flags", "512")
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "core/search_item"
            body["params"] = jsonObj.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            DebugLog.d("RequestBody =$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getCommands(body)
                    response.let {

                        commDataModel.postValue(response)
                    }

                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    fun getCommandsGps3() {
        viewModelScope.launch {
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getCommandsGps3(
                        "{\"service\":\"get_commands\",\"api_key\":\"" +
                                DataValues.apiKey +
                                "\"}"
                    ).awaitResponse()
                    commDataModelGps3.postValue(response.body()!!)
                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }
}
