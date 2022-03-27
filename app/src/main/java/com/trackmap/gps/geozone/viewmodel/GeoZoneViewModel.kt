package com.trackmap.gps.geozone.viewmodel

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.trackmap.gps.base.AppBase
import com.trackmap.gps.base.BaseViewModel
import com.trackmap.gps.geozone.model.GeoZoneDetailModel
import com.trackmap.gps.geozone.model.GeoZoneListModel
import com.trackmap.gps.geozone.model.GeoZonesModelRootGps3
import com.trackmap.gps.PostResponseModel
import com.trackmap.gps.R
import com.trackmap.gps.network.client.ApiStatus
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import com.trackmap.gps.utils.Constants
import com.trackmap.gps.utils.DebugLog
import com.trackmap.gps.utils.NetworkUtil
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.awaitResponse

class GeoZoneViewModel : BaseViewModel() {

    private lateinit var _jsonObject: JSONObject
    private lateinit var _innerObject: JSONObject
    var geoNameList = MutableLiveData<List<String>>()
    var geoColorList: MutableList<String>? = null
    var geoZoneList = MutableLiveData<List<GeoZoneListModel.Item>>()
    var geoZoneListGps3 = MutableLiveData<GeoZonesModelRootGps3>()
    var geoZoneDeletedGps3 = MutableLiveData<Boolean>()
    var geoZoneDetail = MutableLiveData<GeoZoneDetailModel>()
    var isGeoZoneAdded = MutableLiveData<Boolean>()
    var isGeoZoneDeleted = MutableLiveData<Boolean>()
    private var hs = mutableSetOf<String>()

    fun callApiForGeoZoneListData() {

        coroutineScope.launch {
            try {
                _jsonObject = JSONObject()
                _jsonObject.put("itemsType", "avl_resource")
                _jsonObject.put("propName", "zones_library")
                _jsonObject.put("propValueMask", "*")
                _jsonObject.put("sortType", "zones_library")
                _jsonObject.put("propType", "propitemname")

                _innerObject = JSONObject()
                _innerObject.put("spec", _jsonObject)
                _innerObject.put("force", 1)
                _innerObject.put("flags", 4097)
                _innerObject.put("from", 0)
                _innerObject.put("to", 1000)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "core/search_items"
            body["params"] = _innerObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            _status.value = ApiStatus.LOADING

            DebugLog.d("RequestBody geozone=$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getGeoZoneListData(body)
                    DebugLog.d("ResponseBody geozone = $response")

                    geoZoneList.postValue(response.items)

                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                    geoZoneList.postValue(null)

                }
            }
        }
    }

    fun callApiForGeoZoneDetailsData(selectedGeoZonId: String) {

        coroutineScope.launch {
            try {
                val jsonArray = JSONArray()
                jsonArray.put(selectedGeoZonId)
                _innerObject = JSONObject()
                _innerObject.put(
                    "itemId",
                    MyPreference.getValueInt(Constants.USER_BACT_ID, 0).toLong()
                )
                _innerObject.put("flags", 25)
                _innerObject.put("col", jsonArray)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "resource/get_zone_data"
            body["params"] = _innerObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            _status.value = ApiStatus.LOADING

            DebugLog.d("RequestBody geozoneDetails=$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getGeoZoneDetailData(
                        "resource/get_zone_data", _innerObject.toString(),
                        MyPreference.getValueString(Constants.E_ID, "").toString()
                    )
                    val responseJson = JSONArray(response.string())
                    responseJson.let {
                        val tripsObj = JSONObject(responseJson.get(0).toString())
                        geoZoneDetail.postValue(
                            Gson().fromJson(
                                tripsObj.toString(),
                                GeoZoneDetailModel::class.java
                            )
                        )
                    }
                    _status.value = ApiStatus.DONE
                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    @Throws(JSONException::class)
    fun getKeys(jsonobj: JSONObject): Map<String, Any> {
        hs = HashSet()
        val map = HashMap<String, Any>()
        val keys = jsonobj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            hs.add(key)
            var value = jsonobj.get(key)
            if (value is JSONObject) {
                value = getKeys(value)
            }
            map[key] = value
        }
        return map
    }

    fun callApiForGetGeoZonesGps3() {
        coroutineScope.launch {
            val map = mutableMapOf<String, Any?>(
                "service" to "get_zone",
                "api_key" to MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "")!!,
                "zone_id" to "*"

            )
            val parms = JSONObject(map as Map<*, *>).toString()

            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                val call = client.getGeoZonesGenerated(parms)!!.awaitResponse()
                geoZoneListGps3.postValue(call.body())

            }
        }
    }

