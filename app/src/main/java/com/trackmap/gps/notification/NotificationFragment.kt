package com.trackmap.gps.notification

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trackmap.gps.DataValues
import com.trackmap.gps.DataValues.serverData
import com.trackmap.gps.DataValues.username
import com.trackmap.gps.MainActivity
import com.trackmap.gps.R
import com.trackmap.gps.base.AppBase
import com.trackmap.gps.base.BaseFragment
import com.trackmap.gps.databinding.FragmentNotificationBinding
import com.trackmap.gps.network.ApiClientForBrainvire
import com.trackmap.gps.network.ApiClientForGps3
import com.trackmap.gps.notification.adapter.NotificationAdapter
import com.trackmap.gps.notification.adapter.NotificationAdapterGps3
import com.trackmap.gps.notification.model.NotiDetailResponse
import com.trackmap.gps.notification.model.NotificationDataGps3
import com.trackmap.gps.notification.model.NotificationDetails
import com.trackmap.gps.notification.viewModel.NotificationViewModel
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.utils.Constants
import com.trackmap.gps.utils.DebugLog
import com.trackmap.gps.utils.NetworkUtil
import com.trackmap.gps.utils.Utils
import kotlinx.android.synthetic.main.layout_custom_action_bar.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.awaitResponse
import java.text.SimpleDateFormat

/**
 * A simple [Fragment] subclass.
 */
class NotificationFragment : BaseFragment() {

    lateinit var binding: FragmentNotificationBinding
    lateinit var viewmodel: NotificationViewModel
    lateinit var adapter: NotificationAdapter
    lateinit var adapterGps3: NotificationAdapterGps3
    var carId: String = ""
    var notificationDetail: NotificationDetails? = null
    var notiDetailList = mutableListOf<NotiDetailResponse>()
    var notiDetailListGps3 = mutableListOf<NotificationDataGps3>()

    var latitude: Double = 0.0
    var longitude: Double = 0.0
    private var currentPage = 1
    var totalRecords = 0
    var loadingMore = true
    var isCallApi = false

