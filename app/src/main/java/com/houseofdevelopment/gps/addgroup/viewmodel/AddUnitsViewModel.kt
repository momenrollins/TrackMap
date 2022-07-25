package com.houseofdevelopment.gps.addgroup.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.houseofdevelopment.gps.base.AppBase
import com.houseofdevelopment.gps.base.BaseViewModel
import com.houseofdevelopment.gps.network.client.ApiStatus
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey
import com.houseofdevelopment.gps.utils.Constants
import com.houseofdevelopment.gps.utils.DebugLog
import com.houseofdevelopment.gps.utils.NetworkUtil
import com.houseofdevelopment.gps.utils.Utils
import com.houseofdevelopment.gps.vehicallist.model.GroupImeisModelGps3
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.*
import java.util.*
import kotlin.collections.ArrayList

class AddUnitsViewModel : BaseViewModel() {
    private lateinit var _innerObject: JSONObject
    private lateinit var _jsonObject: JSONObject
    val groupIdData = MutableLiveData<String>()
    val unitsData = MutableLiveData<Boolean>()
    var unitList = MutableLiveData<List<String>>()
    var updatedUnitList = MutableLiveData<Boolean>()
    var userUnitList = MutableLiveData<Boolean>()
    var unitItemsGps3 = MutableLiveData<GroupImeisModelGps3>()
    var updateGroupItemsGps3 = MutableLiveData<GroupImeisModelGps3>()

    /**
     *  Call api for create group
     */
    fun callApiForCreateGroup(strValue: CharSequence) {
        coroutineScope.launch {
            try {
                _jsonObject = JSONObject()
                _jsonObject.put("creatorId", MyPreference.getValueString(Constants.USER_ID, ""))
                _jsonObject.put("name", strValue)
                _jsonObject.put("dataFlags", 1)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "core/create_unit_group"
            body["params"] = _jsonObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            DebugLog.d("RequestBody CreateGroup =$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                com.houseofdevelopment.gps.utils.Utils.hideProgressBar()

                groupIdData.postValue("")
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getCreatedGroupData(body)
                    DebugLog.d("ResponseBody check_updates = $response")
                    response.body().let {
                        val jsonObject = JSONObject(response.body().toString())
                        val itemObject = jsonObject.getJSONObject("item")
                        groupIdData.postValue(itemObject.getString("id"))
                    }

                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    private val TAG = "AddUnitsViewModel"
    fun callApiForCreateGroupGps3(groupName: String, imeisList: ArrayList<String>) {
        coroutineScope.launch {
            val map = mutableMapOf<String, Any?>(
                "service" to "create_group_imeis",
                "api_key" to MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "")!!,
                "group_name" to groupName,
                "group_desc" to "",
                "imeis" to imeisList
            )
            val parms = JSONObject(map as Map<*, *>).toString()
            Log.d(TAG, "callApiForCreateGroupGps3: $parms")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                Utils.hideProgressBar()

                unitList.postValue(ArrayList())
            } else {
                try {
                    _status.value = ApiStatus.DONE
                    val call = client.createGroupCall(parms).awaitResponse()
                    if (call.isSuccessful) {
//                        updatedUnitList.postValue(call.body()!!.status)
//                        Log.d(TAG, "callApiForCreateGroupGps3:id ${call.body()!!.id}")
                        val item = GroupImeisModelGps3(true, groupName, call.body()!!.id, imeisList)
                        unitItemsGps3.postValue(item)
                    }
                } catch (error: java.lang.Exception) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }

    }

    fun callApiForUpdateGroupImeisGps3(
        group_id: Int,
        imeisList: ArrayList<String>,
        type: String,
        selectedCarListGps3: ArrayList<String>,
        groupName: String
    ) {

        coroutineScope.launch {

            _status.value = ApiStatus.LOADING

            val map = mutableMapOf<String, Any?>(
                "service" to "update_group_imeis",
                "api_key" to MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "")!!,
                "group_id" to group_id,
                "imeis" to imeisList,
                "type" to type
            )
            val parms = JSONObject(map as Map<*, *>).toString()
            Log.d("TAG", "initRecyclerView: $parms")

            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                Utils.hideProgressBar()
                if (type == "remove")
                    updatedUnitList.postValue(false)
                else
                    updateGroupItemsGps3.postValue(GroupImeisModelGps3())
            } else {
                try {
                    val call = client.UpdateGroupImeis(parms).awaitResponse()
                    if (call.isSuccessful) {
                        if (type == "remove")
                            updatedUnitList.postValue(call.body()!!.status)
                        else
                            updateGroupItemsGps3.postValue(
                                GroupImeisModelGps3(
                                    true,
                                    groupName,
                                    group_id.toString(),
                                    imeisList + selectedCarListGps3
                                )
                            )
                    }
                    _status.value = ApiStatus.DONE
                } catch (error: java.lang.Exception) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    /**
     * Call api for add units to Group
     */
    fun callApiForAddUnitsToGroup(
        carId: JSONArray,
        groupId: Long,
        toDelete: Boolean
    ) {
        coroutineScope.launch {
            try {
                _jsonObject = JSONObject()
                _jsonObject.put("itemId", groupId)
                _jsonObject.put("units", carId)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "unit_group/update_units"
            body["params"] = _jsonObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            DebugLog.d("RequestBody check_updates =$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                Utils.hideProgressBar()
                updatedUnitList.postValue(false)
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getAddUnitsToGroup(body)
                    DebugLog.d("ResponseBody check_updates = $response")
                    if (toDelete) {
                        if (response.body().toString().contains("\"u\""))
                            updatedUnitList.postValue(true)
                    } else
                        unitList.postValue(listOf(response.body().toString()))
                    /* if (response.items[]!!.isNotEmpty()) {
                         _createdGroupData.postValue(response.items)
                         createGroup = response.items
                     }*/
                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }
}
