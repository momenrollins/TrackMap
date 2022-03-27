package com.trackmap.gps.homemap.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.trackmap.gps.PostResponseModel
import com.trackmap.gps.base.AppBase
import com.trackmap.gps.base.BaseViewModel
import com.trackmap.gps.geozone.model.GeoZoneDetailModel
import com.trackmap.gps.homemap.model.MapListDataModelGps3
import com.trackmap.gps.homemap.model.CarDriverModelGps3
import com.trackmap.gps.vehicallist.model.ItemGps3
import com.trackmap.gps.homemap.model.SensorItemModelGps3
import com.trackmap.gps.homemap.model.*
import com.trackmap.gps.homemap.ui.ClusterRender
import com.trackmap.gps.login.model.SignInRequest
import com.trackmap.gps.network.ApiClientForBrainvire
import com.trackmap.gps.network.ApiClientForGps3
import com.trackmap.gps.network.client.ApiStatus
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import com.trackmap.gps.usersettings.model.GetNotificationStatusRequest
import com.trackmap.gps.utils.Constants
import com.trackmap.gps.utils.DebugLog
import com.trackmap.gps.utils.NetworkUtil
import com.trackmap.gps.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.*

class HomeMapViewModel : BaseViewModel() {

    // Gps1, Gps2
    var carDataList = ArrayList<Item>()
    var unitList = MutableLiveData<List<CheckedUnitModel>>()
    var carAddressData = MutableLiveData<String>()
    var carDriverNameData = MutableLiveData<String>()
    var carCarDetails = MutableLiveData<GetCarDetailsModel>()
    var sensorDetails = MutableLiveData<String>()
    var geoZoneDetail = MutableLiveData<ArrayList<GeoZoneDetailModel>>()

    // Gps3
    var carCarDetailsGPS3 = MutableLiveData<ArrayList<ItemGps3>>()
    var carAddressDataGPS3 = MutableLiveData<String>()
    var isRightKey = MutableLiveData<Boolean>()
    var carDriverNameDataGPS3 = MutableLiveData<CarDriverModelGps3>()
    var sensorDetailsGps3 = MutableLiveData<SensorItemModelGps3>()


    val handler = Handler(Looper.getMainLooper())
    private var hs = ArrayList<String>()
    var tokenExpiredCall = MutableLiveData<Boolean>()
    var isTimeOut = false
    var counter = 6

    val updateTextTask = object : Runnable {
        override fun run() {
            handler.postDelayed(this, 10000)// every 10 sec repeat

            if (MyPreference.getValueString(PrefKey.SELECTED_SERVER_DATA, "")!!.contains("s3")) {
                getCarList()
            } else {
                val carData = mutableListOf<UpdatedUnitModel>()
                for (carList in carDataList) {
                    val unitModel = carList.nm?.let {
                        carList.cls?.let { it1 ->
                            carList.id?.let { it2 ->
                                carList.bact?.let { it3 ->
                                    UpdatedUnitModel(it, it1, it2, it3)
                                }
                            }
                        }
                    }
                    unitModel?.let { carData.add(it) }
                }
                DebugLog.e("startRefreshingAPI")
                callAPIForUpdateUnits(carData)
                if (counter % 6 == 0)
                    getInactiveBindStatus()
                counter++
            }
        }
    }