    private val TAG = "NotificationFragment"

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            val comingFrom = arguments?.getString("comingFrom")
            if (comingFrom?.equals("onClickOfVehicle")!!) {
                carId = arguments!!.getString("carId").toString()
            }
        }
        Log.d(TAG, "onCreate: ca $carId")
        if (serverData.contains("s3"))
            callApiForNotificationDetailGps3(carId)
        else
            callApiForNotificationDetail(carId, currentPage.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_notification, container, false
        )
        viewmodel = ViewModelProvider(this).get(NotificationViewModel::class.java)

        initializeAdapter()
        handleActionBar(R.string.notification)
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                if (serverData.contains("s3")) {
                    val filterList = java.util.ArrayList<NotificationDataGps3>()
                    for (model in notiDetailListGps3) {
                        val date = Utils.getDateFromMillis(
                            getTimeStamp(model.dt_tracker) + DataValues.tz.getOffset(
                                getTimeStamp(model.dt_tracker)
                            ),
                            "yyyy-MM-dd HH:mm:ss"
                        )
                        val msg =
                            "${context!!.getString(R.string.notif_title)} :  ${model.desc}\n" +
                                    "${context!!.getString(R.string.unit_name)} :  ${model.name}\n" +
                                    "${context!!.getString(R.string.in_location)} :  ${model.pos}\n" +
                                    "${context!!.getString(R.string.in_time)} :  ${date!!.trim()}\n" +
                                    "${context!!.getString(R.string.speed)} :  ${model.speed}"

                        if (msg.lowercase().trim().contains(editable.toString().lowercase())) {
                            filterList.add(model)
                        }
                    }
                    (binding.rvNotification.adapter as NotificationAdapterGps3).updateList(
                        filterList
                    )
                } else {
                    val filterList = java.util.ArrayList<NotiDetailResponse>()
                    for (name in notiDetailList) {
                        if (name.msg?.lowercase()?.trim()
                                ?.contains(editable.toString().lowercase())!!
                        ) {
                            filterList.add(name)
                        }
                    }
                    (binding.rvNotification.adapter as NotificationAdapter).updateList(filterList)
                }
            }
        })

        (activity as MainActivity).add_vehicle.visibility = View.INVISIBLE
        (activity as MainActivity).chk_check.visibility = View.INVISIBLE
        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTimeStamp(dateString: String?): Long {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString!!)
        return date!!.time
    }

    /**
     * Load more logic for pagination
     */
    private fun rvAddOnScrollListener() {
        binding.rvNotification.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val visibleItemCount =
                        (binding.rvNotification.layoutManager as LinearLayoutManager).childCount
                    val totalItemCount =
                        (binding.rvNotification.layoutManager as LinearLayoutManager).itemCount
                    val firstVisibleItem =
                        (binding.rvNotification.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    if (visibleItemCount + firstVisibleItem >= totalItemCount - 3 && loadingMore && !isCallApi && binding.rvNotification.adapter?.itemCount!! < totalRecords && binding.rvNotification.adapter?.itemCount!! < 100) {
                        isCallApi = true
                        currentPage += 1
                        if (serverData.contains("s3"))
                            callApiForNotificationDetailGps3(carId)
                        else
                            callApiForNotificationDetail(carId, currentPage.toString())
                    }
                }
            }
        })
    }

    private fun callApiForNotificationDetail(carId: String, startPage: String) {
        // Create a Coroutine scope using a job to be able to cancel when needed
        val viewModelJob = Job()
        // the Coroutine runs using the Main (UI) dispatcher
        val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
        val client = ApiClientForBrainvire.getApiInterface()

        Utils.showProgressBar(requireContext())

        notificationDetail = NotificationDetails()
        notificationDetail?.unit = carId
        notificationDetail?.start = startPage
        notificationDetail?.username = MyPreference.getValueString(Constants.USER_NAME, "")
        DebugLog.d("callApiForNotificationDetail - $startPage - ${notificationDetail!!.username}")

        coroutineScope.launch {

            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
            } else {

                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.callNotificationDetail(notificationDetail)

                    val resp = response.string()

                    val jsonObject = JSONObject(resp)
                    val metaObj = JSONObject(jsonObject.getString("meta"))
                    val code = metaObj.getString("status_code")

                    if (code.equals("200")) {
                        Utils.hideProgressBar()

                        val dataObj = JSONObject(jsonObject.getString("data"))
                        val originalObj = dataObj.getJSONObject("original")
                        totalRecords = originalObj.getInt("recordsTotal")
                        val jsonArray: JSONArray
                        jsonArray = originalObj.getJSONArray("data")
                        val detailList = mutableListOf<NotiDetailResponse>()

                        for (i in 0 until jsonArray.length()) {
                            val detailObj: JSONObject = jsonArray.get(i) as JSONObject

                            val details = NotiDetailResponse()

                            details.created_at = detailObj.getString("created_at")
                            details.time = detailObj.getString("time")
                            details.msg = detailObj.getString("msg")
                            details.unit_name = detailObj.getString("unit_name")
                            if (detailObj.getString("lat") != "null") details.lat =
                                detailObj.getString("lat").toDouble()
                            if (detailObj.getString("lat") != "null") details.longitude =
                                detailObj.getString("long").toDouble()

                            detailList.add(details)
                        }
                        if (currentPage == 1) {
                            notiDetailList.clear()
                            notiDetailList.addAll(detailList)
                            (binding.rvNotification.adapter as NotificationAdapter).updateList(
                                notiDetailList
                            )
                            binding.noDataLay.visibility = View.GONE
                            binding.rvNotification.visibility = View.VISIBLE
                        } else {
                            notiDetailList.addAll(detailList)
                            (binding.rvNotification.adapter as NotificationAdapter).updateList(
                                notiDetailList
                            )
                        }
                        if (notiDetailList.size == 0) {
                            binding.noDataLay.visibility = View.VISIBLE
                            binding.rvNotification.visibility = View.GONE
                        }
                    } else {
                        binding.noDataLay.visibility = View.VISIBLE
                        binding.rvNotification.visibility = View.GONE
                    }

                } catch (error: Throwable) {
                    DebugLog.d("ResponseBody noti-detail  = $error")
                    Utils.hideProgressBar()
                    binding.noDataLay.visibility = View.VISIBLE
                    binding.rvNotification.visibility = View.GONE

                }
            }
        }
    }

    private fun callApiForNotificationDetailGps3(carId: String) {
        // Create a Coroutine scope using a job to be able to cancel when needed
        val viewModelJob = Job()
        // the Coroutine runs using the Main (UI) dispatcher
        val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

//        ApiClientForBrainvire.getRetrofit()
        ApiClientForGps3.initRetrofit()
        val client = ApiClientForGps3.getApiInterface()

        Utils.showProgressBar(requireContext())

        coroutineScope.launch {
            Log.d(TAG, "callApiForNotificationDetailGps3: size $username $carId")

            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
            } else {

                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getNotificationsGps3(username, carId).awaitResponse()
                    Log.d(TAG, "callApiForNotificationDetailGps3: size ${response.body()!!.size}")
                    if (response.isSuccessful) {
                        Utils.hideProgressBar()
                        if (currentPage == 1) {
                            notiDetailListGps3.clear()
                            notiDetailListGps3.addAll(response.body()!!)
                            (binding.rvNotification.adapter as NotificationAdapterGps3).updateList(
                                notiDetailListGps3
                            )
                            Log.d(
                                TAG,
                                "callApiForNotificationDetailGps3: ✌ 1 ${notiDetailListGps3.size}"
                            )
                            binding.noDataLay.visibility = View.GONE
                            binding.rvNotification.visibility = View.VISIBLE
                        } else {
                            notiDetailListGps3.addAll(response.body()!!)
                            (binding.rvNotification.adapter as NotificationAdapterGps3).updateList(
                                notiDetailListGps3
                            )
                            Log.d(
                                TAG,
                                "callApiForNotificationDetailGps3: ✌ 2 ${notiDetailListGps3.size}"
                            )
                        }
                        if (notiDetailListGps3.size == 0) {
                            binding.noDataLay.visibility = View.VISIBLE
                            binding.rvNotification.visibility = View.GONE
                        }
                    } else {
                        binding.noDataLay.visibility = View.VISIBLE
                        binding.rvNotification.visibility = View.GONE
                    }

                } catch (error: Throwable) {
                    DebugLog.d("ResponseBody noti-detail  = ${error.message}")
                    Utils.hideProgressBar()
                    binding.noDataLay.visibility = View.VISIBLE
                    binding.rvNotification.visibility = View.GONE

                }
            }
        }
    }

    private fun initializeAdapter() {

        if (serverData.contains("s3")) {
            val handlerGps3 = object : NotificationAdapterGps3.NotificationItemClick {
                override fun itemClick(
                    position: Int, title: String, msg: String, time: String,
                    lat: Double, longitude: Double
                ) {

                    val bundle = bundleOf(
                        "comingFrom" to "notification",
                        "title" to title,
                        "msg" to msg,
                        "time" to time,
                        "lat" to lat,
                        "long" to longitude
                    )
                    findNavController().navigate(
                        R.id.action_notificationFragment_to_notificationDetailMapFragment,
                        bundle
                    )
                }
            }
            adapterGps3 = NotificationAdapterGps3(requireContext(), handlerGps3, notiDetailListGps3)
            binding.rvNotification.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.rvNotification.adapter = adapterGps3
        } else {
            val handler = object : NotificationAdapter.NotificationItemClick {
                override fun itemClick(
                    position: Int, title: String, msg: String, time: String,
                    lat: Double, longitude: Double
                ) {
                    val bundle = bundleOf(
                        "comingFrom" to "notification",
                        "title" to title,
                        "msg" to msg,
                        "time" to time,
                        "lat" to lat,
                        "long" to longitude
                    )
                    findNavController().navigate(
                        R.id.action_notificationFragment_to_notificationDetailMapFragment,
                        bundle
                    )
                }
            }
            adapter = NotificationAdapter(handler, notiDetailList)
            binding.rvNotification.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.rvNotification.adapter = adapter
        }

        rvAddOnScrollListener()
    }
}
