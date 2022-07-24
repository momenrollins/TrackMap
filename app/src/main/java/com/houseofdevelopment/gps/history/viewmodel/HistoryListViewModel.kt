package com.houseofdevelopment.gps.history.viewmodel

import android.util.Log

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.houseofdevelopment.gps.base.AppBase
import com.houseofdevelopment.gps.base.BaseViewModel
import com.houseofdevelopment.gps.history.model.RouteRootGps3
import com.houseofdevelopment.gps.track.model.MessageRoot
import com.houseofdevelopment.gps.track.model.MsgRootGps3
import com.houseofdevelopment.gps.history.model.TripDetails
import com.houseofdevelopment.gps.network.client.ApiStatus
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey
import com.houseofdevelopment.gps.utils.Constants
import com.houseofdevelopment.gps.utils.DebugLog
import com.houseofdevelopment.gps.utils.NetworkUtil
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.awaitResponse
import java.util.*
import kotlin.collections.ArrayList

class HistoryListViewModel : BaseViewModel() {

    private lateinit var _innerObject: JSONObject
    private lateinit var _jsonObject: JSONObject
    val _tripData = MutableLiveData<String>()
    var getTripData = MutableLiveData<List<TripDetails.FirstObj>>()
    var array: ArrayList<ArrayList<MessageRoot>> = ArrayList()
    var getListHistoryDataGps3 = MutableLiveData<MsgRootGps3>()
    var getListTrackDataGps3 = MutableLiveData<MsgRootGps3>()
    var getRoutesGps3 = MutableLiveData<RouteRootGps3>()
    var isNewData = MutableLiveData<String>()

    var ar: ArrayList<MessageRoot> = ArrayList()

    /**
     *  Get list of car history data
     */
    fun callApiForHistoryDetails(carId: String, fromInterval: Long, toInterval: Long) {

        Log.d(TAG, "callApiForHistoryDetails: $fromInterval $toInterval")
        var jsonArray: JSONArray
        viewModelScope.launch {
            try {
                _jsonObject = JSONObject()
                _jsonObject.put("type", "trips")
                _jsonObject.put("filter1", 0)

                jsonArray = JSONArray()
                jsonArray.put(_jsonObject)

                _innerObject = JSONObject()
                _innerObject.put("ivalType", 1)
                _innerObject.put("itemId", carId.toLong())
                _innerObject.put("timeFrom", fromInterval)
                _innerObject.put("timeTo", toInterval)
                _innerObject.put("detectors", jsonArray)


            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "events/load"
            body["params"] = _innerObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            _status.value = ApiStatus.LOADING

            DebugLog.d("RequestBody history=$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getExecuteReport(body)
                    response.body().let {
                        val jsonObject = JSONObject(response.body().toString())
                        val eventsObj = jsonObject.getJSONObject("events")
                        val tripsObj = eventsObj.getJSONObject("trips")
                        val value = tripsObj.getString("0")
                        _tripData.postValue(value)
                    }
                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    fun callApiForHistoryDetailsGps3(
        carId: String,
        fromInterval: String,
        toInterval: String

    ) {
        Log.d(TAG, "callApiForHistoryDetails: $carId,$fromInterval,$toInterval -- ")

        MyPreference.setValueString(PrefKey.dateSelected, toInterval)
        viewModelScope.launch {
//            _status.value = ApiStatus.LOADING
            loading(true)
            isNewData.postValue("false")

            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            }
            try {

                // this will run on a thread managed by Retrofit
                val response = client.getCarHistory(
                    "user",
                    MyPreference.getValueString(
                        PrefKey.ACCESS_TOKEN,
                        ""
                    )!!,
                    "OBJECT_GET_MESSAGES,$carId,$fromInterval,$toInterval"
                ).awaitResponse()
                if (!response.isSuccessful) {
                    Log.d(TAG, "onFailure: ${response.message()}")
                    isNewData.postValue("false")
                    getListHistoryDataGps3.postValue(MsgRootGps3())
                    loading(false)

                } else {
                    val msgRootGps3 = response.body()!!

                    loading(false)
                    getListTrackDataGps3.postValue(msgRootGps3)
                    getListHistoryDataGps3.postValue(msgRootGps3)
                    if (msgRootGps3.isStatus && msgRootGps3.message_root.size > 0)
                        isNewData.postValue("true")
                    else {
                        /*    Toast.makeText(
                                AppBase.instance,
                                AppBase.instance.getString(R.string.no_coordinates),
                                Toast.LENGTH_SHORT
                            ).show()*/
                        isNewData.postValue("no tracks")
                    }

                }
            } catch (error: Throwable) {
                loading(false)

                errorMsg = error.toString()
                Log.d(TAG, "onFailure: catch")
            }
        }
    }

    fun callApiForRouteGps3(
        imei: String, timeFrom: String, timeTo: String, stopDuration: Float
    ) {
        Log.d(TAG, "callApiForRouteGps3: $imei,$timeFrom,$timeTo")
        viewModelScope.launch {
//            _status.value = ApiStatus.LOADING
            loading(true)
            isNewData.postValue("false")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            }
            try {
                // this will run on a thread managed by Retrofit
                val response = client.getRouteGps3(
                    "user",
                    MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "")!!,
                    "OBJECT_GET_ROUTE,$imei,$timeFrom,$timeTo,$stopDuration"
                ).awaitResponse()
                if (response.isSuccessful) {
                    val routeRootGps3 = response.body()!!
                    loading(false)
                    getRoutesGps3.postValue(routeRootGps3)
                    if (routeRootGps3.route?.size!! > 0)
                        isNewData.postValue("true")
                    else {
                        isNewData.postValue("no tracks")
                    }
                } else {
                    Log.d(TAG, "onFailure: ${response.message()}")
                    isNewData.postValue("false")
                    getRoutesGps3.postValue(RouteRootGps3())
                    loading(false)
                }
            } catch (error: Throwable) {
                getRoutesGps3.postValue(RouteRootGps3())
                loading(false)
                errorMsg = error.toString()
                Log.d(TAG, "onFailure: ${error.message}")
            }
        }
    }

    private val TAG = "HistoryListViewModel"
    fun callApiForTripDetails(fromInterval: Long, toInterval: Long) {
        viewModelScope.launch {
            try {
                _jsonObject = JSONObject()
                _jsonObject.put("type", "trips")
                _jsonObject.put("timeFrom", fromInterval)
                _jsonObject.put("timeTo", toInterval)
                _jsonObject.put("detalization", 11)

                _innerObject = JSONObject()
                _innerObject.put("selector", _jsonObject)

            } catch (e: Exception) {
                e.printStackTrace()
            }
            val body = HashMap<String, String>()
            body["svc"] = "events/get"
            body["params"] = _innerObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()
            Log.d(TAG, "callApiForTripDetails: $body")

            _status.value = ApiStatus.LOADING

            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getTripsData(body)
                    if (response.trips != null && response.trips.firstObj != null) {
                        Log.d(TAG, "callApiForTripDetails: ${response.trips.firstObj.size}")
                        getTripData.postValue(response.trips.firstObj)
                    } else {
                        getTripData.postValue(ArrayList())
                    }

                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    DebugLog.e("+" + error.message.toString())
                    error.toString()
                    _status.value = ApiStatus.ERROR
                }

            }
        }
    }

    fun loading(isLoading: Boolean) {
        if (isLoading) _status.value = ApiStatus.LOADING
        else _status.value = ApiStatus.DONE
    }

}