package com.trackmap.gps.track.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.trackmap.gps.base.AppBase
import com.trackmap.gps.base.BaseViewModel
import com.trackmap.gps.network.client.ApiClient
import com.trackmap.gps.network.client.ApiStatus
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.track.model.VideoModel
import com.trackmap.gps.utils.Constants
import com.trackmap.gps.utils.DebugLog
import com.trackmap.gps.utils.NetworkUtil
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import java.util.*

class  TrackViewModel : BaseViewModel(), LifecycleObserver {

    private lateinit var _innerObject: JSONObject
    private lateinit var _jsonObject: JSONObject
    var tripData = MutableLiveData<JSONArray>()
    var unloadtripData = MutableLiveData<String>()
    var liveTripData = MutableLiveData<JSONArray>()
    var locationList = MutableLiveData<VideoModel>()

    fun clearData() {
        _status = MutableLiveData<ApiStatus>()
        tripData = MutableLiveData<JSONArray>()
        unloadtripData = MutableLiveData<String>()
    }

    /**
     *  Get list of car history data
     */
    fun callApiForUnloadHistoryDetails() {

        coroutineScope.launch {

            val body = HashMap<String, String>()
            body["svc"] = "events/unload"
            body["params"] = JSONObject().toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            _status.value = ApiStatus.LOADING

            DebugLog.d("RequestBody history=$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getHistoryData(
                        "events/unload", JSONObject().toString(), MyPreference.getValueString(
                            Constants.E_ID, ""
                        ).toString()
                    )
                    DebugLog.d("ResponseBody history = $response")
                    response.let {
                        //  val jsonObject = JSONObject(response.body().toString())

                        unloadtripData.postValue("")
                    }
                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    /**
     *  Get list of car history data
     */
    fun callApiForTrackHistoryDetails(carId: String, fromInterval: Long, toInterval: Long) {
         var jsonArray: JSONArray
        val jsonFinal = JSONObject()
        coroutineScope.launch {
            try {
                //// First Object
                val jsonObject = JSONObject()
                jsonObject.put("type", "trips")
                jsonObject.put("filter1", 0)
                jsonArray = JSONArray()
                jsonArray.put(jsonObject)
                val _jsonObject2 = JSONObject()
                _jsonObject2.put("ivalType", 1)
                _jsonObject2.put("itemId", carId.toLong())
                _jsonObject2.put("timeFrom", fromInterval)
                _jsonObject2.put("timeTo", toInterval)
                _jsonObject2.put("detectors", jsonArray)


                val json = JSONObject()
                json.put("svc", "events/load")
                json.put("params", _jsonObject2)

                // Second Object
                _jsonObject = JSONObject()
                _jsonObject.put("type", "*")
                _jsonObject.put("timeFrom", fromInterval)
                _jsonObject.put("timeTo", toInterval)
                _jsonObject.put("detalization", 11)

                _innerObject = JSONObject()
                _innerObject.put("selector", _jsonObject)


                val json1 = JSONObject()
                json1.put("svc", "events/get")
                json1.put("params", _innerObject)

                //// Third Object
                val thirdData = JSONObject()
                thirdData.put("expr", "trips{s<1}")
                thirdData.put("timeFrom", fromInterval)
                thirdData.put("timeTo", toInterval)
                thirdData.put("detalization", 11)

                val _innerObject1 = JSONObject()
                _innerObject1.put("selector", thirdData)

                val json2 = JSONObject()
                json2.put("svc", "events/get")
                json2.put("params", _innerObject1)
                // Final Array
                jsonArray = JSONArray()
                jsonArray.put(json)
                jsonArray.put(json1)
                jsonArray.put(json2)

                jsonFinal.put("params", jsonArray)
                jsonFinal.put("flags", 0)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            _status.value = ApiStatus.LOADING

            DebugLog.d("RequestBody Params=${jsonFinal}")
            DebugLog.d(
                "RequestBody E_ID=${
                    MyPreference.getValueString(Constants.E_ID, "").toString()
                }"
            )
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getHistoryData(
                        "core/batch", jsonFinal.toString(), MyPreference.getValueString(
                            Constants.E_ID, ""
                        ).toString()
                    )
                    val responseJson = JSONArray(response.string())
                    //DebugLog.d("ResponseBody history = $response")
                    responseJson.let {
                        tripData.postValue(responseJson)
                    }
                    _status.value = ApiStatus.DONE
                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }


    fun callApiForTrip(carId: String, fromInterval: Long, toInterval: Long) {

        DebugLog.e("TripData==carId$carId")
        DebugLog.e("TripData==fromInterval$fromInterval")
        DebugLog.e("TripData==toInterval$toInterval")
        val jsonFinal = JSONObject()
        coroutineScope.launch {
            try {
                //// First Object
                jsonFinal.put("itemId", carId.toLong())
                jsonFinal.put("timeFrom", fromInterval)
                jsonFinal.put("timeTo", toInterval)
                jsonFinal.put("flags", 1)
                jsonFinal.put("flagsMask", 65281)
                jsonFinal.put("loadCount", 5000)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            _status.value = ApiStatus.LOADING

            DebugLog.d("RequestBody TripData=$jsonFinal")
            DebugLog.d(
                "RequestBody TripData=${
                    MyPreference.getValueString(Constants.E_ID, "").toString()
                }"
            )
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                DebugLog.e("ResponseBody TripDatawwww111 ")

                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    DebugLog.e("ResponseBody TripDatawwww ")

                    // this will run on a thread managed by Retrofit
                    val response = client.getHistoryData(
                        "messages/load_interval", jsonFinal.toString(), MyPreference.getValueString(
                            Constants.E_ID, ""
                        ).toString()
                    )
                    val jsonObject = JSONObject(response.string())
                    val messages = jsonObject.getJSONArray("messages")
                    liveTripData.postValue(messages)
                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    DebugLog.e("ResponseBody TripDataxxxx = ${error}")

                    errorMsg = error.toString()
                }
            }
        }
    }
    private  val TAG = "TrackViewModel"
     fun callLocation() {

        val timeFrom = MyPreference.getValueString("timeFrom", "")
        val timeTo = MyPreference.getValueString("timeTo", "")
        val itemId = MyPreference.getValueString("itemId", "")
        val map = mutableMapOf<String, Any?>(
            "itemId" to itemId,
            "timeFrom" to timeFrom,
            "timeTo" to timeTo,
            "flags" to 0,
            "flagsMask" to 65280,
            "loadCount" to 10000
        )
        val sid = MyPreference.getValueString(Constants.E_ID, "").toString()
        val json = JSONObject(map as Map<*, *>).toString()

         Log.d(TAG, "callLocation: eventslog ${json}")
        val call: Call<VideoModel?>? =
            ApiClient.getApiClient().Video_MODEL_CALL("messages/load_interval", json, sid)

        call!!.enqueue(object : Callback<VideoModel?> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(
                call: Call<VideoModel?>,
                response: retrofit2.Response<VideoModel?>
            ) {

                if (!response.isSuccessful) {
                    return
                }
                val model: VideoModel? = response.body()
                locationList.postValue(model!!)

            }

            override fun onFailure(call: Call<VideoModel?>, t: Throwable) {
            }
        })
    }



    fun loading(isLoading: Boolean) {
        if (isLoading) _status.value = ApiStatus.LOADING
        else _status.value = ApiStatus.DONE
    }
}