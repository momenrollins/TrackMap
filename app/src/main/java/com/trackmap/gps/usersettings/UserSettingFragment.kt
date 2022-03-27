package com.trackmap.gps.usersettings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.trackmap.gps.DataValues
import com.trackmap.gps.DataValues.serverData
import com.trackmap.gps.DataValues.username
import com.trackmap.gps.R
import com.trackmap.gps.base.AppBase
import com.trackmap.gps.base.BaseFragment
import com.trackmap.gps.databinding.FragmentUserSettingBinding
import com.trackmap.gps.login.ui.LoginActivity
import com.trackmap.gps.network.ApiClientForBrainvire
import com.trackmap.gps.network.ApiClientForGps3
import com.trackmap.gps.network.client.ApiClient
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import com.trackmap.gps.report.ui.TemplateActivity
import com.trackmap.gps.usersettings.model.GetNotificationStatusRequest
import com.trackmap.gps.usersettings.model.SetNotificationRequest
import com.trackmap.gps.usersettings.model.SignoutRequest
import com.trackmap.gps.utils.DebugLog
import com.trackmap.gps.utils.NetworkUtil
import com.trackmap.gps.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.awaitResponse
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class UserSettingFragment : BaseFragment() {

    lateinit var binding: FragmentUserSettingBinding
    var setNotificationRequest: SetNotificationRequest? = null
    var signOutRequest: SignoutRequest? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_user_setting, container, false)
        // Inflate the layout for this fragment
        // if(MyPreference.getValueBoolean(PrefKey.IS_CEO_DATA, false))
        if (Locale.getDefault().displayLanguage.equals("العربية")) {
            binding.imgChangePasswordArrow.rotation = 270F
            binding.imgmapTypeArrow.rotation = 270F

        } else {
            binding.imgChangePasswordArrow.rotation = 90F
            binding.imgmapTypeArrow.rotation = 90F

        }
        if (serverData.contains("s3"))
            getNotificationStatusGps3()
        else
            getNotificationStatus()
        binding.constChangePassword.setOnClickListener {
            findNavController().navigate(R.id.action_userSettingFragment_to_changePasswordFragment)
        }
        binding.btnSignOut.setOnClickListener {
            Log.d(
                TAG,
                "okhttp SignOut token: ${
                    MyPreference.getValueString(PrefKey.FCM_TOKEN, "").toString()
                }"
            )
