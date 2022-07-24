package com.houseofdevelopment.gps.notification.viewModel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.houseofdevelopment.gps.BuildConfig
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.base.AppBase
import com.houseofdevelopment.gps.base.BaseViewModel
import com.houseofdevelopment.gps.PostResponseModel
import com.houseofdevelopment.gps.network.client.ApiStatus
import com.houseofdevelopment.gps.notification.model.NotificationDataGps3
import com.houseofdevelopment.gps.notification.model.NotificationDataRootGps3
import com.houseofdevelopment.gps.notification.model.NotificationListData
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey
import com.houseofdevelopment.gps.utils.Constants
import com.houseofdevelopment.gps.utils.DebugLog
import com.houseofdevelopment.gps.utils.NetworkUtil
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationViewModel : BaseViewModel() {

    private lateinit var _jsonObject: JSONObject
    var notiNameList = MutableLiveData<ArrayList<NotificationListData.FirstObj>>()
    var notiNameListGps3 = MutableLiveData<ArrayList<NotificationDataGps3>>()
    var isNotificationONCreated = MutableLiveData<Boolean>()

    fun callApiForNotificationListData() {

        coroutineScope.launch {
            try {
                _jsonObject = JSONObject()
                _jsonObject.put("itemId", MyPreference.getValueInt(Constants.USER_BACT_ID, 0))
                _jsonObject.put("col", "")

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "resource/get_notification_data"
            body["params"] = _jsonObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            _status.value = ApiStatus.LOADING

            DebugLog.d("RequestBody get_notification_list=$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
//                    val response = client.getNotificationList(body)
                    val response = client.getNotificationList(body)

                    val jsonArray = JSONArray(response.string())

                    val nameList = ArrayList<NotificationListData.FirstObj>()
                    var jsonObject: JSONObject

                    for (n in 0 until jsonArray.length()) {
                        jsonObject = jsonArray.getJSONObject(n)

                        val notiObj = NotificationListData().FirstObj()
                        notiObj.id = jsonObject.getString("id")
                        notiObj.n = jsonObject.getString("n")
                        notiObj.fl = jsonObject.getInt("fl")
                        DebugLog.d("ResponseBody get notiObj.fl = ${notiObj.fl}")
                        nameList.add(notiObj)
                    }
                    notiNameList.postValue(nameList)

                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    fun callApiForNotificationListDataGps3() {
        coroutineScope.launch {
            _status.value = ApiStatus.LOADING

            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    val response = client.getNotificationListGps3(
                        "{\"service\":\"get_events\",\"api_key\":\"" +
                                MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "") +
                                "\"}"
                    )
                    Log.d(
                        TAG,
                        "callApiForNotificationListDataGps3: " + "{\"service\":\"get_events\",\"api_key\":\"" +
                                MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "") +
                                "\"}"
                    )
                    response.enqueue(object : Callback<NotificationDataRootGps3> {
                        override fun onResponse(
                            call: Call<NotificationDataRootGps3>,
                            response: Response<NotificationDataRootGps3>
                        ) {
                            if (!response.isSuccessful) {
                                Toast.makeText(AppBase.instance, "no data", Toast.LENGTH_SHORT)
                                    .show()
                                return
                            } else {
                                val notificationDataRoot = response.body()!!
                                Log.d(TAG, "onResponse: st ${notificationDataRoot.status}")
                                if (notificationDataRoot.status) {
                                    notiNameListGps3.postValue(notificationDataRoot.data)
                                    _status.value = ApiStatus.SUCCESSFUL

                                } else {
                                    Log.d(TAG, "onResponse: st ${notificationDataRoot.msg}")
                                    errorMsg = notificationDataRoot.msg
                                    _status.value = ApiStatus.ERROR
                                }
                            }
                        }

                        override fun onFailure(call: Call<NotificationDataRootGps3>, t: Throwable) {
                            Log.d(TAG, "onFailure: ${t.message}")
                        }
                    })
                } catch (error: Throwable) {
                    Log.d(TAG, "callApiForNotificationListDataGps3: ${error.message}")
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    fun callApiForSwitchOnOff(selId: String, status: Int) {

        coroutineScope.launch {
            try {
                _jsonObject = JSONObject()
                _jsonObject.put("id", selId)
                _jsonObject.put("itemId", MyPreference.getValueInt(Constants.USER_BACT_ID, 0))
                _jsonObject.put("callMode", "enable")
                _jsonObject.put("e", status)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "resource/update_notification"
            body["params"] = _jsonObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            _status.value = ApiStatus.LOADING

            DebugLog.d("RequestBody get_notification_list=$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
//                    val response = client.getNotificationList(body)
                    val response = client.getSwitchOnOffData(body)
                    DebugLog.d("ResponseBody getSwitchOnOff = $response")
                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    fun callApiForSwitchOnOffGps3(selId: String, status: Boolean) {
        coroutineScope.launch {
            _status.value = ApiStatus.LOADING
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getSwitchOnOffDataGps3(
                        "{\"service\":\"update_event\",\"api_key\":\"" +
                                MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "") +
                                "\",\"event_id\":" +
                                selId +
                                ",\"active\":\"" +
                                !status +
                                "\"}"
                    )
                    Log.d(
                        TAG,
                        "callApiForSwitchOnOffGps3: " + "{\"service\":\"update_event\",\"api_key\":\"" +
                                MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "") +
                                "\",\"event_id\":" +
                                selId +
                                ",\"active\":\"" +
                                !status +
                                "\"}"
                    )
                    response.enqueue(object : Callback<PostResponseModel> {
                        override fun onResponse(
                            call: Call<PostResponseModel>,
                            response: Response<PostResponseModel>
                        ) {
                            if (!response.isSuccessful) {
                                Toast.makeText(AppBase.instance, "no data", Toast.LENGTH_SHORT)
                                    .show()
                                return
                            } else {
                                val postResponseModel = response.body()!!
                                Log.d(TAG, "onResponse: sts ${postResponseModel.status}")
                                if (postResponseModel.status) {
                                    _status.value = ApiStatus.SUCCESSFUL

                                } else {
                                    Log.d(TAG, "onResponse: st2 ${postResponseModel.msg}")
                                    errorMsg = postResponseModel.msg
                                    _status.value = ApiStatus.ERROR
                                }
                            }
                        }

                        override fun onFailure(call: Call<PostResponseModel>, t: Throwable) {
                            Log.d(TAG, "onFailure: ${t.message}")
                        }
                    })

                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    Log.d(TAG, "callApiForSwitchOnOffGps3: ${error.message}")
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    /**
     *  Pass notification data for Start Engine : ON
     *  body: {"lower_bound":1,"upper_bound":1}
     */
    fun callApiForStartEngineONNotification(
        unitArray: JSONArray,
        name: String,
        context: Context
    ) {

        lateinit var schObject: JSONObject
        lateinit var trgObject: JSONObject
        lateinit var _pObject: JSONObject
        lateinit var _actArray: JSONArray

        val baseUrl = BuildConfig.BaseURLBrainvire

        coroutineScope.launch {
            try {
                schObject = JSONObject()
                schObject.put("f1", 0)
                schObject.put("f2", 0)
                schObject.put("t1", 0)
                schObject.put("t2", 0)
                schObject.put("m", 0)
                schObject.put("y", 0)
                schObject.put("w", 0)
                schObject.put("f1", 0)

                _pObject = JSONObject()
                _pObject.put("sensor_type", "engine operation")
                _pObject.put("sensor_name_mask", "*")
                _pObject.put("lower_bound", 0)
                _pObject.put("upper_bound", 0)
                _pObject.put("prev_msg_diff", 0)
                _pObject.put("merge", 1)
                _pObject.put("type", 1)

                trgObject = JSONObject()
                trgObject.put("t", "sensor_value")
                trgObject.put("p", _pObject)

                val _actPObject = JSONObject()
//                _actPObject.put("url",URLEncoder.encode("https://tawasoul.brainvire.dev/front/api/v1/send_push?user=brainvire&msg="))
//                _actPObject.put("url","https://tawasoul.brainvire.dev/front/api/v1/send_push?user=brainvire&msg=")
                var username = MyPreference.getValueString(Constants.USER_NAME, "")
                username = username!!.replace(" ", "%20")
                // _actPObject.put("url", "https://tawasoul.brainvire.dev/front/api/v1/send_push?user=$username&msg=")
                // _actPObject.put("url", "https://tawasoul.brainvire.dev/front/api/v1/send_push?user=$username&msg=")
                _actPObject.put("url", "${baseUrl}send_push?user=$username&msg=")
                _actPObject.put("get", 1)

                val _actPObject1 = JSONObject()
                _actPObject1.put("flags", 1)

                val _actObject1 = JSONObject()
                _actObject1.put("t", "event")
                _actObject1.put("p", _actPObject1)

                val _actObject = JSONObject()
                _actObject.put("t", "push_messages")
                _actObject.put("p", _actPObject)

                _actArray = JSONArray()
                _actArray.put(_actObject)
                _actArray.put(_actObject1)

                _jsonObject = JSONObject()
                _jsonObject.put("n", name)
                _jsonObject.put("ta", 1603746000)
                _jsonObject.put("td", 0)
                _jsonObject.put("tz", 134228528)
                _jsonObject.put("la", "en")
                _jsonObject.put("ma", 0)
                _jsonObject.put("sch", schObject)
                _jsonObject.put("ctrl_sch", schObject)
                _jsonObject.put("un", unitArray)
                _jsonObject.put("trg", trgObject)
                _jsonObject.put("d", "")
                _jsonObject.put("act", _actArray)
                _jsonObject.put(
                    "txt",
                    context.getString(R.string.notification_data_for_start_engine_on)
                )
                _jsonObject.put("fl", 0)
                _jsonObject.put("mast", 0)
                _jsonObject.put("mpst", 0)
                _jsonObject.put("cdt", 0)
                _jsonObject.put("mmtd", 864000)
                _jsonObject.put("cp", 0)
                _jsonObject.put("id", 0)
                _jsonObject.put(
                    "itemId",
                    MyPreference.getValueInt(Constants.USER_BACT_ID, 0).toLong()
                )
                _jsonObject.put("callMode", "create")

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "resource/update_notification"
            body["params"] = _jsonObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            _status.value = ApiStatus.LOADING

            DebugLog.d("RequestBody get_notification_list Start= ${_jsonObject.toString()}")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getStartEngineONData(body)
                    val res = response.string()
                    if (res.contains("error\":7")) {
                        Toast.makeText(
                            AppBase.instance,
                            AppBase.instance.getString(R.string.you_dont_have_access_to_this_feature),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val jsonArray = JSONArray(res)
                        DebugLog.d("ResponseBody get_notification_list = $jsonArray")
                        if (jsonArray.length() > 0) {
                            isNotificationONCreated.postValue(true)
                        } else {
                            _status.value = ApiStatus.ERROR
                        }
                    }

                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    DebugLog.e("ResponseBody get_notification_list CATCH = ${error.message}")

                    errorMsg = error.toString()
                    DebugLog.d("RequestBody get_notification_list errorMsg = $errorMsg")

                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }



    fun callApiForSensorWeightNotification(
        unitArray: JSONArray,
        name: String,
        from: String,
        to: String,
        typeWeight: Int,
        context: Context
    ) {

        Log.d(TAG, "callApiForSensorWeightNotification:from ${from},${to}")
        lateinit var schObject: JSONObject
        lateinit var trgObject: JSONObject
        lateinit var _pObject: JSONObject
        lateinit var _actArray: JSONArray

        val baseUrl = BuildConfig.BaseURLBrainvire

        coroutineScope.launch {
            try {
                schObject = JSONObject()
                schObject.put("f1", 0)
                schObject.put("f2", 0)
                schObject.put("t1", 0)
                schObject.put("t2", 0)
                schObject.put("m", 0)
                schObject.put("y", 0)
                schObject.put("w", 0)
                schObject.put("f1", 0)

                _pObject = JSONObject()
                _pObject.put("sensor_type", "weight")
                _pObject.put("sensor_name_mask", "*")
                _pObject.put("lower_bound", from)
                _pObject.put("upper_bound", to)
                _pObject.put("prev_msg_diff", 0)
                _pObject.put("merge", 1)
                _pObject.put("type", typeWeight)

                trgObject = JSONObject()
                trgObject.put("t", "sensor_value")
                trgObject.put("p", _pObject)

                val _actPObject = JSONObject()
//                _actPObject.put("url",URLEncoder.encode("https://tawasoul.brainvire.dev/front/api/v1/send_push?user=brainvire&msg="))
//                _actPObject.put("url","https://tawasoul.brainvire.dev/front/api/v1/send_push?user=brainvire&msg=")
                var username = MyPreference.getValueString(Constants.USER_NAME, "")
                username = username!!.replace(" ", "%20")
                // _actPObject.put("url", "https://tawasoul.brainvire.dev/front/api/v1/send_push?user=$username&msg=")
                // _actPObject.put("url", "https://tawasoul.brainvire.dev/front/api/v1/send_push?user=$username&msg=")
                _actPObject.put("url", "${baseUrl}send_push?user=$username&msg=")
                _actPObject.put("get", 1)

                val _actPObject1 = JSONObject()
                _actPObject1.put("flags", 1)

                val _actObject1 = JSONObject()
                _actObject1.put("t", "event")
                _actObject1.put("p", _actPObject1)

                val _actObject = JSONObject()
                _actObject.put("t", "push_messages")
                _actObject.put("p", _actPObject)

                _actArray = JSONArray()
                _actArray.put(_actObject)
                _actArray.put(_actObject1)

                _jsonObject = JSONObject()
                _jsonObject.put("n", name)
                _jsonObject.put("ta", 1603746000)
                _jsonObject.put("td", 0)
                _jsonObject.put("tz", 134228528)
                _jsonObject.put("la", "en")
                _jsonObject.put("ma", 0)
                _jsonObject.put("sch", schObject)
                _jsonObject.put("ctrl_sch", schObject)
                _jsonObject.put("un", unitArray)
                _jsonObject.put("trg", trgObject)
                _jsonObject.put("d", "")
                _jsonObject.put("act", _actArray)
                _jsonObject.put(
                    "txt",
                    context.getString(R.string.api_call_for_weight_notification_test)
                )
                _jsonObject.put("fl", 0)
                _jsonObject.put("mast", 0)
                _jsonObject.put("mpst", 0)
                _jsonObject.put("cdt", 0)
                _jsonObject.put("mmtd", 864000)
                _jsonObject.put("cp", 0)
                _jsonObject.put("id", 0)
                _jsonObject.put(
                    "itemId",
                    MyPreference.getValueInt(Constants.USER_BACT_ID, 0).toLong()
                )
                _jsonObject.put("callMode", "create")

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "resource/update_notification"
            body["params"] = _jsonObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            _status.value = ApiStatus.LOADING

            DebugLog.d("RequestBody get_notification_list Start= ${_jsonObject.toString()}")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getStartEngineONData(body)
                    val res = response.string()
                    if (res.contains("error\":7")) {
                        Toast.makeText(
                            AppBase.instance,
                            AppBase.instance.getString(R.string.you_dont_have_access_to_this_feature),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val jsonArray = JSONArray(res)
                        DebugLog.d("ResponseBody get_notification_list = $jsonArray")
                        if (jsonArray.length() > 0) {
                            isNotificationONCreated.postValue(true)
                        } else {
                            _status.value = ApiStatus.ERROR
                        }
                    }

                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    DebugLog.e("ResponseBody get_notification_list CATCH = ${error.message}")

                    errorMsg = error.toString()
                    DebugLog.d("RequestBody get_notification_list errorMsg = $errorMsg")

                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }



    /**
     *  Pass notification data for Start Engine : OFF
     *  body: {"lower_bound":0,"upper_bound":0}
     */
    fun callApiForStartEngineOFFNotification(
        unitArray: JSONArray,
        name: String,
        context: Context
    ) {

        lateinit var _schObject: JSONObject
        lateinit var _trgObject: JSONObject
        lateinit var _pObject: JSONObject
        lateinit var _actArray: JSONArray

        val baseUrl = BuildConfig.BaseURLBrainvire

        coroutineScope.launch {
            try {
                _schObject = JSONObject()
                _schObject.put("f1", 0)
                _schObject.put("f2", 0)
                _schObject.put("t1", 0)
                _schObject.put("t2", 0)
                _schObject.put("m", 0)
                _schObject.put("y", 0)
                _schObject.put("w", 0)
                _schObject.put("f1", 0)

                _pObject = JSONObject()
                _pObject.put("sensor_type", "engine operation")
                _pObject.put("sensor_name_mask", "*")
                _pObject.put("lower_bound", 0)
                _pObject.put("upper_bound", 0)
                _pObject.put("prev_msg_diff", 0)
                _pObject.put("merge", 1)
                _pObject.put("type", 0)

                _trgObject = JSONObject()
                _trgObject.put("t", "sensor_value")
                _trgObject.put("p", _pObject)

                val _actPObject = JSONObject()
//                _actPObject.put("url",URLEncoder.encode("https://tawasoul.brainvire.dev/front/api/v1/send_push?user=brainvire&msg="))
//                _actPObject.put("url","https://tawasoul.brainvire.dev/front/api/v1/send_push?user=brainvire&msg=")
                var username = MyPreference.getValueString(Constants.USER_NAME, "")
                username = username!!.replace(" ", "%20")
                // _actPObject.put("url", "https://tawasoul.brainvire.dev/front/api/v1/send_push?user=$username&msg=")
                _actPObject.put("url", "${baseUrl}send_push?user=$username&msg=")
                _actPObject.put("get", 1)

                val _actPObject1 = JSONObject()
                _actPObject1.put("flags", 1)

                val _actObject1 = JSONObject()
                _actObject1.put("t", "event")
                _actObject1.put("p", _actPObject1)

                val _actObject = JSONObject()
                _actObject.put("t", "push_messages")
                _actObject.put("p", _actPObject)

                _actArray = JSONArray()
                _actArray.put(_actObject)
                _actArray.put(_actObject1)

                _jsonObject = JSONObject()
                _jsonObject.put("n", name)
                _jsonObject.put("ta", 1603746000)
                _jsonObject.put("td", 0)
                _jsonObject.put("tz", 134228528)
                _jsonObject.put("la", "en")
                _jsonObject.put("ma", 0)
                _jsonObject.put("sch", _schObject)
                _jsonObject.put("ctrl_sch", _schObject)
                _jsonObject.put("un", unitArray)
                _jsonObject.put("trg", _trgObject)
                _jsonObject.put("d", "")
                _jsonObject.put("act", _actArray)
                _jsonObject.put(
                    "txt",
                    context.getString(R.string.notification_data_for_start_engine_off)
                )
                _jsonObject.put("fl", 0)
                _jsonObject.put("mast", 0)
                _jsonObject.put("mpst", 0)
                _jsonObject.put("cdt", 0)
                _jsonObject.put("mmtd", 864000)
                _jsonObject.put("cp", 0)
                _jsonObject.put("id", 0)
                _jsonObject.put(
                    "itemId",
                    MyPreference.getValueInt(Constants.USER_BACT_ID, 0).toLong()
                )
                _jsonObject.put("callMode", "create")

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "resource/update_notification"
            body["params"] = _jsonObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            _status.value = ApiStatus.LOADING

            DebugLog.d("RequestBody get_notification_list= startno $body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getStartEngineONData(body)
                    DebugLog.d("ResponseBody get_notification_list = $response")
                    val res = response.string()
                    if (res.contains("error\":7")) {
                        Toast.makeText(
                            AppBase.instance,
                            AppBase.instance.getString(R.string.you_dont_have_access_to_this_feature),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val jsonArray = JSONArray(res)

                        if (jsonArray.length() > 0) {
                            isNotificationONCreated.postValue(true)
                        } else {
                            Log.d("JSON response", "getting blank json array")
                        }
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
     *  API call for speed Notification test
     */
    fun callApiForSpeedNotificationEmail(
        unitArray: JSONArray,
        name: String,
        speedValue: Int,
        context: Context
    ) {

        lateinit var _schObject: JSONObject
        lateinit var _trgObject: JSONObject
        lateinit var _pObject: JSONObject
        lateinit var _actArray: JSONArray

        val baseUrl = BuildConfig.BaseURLBrainvire

        coroutineScope.launch {
            try {

                _schObject = JSONObject()
                _schObject.put("f1", 0)
                _schObject.put("f2", 0)
                _schObject.put("t1", 0)
                _schObject.put("t2", 0)
                _schObject.put("m", 0)
                _schObject.put("y", 0)
                _schObject.put("w", 0)
                _schObject.put("f1", 0)

                _pObject = JSONObject()
                _pObject.put("sensor_type", "")
                _pObject.put("sensor_name_mask", "")
                _pObject.put("lower_bound", 0)
                _pObject.put("upper_bound", 0)
                _pObject.put("merge", 0)
                _pObject.put("min_speed", 0)
                _pObject.put("max_speed", speedValue)
                _pObject.put("driver", 0)

                _trgObject = JSONObject()
                _trgObject.put("t", "speed")
                _trgObject.put("p", _pObject)

                val _actPObject = JSONObject()
//                _actPObject.put("url",URLEncoder.encode("https://tawasoul.brainvire.dev/front/api/v1/send_push?user=brainvire&msg="))
//                _actPObject.put("url","https://tawasoul.brainvire.dev/front/api/v1/send_push?user=brainvire&msg=")
                var username = MyPreference.getValueString(Constants.USER_NAME, "")
                username = username!!.replace(" ", "%20")
                // _actPObject.put("url", "https://tawasoul.brainvire.dev/front/api/v1/send_push?user=$username&msg=")
                _actPObject.put("url", "${baseUrl}send_push?user=$username&msg=")
                _actPObject.put("get", 1)

                val _actPObject1 = JSONObject()
                _actPObject1.put("flags", 1)

                val _actObject1 = JSONObject()
                _actObject1.put("t", "event")
                _actObject1.put("p", _actPObject1)

                val _actObject = JSONObject()
                _actObject.put("t", "push_messages")
                _actObject.put("p", _actPObject)

                _actArray = JSONArray()
                _actArray.put(_actObject)
                _actArray.put(_actObject1)

                _jsonObject = JSONObject()
                _jsonObject.put("n", name)
                _jsonObject.put("ta", 1603746000)
                _jsonObject.put("td", 0)
                _jsonObject.put("tz", 134228528)
                _jsonObject.put("la", "en")
                _jsonObject.put("ma", 0)
                _jsonObject.put("sch", _schObject)
                _jsonObject.put("ctrl_sch", _schObject)
                _jsonObject.put("un", unitArray)
                _jsonObject.put("trg", _trgObject)
                _jsonObject.put("d", "")
                _jsonObject.put("act", _actArray)
                _jsonObject.put(
                    "txt",
                    context.getString(R.string.api_call_for_speed_notification_test)
                )
                _jsonObject.put("fl", 0)
                _jsonObject.put("mast", 30)
                _jsonObject.put("mpst", 0)
                _jsonObject.put("cdt", 0)
                _jsonObject.put("mmtd", 864000)
                _jsonObject.put("cp", 0)
                _jsonObject.put("id", 0)
                _jsonObject.put(
                    "itemId",
                    MyPreference.getValueInt(Constants.USER_BACT_ID, 0).toLong()
                )
                _jsonObject.put("callMode", "create")

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "resource/update_notification"
            body["params"] = _jsonObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            _status.value = ApiStatus.LOADING

            DebugLog.d("RequestBody get_notification_list=$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getStartEngineONData(body)
                    DebugLog.d("ResponseBody get_notification_list = $response")
                    val res = response.string()
                    if (res.contains("error\":7")) {
                        Toast.makeText(
                            AppBase.instance,
                            AppBase.instance.getString(R.string.you_dont_have_access_to_this_feature),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val jsonArray = JSONArray(res)

                        if (jsonArray.length() > 0) {
                            isNotificationONCreated.postValue(true)
                        }
                    }

                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }




    fun callApiForConnectionLossNotificationEmail(
        unitArray: JSONArray,
        name: String,
        time: Int,
        context: Context
    ) {
        Log.d(
            "TAG",
            "onCreateView: comm:-- ConnectionTimeNotification== ${time}"
        )

        lateinit var _schObject: JSONObject
        lateinit var _trgObject: JSONObject
        lateinit var _pObject: JSONObject
        lateinit var _actArray: JSONArray

        val baseUrl = BuildConfig.BaseURLBrainvire

        coroutineScope.launch {
            try {

                _schObject = JSONObject()
                _schObject.put("f1", 0)
                _schObject.put("f2", 0)
                _schObject.put("t1", 0)
                _schObject.put("t2", 0)
                _schObject.put("m", 0)
                _schObject.put("y", 0)
                _schObject.put("w", 0)
                _schObject.put("f1", 0)

                _pObject = JSONObject()
                _pObject.put("time", time.toString())
                _pObject.put("type", "1")
                _pObject.put("include_lbs", 1)
                _pObject.put("check_restore", 1)

                _trgObject = JSONObject()
                _trgObject.put("t", "outage")
                _trgObject.put("p", _pObject)

                val _actPObject = JSONObject()
//                _actPObject.put("url",URLEncoder.encode("https://tawasoul.brainvire.dev/front/api/v1/send_push?user=brainvire&msg="))
//                _actPObject.put("url","https://tawasoul.brainvire.dev/front/api/v1/send_push?user=brainvire&msg=")
                var username = MyPreference.getValueString(Constants.USER_NAME, "")
                username = username!!.replace(" ", "%20")
                // _actPObject.put("url", "https://tawasoul.brainvire.dev/front/api/v1/send_push?user=$username&msg=")
                _actPObject.put("url", "${baseUrl}send_push?user=$username&msg=")
                _actPObject.put("get", 1)

                val _actPObject1 = JSONObject()
                _actPObject1.put("flags", 1)

                val _actObject1 = JSONObject()
                _actObject1.put("t", "event")
                _actObject1.put("p", _actPObject1)

                val _actObject = JSONObject()
                _actObject.put("t", "push_messages")
                _actObject.put("p", _actPObject)

                _actArray = JSONArray()
                _actArray.put(_actObject)
                _actArray.put(_actObject1)

                _jsonObject = JSONObject()
                _jsonObject.put("n", name)
                _jsonObject.put("ta", 1603746000)
                _jsonObject.put("td", 0)
                _jsonObject.put("tz", 134228528)
                _jsonObject.put("la", "en")
                _jsonObject.put("ma", 0)
                _jsonObject.put("sch", _schObject)
                _jsonObject.put("ctrl_sch", _schObject)
                _jsonObject.put("un", unitArray)
                _jsonObject.put("trg", _trgObject)
                _jsonObject.put("d", "")
                _jsonObject.put("act", _actArray)
                _jsonObject.put(
                    "txt",
                    context.getString(R.string.api_call_for_connection_loss_notification_test)
                )
                _jsonObject.put("fl", 0)
                _jsonObject.put("mast", 30)
                _jsonObject.put("mpst", 0)
                _jsonObject.put("cdt", 0)
                _jsonObject.put("mmtd", 864000)
                _jsonObject.put("cp", 0)
                _jsonObject.put("id", 0)
                _jsonObject.put(
                    "itemId",
                    MyPreference.getValueInt(Constants.USER_BACT_ID, 0).toLong()
                )
                _jsonObject.put("callMode", "create")

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "resource/update_notification"
            body["params"] = _jsonObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            _status.value = ApiStatus.LOADING

            DebugLog.d("RequestBody get_notification_list=$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getStartEngineONData(body)
                    DebugLog.d("ResponseBody get_notification_list = $response")
                    val res = response.string()
                    if (res.contains("error\":7")) {
                        Toast.makeText(
                            AppBase.instance,
                            AppBase.instance.getString(R.string.you_dont_have_access_to_this_feature),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val jsonArray = JSONArray(res)

                        if (jsonArray.length() > 0) {
                            isNotificationONCreated.postValue(true)
                        }
                    }

                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }


    private val TAG = "NotificationViewModel"
    fun callApiForNotificationEmailGps3(map: MutableMap<String, Any>) {

        val parms = JSONObject(map as Map<*, *>).toString()
        Log.d(TAG, "callApiForSpeedNotificationEmailGps3: $parms ")
        coroutineScope.launch {
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                val call: Call<PostResponseModel> = client.CreateNotificationGps3(parms)!!
                call.enqueue(object : Callback<PostResponseModel> {
                    override fun onResponse(
                        call: Call<PostResponseModel>,
                        response: Response<PostResponseModel>
                    ) {
                        if (!response.isSuccessful) {
                            return
                        }
                        if (response.body()!!.status)
                            isNotificationONCreated.postValue(true)

                    }

                    override fun onFailure(call: Call<PostResponseModel>, t: Throwable) {
                        Log.d(TAG, "onFailure: ${t.message}")
                    }

                })
            }
        }

    }


    /**
     *  API call for Idle-time Notification test
     */
    fun callApiForIdleTimeNotification(
        unitArray: JSONArray,
        name: String,
        idleTime: Int,
        context: Context
    ) {

        lateinit var _schObject: JSONObject
        lateinit var _trgObject: JSONObject
        lateinit var _pObject: JSONObject
        lateinit var _actArray: JSONArray

        val baseUrl = BuildConfig.BaseURLBrainvire
        coroutineScope.launch {
            try {

                _schObject = JSONObject()
                _schObject.put("f1", 0)
                _schObject.put("f2", 0)
                _schObject.put("t1", 0)
                _schObject.put("t2", 0)
                _schObject.put("m", 0)
                _schObject.put("y", 0)
                _schObject.put("w", 0)
                _schObject.put("f1", 0)

                _pObject = JSONObject()
                _pObject.put("sensor_type", "engine operation")
                _pObject.put("sensor_name_mask", "*")
                _pObject.put("lower_bound", 0)
                _pObject.put("upper_bound", 0)
                _pObject.put("merge", 0)
                _pObject.put("min_speed", 1)
                _pObject.put("max_speed", 1)
                _pObject.put("driver", 0)
                _pObject.put("min_idle_time", idleTime)
                _pObject.put("reversed", 1)

                _trgObject = JSONObject()
                _trgObject.put("t", "speed")
                _trgObject.put("p", _pObject)

                val _actPObject = JSONObject()
//                _actPObject.put("url",URLEncoder.encode("https://tawasoul.brainvire.dev/front/api/v1/send_push?user=brainvire&msg="))
                var username = MyPreference.getValueString(Constants.USER_NAME, "")
                username = username!!.replace(" ", "%20")
                // _actPObject.put("url", "https://tawasoul.brainvire.dev/front/api/v1/send_push?user=$username&msg=")
                _actPObject.put("url", "${baseUrl}send_push?user=$username&msg=")
                _actPObject.put("get", 1)

                val _actPObject1 = JSONObject()
                _actPObject1.put("flags", 1)

                val _actObject1 = JSONObject()
                _actObject1.put("t", "event")
                _actObject1.put("p", _actPObject1)

                val _actObject = JSONObject()
                _actObject.put("t", "push_messages")
                _actObject.put("p", _actPObject)

                _actArray = JSONArray()
                _actArray.put(_actObject)
                _actArray.put(_actObject1)

                _jsonObject = JSONObject()
                _jsonObject.put("n", name)
                _jsonObject.put("ta", 1603746000)
                _jsonObject.put("td", 0)
                _jsonObject.put("tz", 134228528)
                _jsonObject.put("la", "en")
                _jsonObject.put("ma", 0)
                _jsonObject.put("sch", _schObject)
                _jsonObject.put("ctrl_sch", _schObject)
                _jsonObject.put("un", unitArray)
                _jsonObject.put("trg", _trgObject)
                _jsonObject.put("d", "")
                _jsonObject.put("act", _actArray)
                _jsonObject.put(
                    "txt",
                    context.getString(R.string.api_call_for_idle_time_notification_test)
                )
                _jsonObject.put("fl", 0)
                _jsonObject.put("mast", 0)
                _jsonObject.put("mpst", 0)
                _jsonObject.put("cdt", 0)
                _jsonObject.put("mmtd", 864000)
                _jsonObject.put("cp", 0)
                _jsonObject.put("id", 0)
                _jsonObject.put(
                    "itemId",
                    MyPreference.getValueInt(Constants.USER_BACT_ID, 0).toLong()
                )
                _jsonObject.put("callMode", "create")

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "resource/update_notification"
            body["params"] = _jsonObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            _status.value = ApiStatus.LOADING

            DebugLog.d("RequestBody get_notification_list=$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getStartEngineONData(body)
                    DebugLog.d("ResponseBody get_notification_list = $response")
                    val res = response.string()
                    if (res.contains("error\":7")) {
                        Toast.makeText(
                            AppBase.instance,
                            AppBase.instance.getString(R.string.you_dont_have_access_to_this_feature),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val jsonArray = JSONArray(res)
                        if (jsonArray.length() > 0) {
                            isNotificationONCreated.postValue(true)
                        }
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
     *  API call for GeoZone In notification
     */
    fun callApiForGeoZoneInNotification(
        unitArray: JSONArray,
        name: String,
        sValue: String,
        context: Context
    ) {

        val baseUrl = BuildConfig.BaseURLBrainvire

        lateinit var _schObject: JSONObject
        lateinit var _trgObject: JSONObject
        lateinit var _pObject: JSONObject
        lateinit var _actArray: JSONArray

        coroutineScope.launch {
            try {

                _schObject = JSONObject()
                _schObject.put("f1", 0)
                _schObject.put("f2", 0)
                _schObject.put("t1", 0)
                _schObject.put("t2", 0)
                _schObject.put("m", 0)
                _schObject.put("y", 0)
                _schObject.put("w", 0)
                _schObject.put("f1", 0)

                _pObject = JSONObject()
                _pObject.put("sensor_type", "")
                _pObject.put("sensor_name_mask", "")
                _pObject.put("lower_bound", 0)
                _pObject.put("upper_bound", 0)
                _pObject.put("merge", 0)
                _pObject.put("min_speed", 0)
                _pObject.put("max_speed", 0)
                _pObject.put("lo", "OR")
                _pObject.put("type", 0)
                _pObject.put("geozone_ids", sValue)

                _trgObject = JSONObject()
                _trgObject.put("t", "geozone")
                _trgObject.put("p", _pObject)

                val _actPObject = JSONObject()
//                _actPObject.put("url",URLEncoder.encode("https://tawasoul.brainvire.dev/front/api/v1/send_push?user=brainvire&msg="))
//                _actPObject.put("url","https://tawasoul.brainvire.dev/front/api/v1/send_push?user=brainvire&msg=")
                var username = MyPreference.getValueString(Constants.USER_NAME, "")
                username = username!!.replace(" ", "%20")
                // _actPObject.put("url", "https://tawasoul.brainvire.dev/front/api/v1/send_push?user=$username&msg=")
                _actPObject.put("url", "${baseUrl}send_push?user=$username&msg=")
                _actPObject.put("get", 1)

                val _actPObject1 = JSONObject()
                _actPObject1.put("flags", 1)

                val _actObject1 = JSONObject()
                _actObject1.put("t", "event")
                _actObject1.put("p", _actPObject1)

                val _actObject = JSONObject()
                _actObject.put("t", "push_messages")
                _actObject.put("p", _actPObject)

                _actArray = JSONArray()
                _actArray.put(_actObject)
                _actArray.put(_actObject1)

                _jsonObject = JSONObject()
                _jsonObject.put("n", name)
                _jsonObject.put("ta", 1603746000)
                _jsonObject.put("td", 0)
                _jsonObject.put("tz", 134228528)
                _jsonObject.put("la", "en")
                _jsonObject.put("ma", 0)
                _jsonObject.put("sch", _schObject)
                _jsonObject.put("ctrl_sch", _schObject)
                _jsonObject.put("un", unitArray)
                _jsonObject.put("trg", _trgObject)
                _jsonObject.put("d", "")
                _jsonObject.put("act", _actArray)
                _jsonObject.put(
                    "txt",
                    context.getString(R.string.api_call_for_geozone_in_notification)
                )
                _jsonObject.put("fl", 0)
                _jsonObject.put("mast", 0)
                _jsonObject.put("mpst", 0)
                _jsonObject.put("cdt", 0)
                _jsonObject.put("mmtd", 864000)
                _jsonObject.put("cp", 0)
                _jsonObject.put("id", 0)
                _jsonObject.put(
                    "itemId",
                    MyPreference.getValueInt(Constants.USER_BACT_ID, 0).toLong()
                )
                _jsonObject.put("callMode", "create")

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "resource/update_notification"
            body["params"] = _jsonObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            _status.value = ApiStatus.LOADING

            DebugLog.d("RequestBody get_notification_list=$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getStartEngineONData(body)
                    DebugLog.d("ResponseBody get_notification_list = $response")
                    val res = response.string()
                    if (res.contains("error\":7")) {
                        Toast.makeText(
                            AppBase.instance,
                            AppBase.instance.getString(R.string.you_dont_have_access_to_this_feature),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val jsonArray = JSONArray(res)
                        if (jsonArray.length() > 0) {
                            isNotificationONCreated.postValue(true)
                        }
                    }

                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    Log.e(TAG, "callApiForGeoZoneInNotification: CATCH ${error.message}", )
                    errorMsg = error.toString()
                    Log.d(TAG, "callApiForGeoZoneInNotification:${errorMsg} ")
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    /**
     *  API call for GeoZone Out notification
     */
    fun callApiForGeoZoneOutNotification(
        unitArray: JSONArray,
        name: String,
        sValue: String,
        context: Context
    ) {

        lateinit var _schObject: JSONObject
        lateinit var _trgObject: JSONObject
        lateinit var _pObject: JSONObject
        lateinit var _actArray: JSONArray

        val baseUrl = BuildConfig.BaseURLBrainvire

        coroutineScope.launch {
            try {

                _schObject = JSONObject()
                _schObject.put("f1", 0)
                _schObject.put("f2", 0)
                _schObject.put("t1", 0)
                _schObject.put("t2", 0)
                _schObject.put("m", 0)
                _schObject.put("y", 0)
                _schObject.put("w", 0)
                _schObject.put("f1", 0)

                _pObject = JSONObject()
                _pObject.put("sensor_type", "")
                _pObject.put("sensor_name_mask", "")
                _pObject.put("lower_bound", 0)
                _pObject.put("upper_bound", 0)
                _pObject.put("merge", 0)
                _pObject.put("min_speed", 0)
                _pObject.put("max_speed", 0)
                _pObject.put("geozone_ids", sValue)
                _pObject.put("lo", "OR")
                _pObject.put("type", 1)

                _trgObject = JSONObject()
                _trgObject.put("t", "geozone")
                _trgObject.put("p", _pObject)

                val _actPObject = JSONObject()
//                _actPObject.put("url",URLEncoder.encode("https://tawasoul.brainvire.dev/front/api/v1/send_push?user=brainvire&msg="))
//                _actPObject.put("url","https://tawasoul.brainvire.dev/front/api/v1/send_push?user=brainvire&msg=")
                var username = MyPreference.getValueString(Constants.USER_NAME, "")
                username = username!!.replace(" ", "%20")
                // _actPObject.put("url", "https://tawasoul.brainvire.dev/front/api/v1/send_push?user=$username&msg=")
                _actPObject.put("url", "${baseUrl}send_push?user=$username&msg=")
                _actPObject.put("get", 1)

                val _actPObject1 = JSONObject()
                _actPObject1.put("flags", 1)

                val _actObject1 = JSONObject()
                _actObject1.put("t", "event")
                _actObject1.put("p", _actPObject1)

                val _actObject = JSONObject()
                _actObject.put("t", "push_messages")
                _actObject.put("p", _actPObject)

                _actArray = JSONArray()
                _actArray.put(_actObject)
                _actArray.put(_actObject1)

                _jsonObject = JSONObject()
                _jsonObject.put("n", name)
                _jsonObject.put("ta", 1603746000)
                _jsonObject.put("td", 0)
                _jsonObject.put("tz", 134228528)
                _jsonObject.put("la", "en")
                _jsonObject.put("ma", 0)
                _jsonObject.put("sch", _schObject)
                _jsonObject.put("ctrl_sch", _schObject)
                _jsonObject.put("un", unitArray)
                _jsonObject.put("trg", _trgObject)
                _jsonObject.put("d", "")
                _jsonObject.put("act", _actArray)
                _jsonObject.put(
                    "txt",
                    context.getString(R.string.api_call_for_geozone_out_notification)
                )
                _jsonObject.put("fl", 0)
                _jsonObject.put("mast", 0)
                _jsonObject.put("mpst", 0)
                _jsonObject.put("cdt", 0)
                _jsonObject.put("mmtd", 864000)
                _jsonObject.put("cp", 0)
                _jsonObject.put("id", 0)
                _jsonObject.put(
                    "itemId",
                    MyPreference.getValueInt(Constants.USER_BACT_ID, 0).toLong()
                )
                _jsonObject.put("callMode", "create")

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "resource/update_notification"
            body["params"] = _jsonObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            _status.value = ApiStatus.LOADING

            DebugLog.d("RequestBody get_notification_list=$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getStartEngineONData(body)
                    DebugLog.d("ResponseBody get_notification_list = $response")
                    val res = response.string()
                    if (res.contains("error\":7")) {
                        Toast.makeText(
                            AppBase.instance,
                            AppBase.instance.getString(R.string.you_dont_have_access_to_this_feature),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val jsonArray = JSONArray(res)
                        if (jsonArray.length() > 0) {
                            isNotificationONCreated.postValue(true)
                        }
                    }

                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }
}