    fun getInactiveBindStatus() {
        // Create a Coroutine scope using a job to be able to cancel when needed
        val viewModelJob = Job()
        // the Coroutine runs using the Main (UI) dispatcher
        val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
        val client = ApiClientForBrainvire.getApiInterface()

        Log.d(TAG, "getInactiveBindStatus:par ")
//        Utils.showProgressBar(this)
        coroutineScope.launch {

            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
//                Utils.hideProgressBar()
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getNotificationStatus(GetNotificationStatusRequest())

                    val rsp = response.string()
                    val jsonObject = JSONObject(rsp)

                    val metaObj = jsonObject.getJSONObject("meta")
                    val statusCode = metaObj.getString("status_code")
//                    Utils.hideProgressBar()
                    if (statusCode.equals("200", true)) {
                        val data = jsonObject.getJSONObject("data")
                        val status = data.getString("status")
                        Log.d(TAG, "getInactiveBindStatus:00  $data $status")
                        /*if(status.equals("Inactive", true)) {
                            val builder = AlertDialog.Builder(this@MainActivity)
                            //set title for alert dialog
                            builder.setTitle(getString(R.string.app_name))
                            //set message for alert dialog
                            builder.setMessage("Your account has been Inactivated. Please contact Support for further assistance.")

                            //performing positive action
                            builder.setPositiveButton("Ok"){dialogInterface, which ->
                                dialogInterface.dismiss()

                                callLogoutApi()
                            }
                            val alertDialog: AlertDialog = builder.create()
                            alertDialog.setCancelable(false)
                            alertDialog.show()
                        }*/

                        MyPreference.setValueString(PrefKey.IS_USER_ACTIVE, status)
                        val isBindAllow = data.getString("is_bind_allow")
                        if (isBindAllow.equals("1")) {
                            MyPreference.setValueBoolean(PrefKey.IS_CEO_DATA, true)
                        } else {
                            MyPreference.setValueBoolean(PrefKey.IS_CEO_DATA, false)
                        }
                        val days = data.getString("days")
                        if (!days.toString()
                                .equals("null", true) && !days.toString().equals("0", true)
                        ) {
                            MyPreference.setValueBoolean(PrefKey.SUBSCRIPTION_DAYS, true)
                        } else
                            MyPreference.setValueBoolean(PrefKey.SUBSCRIPTION_DAYS, false)
                    }
                    Log.d(TAG, "getInactiveBindStatus:01  $statusCode")
                } catch (error: Throwable) {
                    Log.d(TAG, "getInactiveBindStatus:02 CATCH ${error.message}")
//                    Utils.hideProgressBar()
                }
            }
        }
    }

    fun clearData() {
        _status = MutableLiveData<ApiStatus>()
        tokenExpiredCall = MutableLiveData<Boolean>()

        carAddressData = MutableLiveData<String>()
        carAddressDataGPS3 = MutableLiveData<String>()

        carDriverNameData = MutableLiveData<String>()
        carDriverNameDataGPS3 = MutableLiveData<CarDriverModelGps3>()

        carCarDetails = MutableLiveData<GetCarDetailsModel>()
        carCarDetailsGPS3 = MutableLiveData<ArrayList<ItemGps3>>()

        sensorDetails = MutableLiveData<String>()
        sensorDetailsGps3 = MutableLiveData<SensorItemModelGps3>()
    }

    fun callsetToken() {
        startRefreshingAPI()
        if (MyPreference.getValueString(PrefKey.SELECTED_SERVER_DATA, "")!!.contains("s3")) {
            getCarList()
        } else {
            DebugLog.e("callsetTokenMethod")
            coroutineScope.launch {
                val jsonObj = JSONObject()
                try {

                    jsonObj.put(
                        "token", MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "").toString()
                    )
                    jsonObj.put("operateAs", "")
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val body = HashMap<String, String>()
                body["svc"] = "token/login"
                body["params"] = jsonObj.toString()
                _status.postValue(ApiStatus.LOADING)
                DebugLog.d("RequestBody =$body")
                if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                    _status.value = ApiStatus.NOINTERNET
                } else {
                    try {
                        // this will run on a thread managed by Retrofit
                        val response = client.callSetToken(body)

                        if (response.eid != null) {
                            if (response.eid!!.isNotEmpty()) {
                                DebugLog.d("e_id" + response.eid)
                                val isFirstTimeSignIn =
                                    MyPreference.getValueBoolean(PrefKey.IS_SIGNIN, false)
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
                                MyPreference.setValueInt(
                                    Constants.USER_BACT_ID,
                                    response.user.bact
                                )
                                Log.d(TAG, "callsetToken: $isFirstTimeSignIn ")
                                if (!isFirstTimeSignIn) {
                                    Log.d(TAG, "callsetToken: TOKEN::3 ")
                                    callSignInMethod(response.user.nm)
                                } else {
                                    callApiForCarListData()
//                                    getCarList()

                                }
                            }
                        } else if (response.error != null && response.error == "8") {
                            _status.value = ApiStatus.ERROR
                            tokenExpiredCall.postValue(true)
                        } else {
                            _status.value = ApiStatus.ERROR
                        }
                    } catch (error: Throwable) {
                        _status.value = ApiStatus.ERROR
                        DebugLog.d("errorToken" + error.printStackTrace())
                        tokenExpiredCall.postValue(false)
                    }
                }
            }
        }
    }

    fun sendTokenGps3(username:String, token:String) {
        ApiClientForGps3.initRetrofit()
        val retrofit = ApiClientForGps3.getApiInterface()
        val call = retrofit.sendTokenGps3(username!!, token)
        call.enqueue(object : Callback<PostResponseModel?> {
            override fun onResponse(
                call: Call<PostResponseModel?>,
                response: Response<PostResponseModel?>
            ) {
                if (!response.isSuccessful) {
                    Log.d(TAG, "onResponse: not suc " + response.message())
                    return
                }
                Log.d(
                    TAG,
                    "onResponse: suc " + if (response.body() != null) response.body()!!.msg else null
                )
            }

            override fun onFailure(call: Call<PostResponseModel?>, t: Throwable) {
                Log.d(TAG, "onFailure: " + t.message)
            }
        })
    }

    fun callSignInMethod(nm: String) {

        val signInRequest = SignInRequest()
        signInRequest.email_username = nm
        signInRequest.device_token = MyPreference.getValueString(PrefKey.FCM_TOKEN, "")
        signInRequest.domain_name = MyPreference.getValueString(PrefKey.SELECTED_SERVER_DATA, "")

        // Create a Coroutine scope using a job to be able to cancel when needed
        val viewModelJob = Job()
        // the Coroutine runs using the Main (UI) dispatcher
        val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
        ApiClientForBrainvire.getRetrofit()
        val client = ApiClientForBrainvire.getApiInterface()
        coroutineScope.launch {

            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.callSignIn(signInRequest)
                    DebugLog.d("ResponseBody callSignIn =1 ${response.body()?.data?.accessToken} ${response.body()?.data?.userDetail?.ownerId}")

                    MyPreference.setValueString(
                        PrefKey.ACCESS_TOKEN_BRAINVIRE,
                        response.body()?.data?.accessToken.toString()
                    )

                    response.body()?.data?.userDetail?.ownerId?.let {
                        MyPreference.setValueString(
                            PrefKey.OWNER_ID, it
                        )
                    }

                    MyPreference.setValueBoolean(PrefKey.IS_SIGNIN, true)

                    val isBindAllow = response.body()?.data?.userDetail?.isBindAllow
                    if (isBindAllow != null && isBindAllow == 1) {
                        MyPreference.setValueBoolean(PrefKey.IS_CEO_DATA, true)
                    } else {
                        MyPreference.setValueBoolean(PrefKey.IS_CEO_DATA, false)
                    }

                    callApiForCarListData()

                } catch (error: Throwable) {
                    DebugLog.d("ResponseBody callSignIn err= ${error.message}")
                    error.printStackTrace()

                    callApiForCarListData()

                }
            }
        }
    }

    //Get Car Details for GPS3

    private val TAG = "HomeMapViewModel"

    fun getCarList() {
        //_status.value = ApiStatus.LOADING
        viewModelScope.launch {
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET

            } else {
                val call: Call<ArrayList<ItemGps3>> =
                    client.getCars(
                        "user",
                        MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "")!!,
                        "USER_GET_OBJECTS"
                    )
                call.enqueue(object : Callback<ArrayList<ItemGps3>> {
                    override fun onResponse(
                        call: Call<ArrayList<ItemGps3>>,
                        response: Response<ArrayList<ItemGps3>>
                    ) {
                        if (!response.isSuccessful) {
                            _status.value = ApiStatus.DONE
                            return
                        } else {
                            val tempData: MapListDataModelGps3?
//                    allCarData = response.body()
                            isRightKey.postValue(true)
                            carCarDetailsGPS3.postValue(response.body())
                            tempData =
                                MapListDataModelGps3(
                                    response.body()
                                )
                            _status.value = ApiStatus.DONE
                            Constants.isFirstTimeApiCall = false
                            Utils.writeCarListingDataOnFileGps3(AppBase.instance, tempData)
                        }

                    }

                    override fun onFailure(call: Call<ArrayList<ItemGps3>>, t: Throwable) {
                        Log.d(TAG, "onFailure:1 " + t.message)
                        _status.value = ApiStatus.DONE

                        viewModelScope.launch {
                            val response = clientMain.getCarsStringMsg(
                                "user",
                                MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "")!!,
                                "USER_GET_OBJECTS"
                            )!!.awaitResponse()
                            val msgString: String = response.body()!!
                            Log.d(TAG, "onResponse: msg $msgString")

                            if (msgString == "ERROR: wrong API key")
                                isRightKey.postValue(false)
                            else
                                isRightKey.postValue(true)
                        }
                    }
                })
            }
        }

    }

    fun callApiForCarAddressGps3(lat: String, lng: String) {
        //_status.value = ApiStatus.LOADING
        if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
            _status.value = ApiStatus.NOINTERNET
        } else {
            viewModelScope.launch {
                try {
                    val call =
                        clientMain.getCarLocation(
                            "user",
                            MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "")!!,
                            "GET_ADDRESS,$lat,$lng"
                        ).awaitResponse()
                    if (call.isSuccessful) {
                        _status.value = ApiStatus.DONE
                        carAddressDataGPS3.postValue(call.body()!!)
                    } else {
                        _status.value = ApiStatus.ERROR
                        Log.d(TAG, "getCarLocation: ${call.message()}")
                    }
                } catch (error: java.lang.Exception) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }

            }
        }
    }

    // get car drivers details

    fun callApiForDriverNameGps3(car_imei: String) {
        viewModelScope.launch {
//            _status.value = ApiStatus.LOADING
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                val map = mutableMapOf<String, Any?>(
                    "service" to "get_driver",
                    "imei" to car_imei,
                    "api_key" to MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "")!!
                )
                val parms = JSONObject(map as Map<*, *>).toString()

                try {
                    val call = client.CarDriverDetails_CALL(parms).awaitResponse()
                    if (call.isSuccessful) {
                        val driverModelGps3: CarDriverModelGps3 = call.body()!!
                        carDriverNameDataGPS3.postValue(
                            driverModelGps3
                        )
                        _status.value = ApiStatus.DONE
                    } else {
                        _status.value = ApiStatus.ERROR
                    }
                } catch (error: java.lang.Exception) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }


            }
        }
    }


    fun callApiForSensorValueGps3(imei: String?) {
        viewModelScope.launch {
            val map = mutableMapOf<String, Any?>(
                "service" to "get_sensors",
                "imei" to imei,
                "sensor_id" to "*",
                "api_key" to MyPreference.getValueString(
                    PrefKey.ACCESS_TOKEN,
                    ""
                )!!
            )
            val parms = JSONObject(map as Map<*, *>).toString()


//            _status.value = ApiStatus.LOADING
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {

                    val call = client.getSensors(parms).awaitResponse()
                    call.let {
                        sensorDetailsGps3.postValue(it.body()!!)
                    }
                    _status.value = ApiStatus.DONE
                } catch (error: Throwable) {
                    sensorDetailsGps3.postValue(SensorItemModelGps3())
                    error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }


    private fun callApiForCarListData() {
        viewModelScope.launch {

            val _innerObject = JSONObject()
            try {
                val _jsonObject = JSONObject()
                _jsonObject.put("itemsType", "avl_unit")
                _jsonObject.put("propName", "sys_user_creator")
                _jsonObject.put("propValueMask", "")
                _jsonObject.put("sortType", "sys_name")
                _jsonObject.put("propType", "creatortree")
                _innerObject.put("spec", _jsonObject)
                _innerObject.put("force", 1)
                _innerObject.put("flags", "4611686018427387903")
                _innerObject.put("from", 0)
                _innerObject.put("to", 1000)


            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "core/search_items"
            body["params"] = _innerObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()


            DebugLog.d("RequestBody search_items=$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getCarListDataOnMap(body)

                    if (response != null && response.items.isNotEmpty()) {
                        carDataList.clear()
                        carDataList.addAll(response.items)
                        DebugLog.e("callApiForCarListData" + response.items.size)
                        val tempData = Utils.getCarListingData(AppBase.instance)
                        if (tempData.items.size == 0) {
                            tempData.items = ArrayList<Item>()
                            tempData.items.addAll(response.items)
                            Utils.writeCarListingDataOnFile(AppBase.instance, tempData)
                        }
                        handler.removeCallbacks(updateTextTask)
                        startRefreshingAPI()
                        val carData = mutableListOf<UpdatedUnitModel>()
                        /*for (carList in carDataList) {
                            val unitModel = carList.nm?.let {
                                carList.cls?.let { it1 ->
                                    carList.id?.let { it2 ->
                                        carList.bact?.let { it3 ->
                                            UpdatedUnitModel(
                                                it,
                                                it1,
                                                it2,
                                                it3
                                            )
                                        }
                                    }
                                }
                            }
                            unitModel?.let { carData.add(it) }
                        }*/
                        callAPIForUpdateUnits(carData)
                        if (MyPreference.getValueBoolean(PrefKey.IS_GEO_ZONES, false)) {
                            callApiForGeoZoneDetailsData()
                        }

                    } else {
                        errorMsg = "Empty"
                        _status.value = ApiStatus.DONE
                    }
                    // _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    errorMsg = error.toString()

                    Log.d(TAG, "callApiForCarListData: ${error.message}")
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    fun callApiForGeoZoneDetailsData() {

        viewModelScope.launch {
            val _innerObject = JSONObject()
            try {

                _innerObject.put(
                    "itemId",
                    MyPreference.getValueInt(Constants.USER_BACT_ID, 0).toLong()
                )
                _innerObject.put("flags", 25)
                _innerObject.put("col", "")

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
                        val arrList = ArrayList<GeoZoneDetailModel>()
                        for (int in 0 until responseJson.length()) {
                            val tripsObj = JSONObject(responseJson.get(int).toString())
                            arrList.add(
                                Gson().fromJson(
                                    tripsObj.toString(), GeoZoneDetailModel::class.java
                                )
                            )
                        }
                        geoZoneDetail.postValue(arrList)
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
     *  Refresh API for every 10 sec interval to update units data
     */
    private fun startRefreshingAPI() {
        /*  handler.post(object : Runnable {
              override fun run() {
                  handler.postDelayed(this, 5000)  // every 5 sec repeat
                  val carData = mutableListOf<UpdatedUnitModel>()
                  for (carList in carDataList) {
                      val unitModel = carList.nm?.let { carList.cls?.let { it1 -> carList.id?.let { it2 -> carList.bact?.let { it3 -> UpdatedUnitModel(it, it1, it2, it3) } } } }
                      unitModel?.let { carData.add(it) }
                  }
                  DebugLog.e("startRefreshingAPI")
                  callAPIForUpdateUnits(carData)
              }
          })*/

        handler.post(updateTextTask)
    }

    private fun callAPIForUpdateUnits(carData: MutableList<UpdatedUnitModel>) {
        viewModelScope.launch {
            val _modeObject = JSONObject()

            try {
                _modeObject.put("mode", "add")
                val _unitArray = JSONArray()
                for (carInfo in carData.indices) {
                    val _unitObject = JSONObject()
                    val _detectInnerObject = JSONObject()

                    _unitObject.put("id", carData[carInfo].carId)
                    _detectInnerObject.put("ignition", "0")
                    _detectInnerObject.put("trips", "0")
                    _unitObject.put("detect", _detectInnerObject)
                    _unitArray.put(carInfo, _unitObject)
                }
                _modeObject.put("units", _unitArray)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = Constants.UPDATE_UNITS
            body["params"] = _modeObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            DebugLog.d("RequestBody update_units =$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                Log.d(TAG, "callAPIForUpdateUnits: $body")
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getUnitInSession(body)
                    if (response.units.toString().isNotEmpty()) {
                        callAPIForCheckUpatedUnits()
                    }

                    //_status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    private fun callAPIForCheckUpatedUnits() {
        viewModelScope.launch {
            val _detailizationObject = JSONObject()
            try {
                _detailizationObject.put("detalization", "3")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val body = HashMap<String, String>()
            body["svc"] = "events/check_updates"
            body["params"] = _detailizationObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()
            DebugLog.d("RequestBody check_updates =$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.checkUnitInSession(body)
                    response.body().let {
                        val responseBody = response.body()

                        val jsonObj = JSONObject(responseBody.toString())
                        Log.d(TAG, "callAPIForCheckUpatedUnits:json ${responseBody!!.size()}")
                        jsonObj.keys()
                        getKeys(jsonObj)
                        val updatedUnitList = mutableListOf<CheckedUnitModel>()
                        for (i in 0 until hs.size) {
                            Log.d(TAG, "callAPIForCheckUpatedUnits:hs ${hs[i]} ")
                            val carId = jsonObj.getJSONArray(hs[i])
                            Log.d(TAG, "callAPIForCheckUpatedUnits:carId ${carId.length()} ")
                            if (carId.length() > 1) {
                                val ignitionData = carId.getJSONObject(0)
                                val tripsData = carId.getJSONObject(1)
                                var ignitionFromLong = ""
                                var ignitionFromLat = ""
                                var ignitionToLong = ""
                                var ignitionToLat = ""
                                var ignitionState = ""

                                val ignition = ignitionData.getJSONObject("ignition")
                                val list = ArrayList<String>()
                                val keys = ignition.keys()
                                while (keys.hasNext()) {
                                    val key = keys.next()
                                    list.add(key)
                                }
                                if (list.size > 0) {
                                    val ignitionstartIndex = ignition.getJSONObject(list[0])
                                    val ignitionFrom = ignitionstartIndex.getJSONObject("from")
                                    ignitionstartIndex.getJSONObject("to")

                                    ignitionFromLong = ignitionFrom.getString("x")
                                    ignitionFromLat = ignitionFrom.getString("y")
                                    ignitionToLong = ignitionFrom.getString("x")
                                    ignitionToLat = ignitionFrom.getString("y")
                                    ignitionState = ignitionstartIndex.getString("state")
                                }
                                val trip = tripsData.getJSONObject("trips")
                                val trip_from = trip.getJSONObject("from")
                                val trip_to = trip.getJSONObject("to")

                                val checkedUnitModel = CheckedUnitModel(
                                    carId = hs[i],
                                    ignitionFromLong = ignitionFromLong,
                                    ignitionFromLat = ignitionFromLat,
                                    ignitionToLong = ignitionToLong,
                                    ignitionToLat = ignitionToLat,
                                    tripFromLong = trip_from.getString("x"),
                                    tripFromLat = trip_from.getString("y"),
                                    tripFromT = trip_from.getString("t"),
                                    tripToLong = trip_to.getString("x"),
                                    tripToLat = trip_to.getString("y"),
                                    tripToT = trip_to.getString("t"),
                                    trip_m = trip.getString("m"),
                                    trip_f = trip.getString("f"),
                                    trip_state = ignitionState,
                                    trip_max_speed = trip.getString("max_speed"),
                                    trip_curr_speed = trip.getString("curr_speed"),
                                    trip_avg_speed = trip.getString("avg_speed"),
                                    trip_distance = trip.getString("distance"),
                                    trip_odometer = trip.getString("odometer"),
                                    trip_course = trip.getString("course"),
                                    trip_altitude = trip.getString("altitude")
                                )

                                updatedUnitList.add(checkedUnitModel)
                            } else {
                                Log.d(
                                    TAG,
                                    "callAPIForCheckUpatedUnits:carIdupdate ${i} ${carId.length()} "
                                )
                            }
                        }
                        Log.d(TAG, "callAPIForCheckUpatedUnits:tes ${updatedUnitList.size}  ")
                        unitList.postValue(updatedUnitList)
                    }
                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    Log.d(TAG, "callAPIForCheckUpatedUnits:err ${error.toString()}  ")
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    fun getCarImageFromData(
        it: List<CheckedUnitModel>?,
        requireContext: Context
    ): ArrayList<Item> {
        val unitCarList = ArrayList<Item>()
        val data: MapListDataModel = Utils.getCarListingData(requireContext)

        if (data.items.size > 0) {
            for (int in 0 until data.items.size) {
                for (car in it!!) {
                    if (data.items[int].id.toString().equals(car.carId, true)) {
                        data.items[int].tripFromLong = (car.tripFromLong)
                        data.items[int].tripFromLat = (car.tripFromLat)
                        data.items[int].tripToLong = (car.tripToLong)
                        data.items[int].tripToLat = (car.tripToLat)
                        data.items[int].tripFromT = (car.tripFromT)
                        data.items[int].tripToT = (car.tripToT)
                        data.items[int].trip_m = (car.trip_m)
                        data.items[int].trip_f = (car.trip_f)
                        data.items[int].trip_state = (car.trip_state)
                        data.items[int].trip_max_speed = (car.trip_max_speed)
                        data.items[int].trip_curr_speed = (car.trip_curr_speed)
                        data.items[int].trip_avg_speed = (car.trip_avg_speed)
                        data.items[int].trip_distance = (car.trip_distance)
                        data.items[int].trip_odometer = (car.trip_odometer)
                        data.items[int].trip_course = (car.trip_course)
                        data.items[int].trip_altitude = (car.trip_altitude)

                        unitCarList.add(data.items[int])
                    }
                }
            }
            carDataList.clear()
            carDataList.addAll(data.items)
            Utils.writeCarListingDataOnFile(requireContext, data)

        }
        Constants.isFirstTimeApiCall = false
        if (unitCarList.isEmpty())
            return ArrayList()

        return unitCarList
    }


    @Throws(JSONException::class)
    fun getKeys(jsonobj: JSONObject): Map<String, Any> {
        hs = ArrayList()
        hs.clear()
        val map = HashMap<String, Any>()
        val keys = jsonobj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            Log.d(TAG, "getKeys: KEY $key")
            hs.add(key)
            var value = jsonobj.get(key)
            if (value is JSONObject) {
                value = getKeys(value)
            }
            map[key] = value
        }
        Log.d(TAG, "getKeys: KEYS SIZE ${hs.size}")
        Log.d(TAG, "getKeys: MAP $map")
        return map
    }

    /**
     * Method to get Car Address details
     */
    fun callApiForCarAddress(longitude: String, latitude: String) {
        viewModelScope.launch {
            val _carAddressArray = JSONArray()
            try {
                val jsonObj_ = JSONObject()
                jsonObj_.put("lon", longitude)
                jsonObj_.put("lat", latitude)
                _carAddressArray.put(jsonObj_)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["coords"] = _carAddressArray.toString()
            body["flags"] = "1255211008"
            body["uid"] = MyPreference.getValueString(Constants.USER_ID, "").toString()

            DebugLog.d("RequestBody =$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    //  _status.value = ApiStatus.LOADING
                    // this will run on a thread managed by Retrofit
                    val response = client.getAddressOfCar(body)
                    response.let {
                        carAddressData.postValue(response.body()?.get(0)!!.asString.toString())
                    }
                    _status.value = ApiStatus.DONE
                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    fun callApiForDriverName(model: ClusterRender) {
        viewModelScope.launch {
            val jsonObj_ = JSONObject()
            try {
                jsonObj_.put("unitId", model.mCarModel.id)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = Constants.GET_UNIT_DRIVER
            body["params"] = jsonObj_.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            DebugLog.d("RequestBody =$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    var driverName = ""
                    // this will run on a thread managed by Retrofit
                    val response = client.getDriverName(body)
                    response.let {
                        val responseBody = response.body()
                        DebugLog.d("ResponseBody =$responseBody")
                        val JsonObj = JSONObject(responseBody.toString())
                        val keys = JsonObj.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val carId = JsonObj.getJSONArray(key)
                            for (i in 0 until carId.length()) {
                                val driverData = carId.getJSONObject(i)
                                driverName = driverData.getString("nm")
                            }
                        }
                        carDriverNameData.postValue(driverName)
                    }

                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    fun callApiForGetCarDetails(model: ClusterRender) {
        viewModelScope.launch {
            val jsonObj_ = JSONObject()
            try {
                jsonObj_.put("id", model.mCarModel.id)
                jsonObj_.put("flags", "4611686018427387903")
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = Constants.SEARCH_ITEM
            body["params"] = jsonObj_.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            DebugLog.d("RequestBody =$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getCarDetails(body)
                    response.let {
                        carCarDetails.postValue(response)
                    }

                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    fun callApiForSensorValue(model: ClusterRender) {
        viewModelScope.launch {
            val jsonObj_ = JSONObject()
            try {
                jsonObj_.put("unitId", model.mCarModel.id)
                jsonObj_.put("flags", "1")
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = Constants.SENSOR_VLAUE
            body["params"] = jsonObj_.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            DebugLog.d("RequestBody =$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getSensorValue(body)
                    response.let {
                        sensorDetails.postValue(response.body().toString())
                    }
//                    DebugLog.d("RequestBody sensorDetails=$sensorDetails")
                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    sensorDetails.postValue("")
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