    fun callApiForDeleteGeoZonesGps3(zoneID: String) {
        coroutineScope.launch {
            val map = mutableMapOf<String, Any?>(
                "service" to "remove_zone",
                "api_key" to MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "")!!,
                "zone_id" to zoneID
            )
            val parms = JSONObject(map as Map<*, *>).toString()
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                val call = client.geoZonesDeletedGps3(parms).awaitResponse()
                if (call.isSuccessful)
                    geoZoneDeletedGps3.postValue(call.body()!!.status)

            }
        }
    }

    fun callApiForAddGeoZoneData(name: String, convertedRGB: Int?) {

        var pArray: JSONArray
        var pObject: JSONObject
        coroutineScope.launch {
            try {
                _jsonObject = JSONObject()
                _jsonObject.put("itemsType", "avl_resource")
                _jsonObject.put("propName", "zones_library")
                _jsonObject.put("propValueMask", "*")
                _jsonObject.put("sortType", "zones_library")
                _jsonObject.put("propType", "propitemname")

                pObject = JSONObject()
                pObject.put("x", 23.8812222455)
                pObject.put("y", 54.6928239225)
                pObject.put("r", 300)

                pArray = JSONArray()
                pArray.put(pObject)

                _innerObject = JSONObject()
                _innerObject.put("n", name)
                _innerObject.put("t", 3)
                _innerObject.put("w", 300)
                _innerObject.put("f", 112)
                _innerObject.put("c", convertedRGB)
                _innerObject.put("tc", 16733440)
                _innerObject.put("ts", 12)
                _innerObject.put("min", 0)
                _innerObject.put("max", 19)
                _innerObject.put("p", pArray)
                _innerObject.put("id", 0)
                _innerObject.put("itemId", MyPreference.getValueInt(Constants.USER_BACT_ID, 0))
                _innerObject.put("callMode", "create")

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "resource/update_zone"
            body["params"] = _innerObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            DebugLog.d("RequestBody search_items=$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getAddGeoZoneData(body)
                    DebugLog.d("ResponseBody search_items = $response")

                    val jsonArray = JSONArray(response.string())
                    if (jsonArray.length() > 0) {
                        isGeoZoneAdded.postValue(true)
                    }
                    _status.value = ApiStatus.DONE
                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    fun callApiForAddGeoZoneData1(
        name: String,
        convertedRGB: Int?,
        centerFromPoint: LatLng,
        distanceTo: Int
    ) {
        var pArray: JSONArray
        var pObject: JSONObject
        coroutineScope.launch {
            try {
                _jsonObject = JSONObject()
                _jsonObject.put("itemsType", "avl_resource")
                _jsonObject.put("propName", "zones_library")
                _jsonObject.put("propValueMask", "*")
                _jsonObject.put("sortType", "zones_library")
                _jsonObject.put("propType", "propitemname")

                pObject = JSONObject()
                pObject.put("x", centerFromPoint.longitude)
                pObject.put("y", centerFromPoint.latitude)
                pObject.put("r", distanceTo)

                pArray = JSONArray()
                pArray.put(pObject)

                _innerObject = JSONObject()
                _innerObject.put("n", name)
                _innerObject.put("t", 3)
                _innerObject.put("w", distanceTo)
                _innerObject.put("f", 112)
                _innerObject.put("c", convertedRGB)
                _innerObject.put("tc", 16733440)
                _innerObject.put("ts", 12)
                _innerObject.put("min", 0)
                _innerObject.put("max", 19)
                _innerObject.put("p", pArray)
                _innerObject.put("id", 0)
                _innerObject.put("d", "")
                _innerObject.put("itemId", MyPreference.getValueInt(Constants.USER_BACT_ID, 0))
                _innerObject.put("callMode", "create")

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "resource/update_zone"
            body["params"] = _innerObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            DebugLog.d("RequestBody search_items=update_zone$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getAddGeoZoneData(body)
                    DebugLog.d("ResponseBody search_items = $response")
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
                            isGeoZoneAdded.postValue(true)
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

    fun callApiForAddGeoZoneDataGps3(zoneColor: String, zoneName: String, zoneVertices: String) {
        coroutineScope.launch {
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getAddGeoZoneDataGps3(
                        "{\"service\":\"create_zone\",\"api_key\":\"" +
                                MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "") +
                                "\",\"group_id\":\"0\",\"zone_color\":\"" +
                                zoneColor +
                                "\",\"zone_name\":\"" +
                                zoneName +
                                "\",\"zone_visible\":\"true\",\"zone_name_visible\":\"true\",\"zone_area\":\"0\",\"zone_vertices\":\"" +
                                zoneVertices +
                                "\"}"
                    ).awaitResponse()
                    val postResponseModel: PostResponseModel = response.body()!!
                    if (postResponseModel.status) {
                        isGeoZoneAdded.postValue(true)
                        DebugLog.d("onResponse: ${postResponseModel.id}")
                        _status.value = ApiStatus.SUCCESSFUL

                    } else {
                        errorMsg = postResponseModel.msg
                        _status.value = ApiStatus.ERROR
                    }

                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    fun callApiForDeleteGeoZone(name: String, id: String) {
        coroutineScope.launch {
            try {
                _innerObject = JSONObject()
                _innerObject.put("t", 3)
                _innerObject.put("f", 112)
                _innerObject.put("w", 300)
                _innerObject.put("id", id)
                _innerObject.put("n", name)
                _innerObject.put("itemId", MyPreference.getValueInt(Constants.USER_BACT_ID, 0))
                _innerObject.put("callMode", "delete")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val body = HashMap<String, String>()
            body["svc"] = "resource/update_zone"
            body["params"] = _innerObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            DebugLog.d("RequestBody zone_delete=$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getDeleteGeoZoneData(body)
                    DebugLog.d("ResponseBody zone_delete = $response")
                    isGeoZoneDeleted.postValue(true)
                    _status.value = ApiStatus.DONE
                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }
}