//             var checkFlag = MyPreference.getValueBoolean(PrefKey.IS_CEO_DATA, false)
            if (serverData.contains("s3")) {
                callApiForSignOutGps3()
            } else {
                callApiForSignOut()
            }
        }
        if (serverData.contains("s3")) {
            /*  if (DataValues.pushNotify == "true") {
                  binding.imgShowPushNotificationArrow.setImageResource(R.drawable.ic_radio_on)
                  binding.imgShowPushNotificationArrow.tag = true
              } else {
                  binding.imgShowPushNotificationArrow.setImageResource(R.drawable.ic_radio_off)
                  binding.imgShowPushNotificationArrow.tag = false
              }*/
        } else binding.imgShowPushNotificationArrow.tag = false
        binding.imgShowPushNotificationArrow.setOnClickListener {
            binding.imgShowPushNotificationArrow.tag =
                !(binding.imgShowPushNotificationArrow.tag as Boolean)
            if (binding.imgShowPushNotificationArrow.tag as Boolean) {
                binding.imgShowPushNotificationArrow.isChecked = true
                MyPreference.setValueString(PrefKey.NOTIFY, "true")
                if (serverData.contains("s3")) {
                    callApiForSwitchOnOffGps3(true)
                } else callApiForPushNotification(true)

            } else {
                binding.imgShowPushNotificationArrow.isChecked = false
                MyPreference.setValueString(PrefKey.NOTIFY, "false")
                if (serverData.contains("s3")) {
                    callApiForSwitchOnOffGps3(false)
                } else callApiForPushNotification(false)
            }
        }
        binding.rdOnOff.isChecked = MyPreference.getValueBoolean(PrefKey.IS_CURRENT_LOCATION, false)
        binding.rdOnOff.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                MyPreference.setValueBoolean(PrefKey.IS_CURRENT_LOCATION, true)
            } else
                MyPreference.setValueBoolean(PrefKey.IS_CURRENT_LOCATION, false)
        }
        binding.rdTrafficonOff.isChecked = MyPreference.getValueBoolean(PrefKey.IS_TRAFFIC, false)
        binding.rdTrafficonOff.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                MyPreference.setValueBoolean(PrefKey.IS_TRAFFIC, true)
            } else
                MyPreference.setValueBoolean(PrefKey.IS_TRAFFIC, false)
        }
        binding.rdGeoonOff.isChecked = MyPreference.getValueBoolean(PrefKey.IS_GEO_ZONES, false)
        binding.rdGeoonOff.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                MyPreference.setValueBoolean(PrefKey.IS_GEO_ZONES, true)
            } else
                MyPreference.setValueBoolean(PrefKey.IS_GEO_ZONES, false)
        }
        binding.constMapType.setOnClickListener {
            val intent = Intent(context, TemplateActivity::class.java)
            intent.putExtra("cate_name", "type_map")
            startActivityForResult(intent, 11)
        }
        binding.cbGroupUnits.isChecked = MyPreference.getValueBoolean(PrefKey.IS_GROUP_UNITS, true)

        binding.cbGroupUnits.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                MyPreference.setValueBoolean(PrefKey.IS_GROUP_UNITS, true)
            } else
                MyPreference.setValueBoolean(PrefKey.IS_GROUP_UNITS, false)
        }
        binding.cbAddBg.isChecked = MyPreference.getValueBoolean(PrefKey.IS_ADD_BG, false)
        binding.cbAddBg.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                MyPreference.setValueBoolean(PrefKey.IS_ADD_BG, true)
            } else
                MyPreference.setValueBoolean(PrefKey.IS_ADD_BG, false)
        }
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 11) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    if (data.hasExtra("result_from")) {
                        val value = data.getIntExtra("position", 0)
                        MyPreference.setValueInt(PrefKey.MAP_TYPE, value)
                    }
                }
            }
        }
    }

    // Create a Coroutine scope using a job to be able to cancel when needed
    val viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
    val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private fun getNotificationStatus() {
        binding.imgShowPushNotificationArrow.tag =
            (MyPreference.getValueString(PrefKey.NOTIFY, "false") == "true")
        binding.imgShowPushNotificationArrow.isChecked =
            binding.imgShowPushNotificationArrow.tag as Boolean
        val client = ApiClientForBrainvire.getApiInterface()
        //Utils.showProgressBar(requireContext())
        coroutineScope.launch {

            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                Utils.hideProgressBar()
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getNotificationStatus(GetNotificationStatusRequest())

                    val rsp = response.string()
                    val jsonObject = JSONObject(rsp)

                    val metaObj = jsonObject.getJSONObject("meta")
                    val statusCode = metaObj.getString("status_code")
                    if (statusCode.equals("200", true)) {
                        val message = metaObj.getString("message")
                        val data = jsonObject.getJSONObject("data")
                        val notificationStatus = data.getString("notification_status")
                        binding.imgShowPushNotificationArrow.tag =
                            notificationStatus.toString().equals("enable", true)
                        MyPreference.setValueString(
                            PrefKey.NOTIFY,
                            binding.imgShowPushNotificationArrow.tag.toString()
                        )
                        DebugLog.d(notificationStatus)
                        binding.imgShowPushNotificationArrow.isChecked =
                            binding.imgShowPushNotificationArrow.tag as Boolean

                        val days = data.getString("days")
                        if (!days.toString().equals("null", true)) {
                            binding.txtRenewAmount.text = days
                        } else
                            binding.txtRenewAmount.text = "0"
                    }

                } catch (error: Throwable) {
                    Utils.hideProgressBar()
                }
            }
        }
    }

    private fun getNotificationStatusGps3() {
        binding.imgShowPushNotificationArrow.tag =
            MyPreference.getValueString(PrefKey.NOTIFY, "false") == "true"
        binding.imgShowPushNotificationArrow.isChecked =
            binding.imgShowPushNotificationArrow.tag as Boolean
        coroutineScope.launch {
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                Utils.hideProgressBar()
            } else {
                try {
                    val client = ApiClient.getApiClient()
                    val data = mutableMapOf<String, Any>(
                        "service" to "user",
                        "api_key" to DataValues.apiKey
                    )
                    val param = JSONObject(data as Map<*, *>).toString()
                    // this will run on a thread managed by Retrofit
                    val response = client.getNotificationStatusGps3(param)
                    val jsonObject = JSONObject(response.string())
                    val metaObj = jsonObject.getJSONObject("data")
                    val status = jsonObject.getBoolean("status")
                    val statusCode = metaObj.getString("push_notify_mobile")
                    MyPreference.setValueString(PrefKey.NOTIFY, statusCode)

                    if (status) {
                        binding.imgShowPushNotificationArrow.tag =
                            statusCode.toString().equals("true", true)
                        binding.imgShowPushNotificationArrow.isChecked =
                            binding.imgShowPushNotificationArrow.tag as Boolean

                        /*val days = metaObj.getString("days")
                        if (!days.toString().equals("null", true)) {
                            binding.txtRenewAmount.text = days
                        } else
                            binding.txtRenewAmount.text = "0"*/
                    }

                } catch (error: Throwable) {
                    Log.d(TAG, "getNotificationStatusGps3: e ${error.message}")
                    Utils.hideProgressBar()
                }
            }
        }
    }

    private fun callApiForSignOutGps3() {
        val viewModelJob = Job()
        // the Coroutine runs using the Main (UI) dispatcher
        val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)
        ApiClientForGps3.initRetrofit()
        val client = ApiClientForGps3.getApiInterface()
        Utils.showProgressBar(requireContext())
        if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
            Toast.makeText(
                requireContext(),
                getString(R.string.internet_connection_slow_or_disconnected), Toast.LENGTH_SHORT
            ).show()
            Utils.hideProgressBar()
        } else {
            coroutineScope.launch {
                Log.d(TAG, "callApiForSignOutGps3: ss")
                try {
                    val token = MyPreference.getValueString(PrefKey.FCM_TOKEN, "")!!
                    val call = client.deleteTokenGps3(username, token)!!.awaitResponse()
                    if (call.isSuccessful) {
                        if (!call.body()!!.status) {
                            try {
                                Toast.makeText(context, call.body()!!.msg, Toast.LENGTH_SHORT)
                                    .show()
                            } catch (e: Exception) {
                                Log.e(TAG, "callApiForSignOutGps3: CATCH ${e.message}")
                            }
                        }
                        Log.d(TAG, "callApiForSignOutGps3: ${ call.body()!!.msg}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "catch callApiForSignOutGps3: ${e.message}")
                }
                Utils.hideProgressBar()
                MyPreference.clearAllData()
                val myIntent = Intent(activity, LoginActivity::class.java)
                myIntent.putExtra("logoutFromApp", true)
                startActivity(myIntent)
                requireActivity().finish()
            }
        }
    }

    private fun callApiForSignOut() {
        // Create a Coroutine scope using a job to be able to cancel when needed
        val viewModelJob = Job()
        // the Coroutine runs using the Main (UI) dispatcher
        val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
        val client = ApiClientForBrainvire.getApiInterface()

        Utils.showProgressBar(requireContext())

        signOutRequest = SignoutRequest()
        signOutRequest!!.deviceToken = MyPreference.getValueString(PrefKey.FCM_TOKEN, "")
        if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
            Toast.makeText(
                requireContext(),
                getString(R.string.internet_connection_slow_or_disconnected), Toast.LENGTH_SHORT
            ).show()
            Utils.hideProgressBar()

        } else {
            coroutineScope.launch {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.callSignOut(signOutRequest)
                    DebugLog.d("ResponseBody callSignIn 1 = $signOutRequest")
                    val rsp = response.string()
                    val jsonObject = JSONObject(rsp)

                    val metaObj = jsonObject.getJSONObject("meta")
                    var message = metaObj.getString("message")
                    var code = metaObj.getInt("status_code")
                    val status = metaObj.getBoolean("status")
                    var messageCode = metaObj.getString("message_code")
                    if (status) {
                        Utils.hideProgressBar()
                        MyPreference.clearAllData()
                        val myIntent = Intent(activity, LoginActivity::class.java)
                        myIntent.putExtra("logoutFromApp", true)
                        startActivity(myIntent)
                        requireActivity().finish()
                    }

                } catch (error: Throwable) {
                    DebugLog.d("ResponseBody callSignIn = $error")
                    Utils.hideProgressBar()
                    MyPreference.clearAllData()
                    val myIntent = Intent(activity, LoginActivity::class.java)
                    myIntent.putExtra("logoutFromApp", true)
                    startActivity(myIntent)
                    requireActivity().finish()
                }
            }
        }
    }

    private fun callApiForSwitchOnOffGps3(status: Boolean) {
        val viewModelJob = Job()
        // the Coroutine runs using the Main (UI) dispatcher
        CoroutineScope(viewModelJob + Dispatchers.Main).launch {
            Utils.showProgressBar(requireContext())
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                Utils.hideProgressBar()
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = ApiClient.getApiClient().getSwitchOnOffDataGps3(
                        "{\"service\":\"update_event\",\"api_key\":\"" +
                                MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "") +
                                "\",\"active\":\"" + status + "\"}"
                    ).awaitResponse()
                    if (response.isSuccessful) {
                        if (response.body()!!.status) {
                            Utils.hideProgressBar()

                        } else {
                            Utils.hideProgressBar()
                        }
                    } else {
                        Toast.makeText(
                            AppBase.instance,
                            "no data " + response.message(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Utils.hideProgressBar()
                } catch (error: Throwable) {
                    Utils.hideProgressBar()
                }
            }
        }
    }


    private val TAG = "UserSettingFragment"

    private fun callApiForPushNotification(isSetOnOff: Boolean) {
        // Create a Coroutine scope using a job to be able to cancel when needed
        val viewModelJob = Job()
        // the Coroutine runs using the Main (UI) dispatcher
        val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
        Utils.showProgressBar(requireContext())
        setNotificationRequest =
            SetNotificationRequest(isSetOnOff, MyPreference.getValueString(PrefKey.FCM_TOKEN, ""))
//        setNotificationRequest?.isSentNotification = isSetOnOff
        coroutineScope.launch {
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                Utils.hideProgressBar()
            } else {

                try {
                    // this will run on a thread managed by Retrofit
                    val client = ApiClientForBrainvire.getApiInterface()
                    val response = client.callSetNotification(
                        isSetOnOff,
                        MyPreference.getValueString(PrefKey.FCM_TOKEN, "")!!
                    )
                    val rsp = response.string()
                    val jsonObject = JSONObject(rsp)
                    val metaObj = jsonObject.getJSONObject("meta")
                    val message = metaObj.getString("message")
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onResume: ttkn 1")
                    Utils.hideProgressBar()
                } catch (error: Throwable) {
                    error.printStackTrace()
                    Log.d(
                        TAG,
                        "onResume: ttkn 1 err ${
                            ApiClientForBrainvire.getRetrofit().baseUrl()
                        } ${error.message} ${error.stackTrace} \n${setNotificationRequest!!.device_token} ${setNotificationRequest!!.isSentNotification} "
                    )
                    Toast.makeText(requireContext(), "Error ${error.message}", Toast.LENGTH_SHORT)
                        .show()
                    Utils.hideProgressBar()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handleActionBarHidePlusIcon(R.string.setting)
    }
}
