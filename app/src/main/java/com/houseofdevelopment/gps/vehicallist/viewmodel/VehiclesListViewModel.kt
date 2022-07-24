package com.houseofdevelopment.gps.vehicallist.viewmodel

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
import com.houseofdevelopment.gps.vehicallist.model.GroupListDataModel
import com.houseofdevelopment.gps.vehicallist.model.GroupListDataModelGps3
import kotlinx.coroutines.launch
import org.json.JSONObject

import retrofit2.awaitResponse
import java.util.*

class VehiclesListViewModel : BaseViewModel() {

    private lateinit var _innerObject: JSONObject
    private lateinit var _jsonObject: JSONObject
    private lateinit var getGroupData: MutableList<GroupListDataModel.Item>
    val groupData = MutableLiveData<List<GroupListDataModel.Item>>()
    val groupDataGps3 = MutableLiveData<GroupListDataModelGps3>()
    val itemGroupDataGps3 = MutableLiveData<GroupImeisModelGps3>()

    val deleteGroup = MutableLiveData<Boolean>()
    var groupId: String = ""

    /**
     *  Get list of group item data
     */
    fun callApiForGroupListData() {
        coroutineScope.launch {
            try {
                _jsonObject = JSONObject()
                _jsonObject.put("itemsType", "avl_unit_group")
                _jsonObject.put("propName", "")
                _jsonObject.put("propValueMask", "58415")
                _jsonObject.put("sortType", "")
                _jsonObject.put("propType", "creatortree")

                _innerObject = JSONObject()
                _innerObject.put("spec", _jsonObject)
                _innerObject.put("force", 1)
                _innerObject.put("flags", 4611686018427387903)
                _innerObject.put("from", 0)
                _innerObject.put("to", 100)


            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "core/search_items"
            body["params"] = _innerObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            _status.value = ApiStatus.LOADING

            DebugLog.d("RequestBody search_group=$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getGroupListData(body)
                    DebugLog.d("ResponseBody search_items = $response")
                    if (response.items!!.isNotEmpty()) {
                        groupData.postValue(response.items)
                        getGroupData = response.items
                    }
                    _status.value = ApiStatus.DONE
                    Utils.hideProgressBar()
                } catch (error: Throwable) {
                    Utils.hideProgressBar()
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    fun callApiForGroupListDataGps3() {
        coroutineScope.launch {
            val map = mutableMapOf<String, Any?>(
                "service" to "get_groups",
                "api_key" to MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "")!!
            )
            loading(true)
            val parms = JSONObject(map as Map<*, *>).toString()
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                val call = client.getGroupCallGps3(parms).awaitResponse()
                if (call.isSuccessful) {
                    loading(false)
                    groupDataGps3.postValue(call.body()!!)
                }
                Utils.hideProgressBar()
            }
        }
    }

    private val TAG = "VehiclesListViewModel"
    fun callApiForGetGroupDataGps3(group_id: Int, isMaping: Boolean) {
        coroutineScope.launch {
            val map = mutableMapOf<String, Any?>(
                "service" to "get_group",
                "api_key" to MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "")!!,
                "group_id" to group_id
            )
            val parms = JSONObject(map as Map<*, *>).toString()
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                val call = client.getGroupDataGps3(parms).awaitResponse()
                if (call.isSuccessful) {
                    if (isMaping) MyPreference.setValueString("SHowgroup", "ShowMaping")
                    else MyPreference.setValueString("SHowgroup", "ShowItems")

                    itemGroupDataGps3.postValue(call.body()!!)
                }
            }
        }
    }

    fun callApiForDeleteGroup(selected: Int) {
        coroutineScope.launch {
            try {
                _jsonObject = JSONObject()
                _jsonObject.put("itemId", selected)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "item/delete_item"
            body["params"] = _jsonObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            DebugLog.d("RequestBody delete group =$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getDeleterGroupData(body)
                    if (response.code() == 200) {
                        deleteGroup.postValue(true)
                    } else {
                        deleteGroup.postValue(false)
                    }
                    DebugLog.d("ResponseBody delete group =$body")
                    /*response.let {

                    }*/

                } catch (error: Throwable) {
                    deleteGroup.postValue(false)
                    error.toString()
                    _status.value = ApiStatus.ERROR
                }

            }
        }
    }

    fun callApiForDeleteGroupGps3(groupId: Int) {
        coroutineScope.launch {
            val map = mutableMapOf<String, Any?>(
                "service" to "remove_group",
                "api_key" to MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "")!!,
                "group_id" to groupId
            )
            val parms = JSONObject(map as Map<*, *>).toString()
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    val call = client.deleteGroupGps3(parms).awaitResponse()
                    if (call.isSuccessful && call.body()!!.status) deleteGroup.postValue(true)
                    else deleteGroup.postValue(false)
                    Log.d("TAG", "callApiForDeleteGroupGps3: ${call.body()!!.status}")
                } catch (e: Exception) {
                    Log.d("TAG", "callApiForDeleteGroupGps3: ${e.message}")
                    deleteGroup.postValue(false)
                }
            }
        }
    }

    fun loading(isLoading: Boolean) {
        if (isLoading) _status.value = ApiStatus.LOADING
        else _status.value = ApiStatus.DONE
    }
}
