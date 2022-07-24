package com.houseofdevelopment.gps.dashboard.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.houseofdevelopment.gps.DataValues

import com.houseofdevelopment.gps.DataValues.serverData
import com.houseofdevelopment.gps.DataValues.tz
import com.houseofdevelopment.gps.MainActivity
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.base.BaseFragment
import com.houseofdevelopment.gps.dashboard.adapter.DashboardAdapter
import com.houseofdevelopment.gps.databinding.ActivityDashboardBinding
import com.houseofdevelopment.gps.geozone.viewmodel.GeoZoneViewModel
import com.houseofdevelopment.gps.utils.Utils
import com.houseofdevelopment.gps.vehicallist.model.GroupImeisModelGps3
import com.houseofdevelopment.gps.vehicallist.model.GroupListDataModel
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DashboardActivity : BaseFragment() {

    lateinit var binding: ActivityDashboardBinding
    var adapter: DashboardAdapter? = null
    lateinit var viewmodel: GeoZoneViewModel

    private val dashImages = arrayListOf(
        R.drawable.dash_power_on,
        R.drawable.dash_power_off,
        R.drawable.dash_stop,
        R.drawable.dash_stop_sens,
        R.drawable.dash_speed,
        R.drawable.dash_zone,
        R.drawable.dash_no_state,
        R.drawable.dash_no_msgs,
    )

    private lateinit var dashTitle: ArrayList<String>
    private val dashColors = arrayListOf(
        R.color.dash_green,
        R.color.dash_grey,
        R.color.dash_red,
        R.color.dash_red,
        R.color.dash_green,
        R.color.dash_blue,
        R.color.dash_grey,
        R.color.dash_grey,
    )
    val onlineList = ArrayList<String>()
    var offlineList = ArrayList<String>()
    val stationaryList = ArrayList<String>()
    val movingList = ArrayList<String>()
    val noStateList = ArrayList<String>()
    val stationaryListWIthEngine = ArrayList<String>()
    val zoneList = ArrayList<String>()
    val noMessage = ArrayList<String>()
    val dashItemList = ArrayList<String>()
    val listOfLists = ArrayList<ArrayList<String>>()
    var unitsMode: GroupListDataModel.Item = GroupListDataModel.Item()
    val imeisModel: GroupImeisModelGps3=GroupImeisModelGps3()

    private val TAG = "DashboardActivity"

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.activity_dashboard, container, false
        )
        viewmodel = ViewModelProvider(this)[GeoZoneViewModel::class.java]
        dashTitle = arrayListOf(
            getString(R.string.online),
            getString(R.string.offline),
            getString(R.string.stationary),
            getString(R.string.stationary_with_ignition_on),
            getString(R.string.moving),
            getString(R.string.geo_zone),
            getString(R.string.no_actual_state),
            getString(R.string.no_messages),
        )
        handleActionBarAString(getString(R.string.drawer_dashboard))
        Utils.showProgressBar(context!!)

        if (DataValues.serverData.contains("s3")) {
            intiatRecycler()

            initRecyclerGps3()

        } else {
            viewmodel.callApiForGeoZoneListData()
            viewmodel.geoZoneList.observe(viewLifecycleOwner) {
                if (it != null) {
                    zoneList.clear()
                    for (item in it) {
                        val gson = Gson()
                        val str = gson.toJson(item.zl)
                        val jsonObject = JSONObject(str)
                        val keys = jsonObject.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            zoneList.add(key)
                        }
                    }
                }
                intiatRecycler()
                Utils.hideProgressBar()
            }
        }

        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTimeStamp(dateString: String?): Long {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString!!)
        return date!!.time
    }

    private fun initRecyclerGps3() {
        (requireActivity() as MainActivity).homeMapViewModel.carCarDetailsGPS3.observe(
            viewLifecycleOwner
        ) {
            Log.d(TAG, "getGroupUnitList: selectedCarListGps3 SIZEss ${it.size}")
            clearLists()
            val items = Utils.getCarListingDataGps3(requireContext()).items
            for (item in items) {
                val timeStamp: Long = getTimeStamp(item.dt_server)
                if (((Calendar.getInstance(TimeZone.getTimeZone(tz.id)).timeInMillis / 1000) - ((timeStamp) + tz.getOffset(
                        timeStamp
                    )) / 1000) / 60 < 11
                ) {
                    Log.d(TAG, "onCreateView: online ${item.name}")
                    onlineList.add(item.imei)
                    if (item.speed == null || item.speed == "0") {
                        /* if ((item.trip_state.toString()
                                 .equals("0", true) ||
                                     item.trip_state?.trim()!!.isEmpty()) || item.sens?.size() == 0
                         ) {*/
                        Log.d(TAG, "initRecyclerGps3: stationaryList ${item.name}")
                        stationaryList.add(item.imei)
                        /* } else {
                             Log.d(TAG, "onCreateView:stationaryListWIthEngine ${item.nm}")
                             stationaryListWIthEngine.add(item.id.toString())
                         }*/
                    }
                    if (item.speed != null && item.speed?.toInt()!! > 0) {
                        Log.d(TAG, "onCreateView: speed ${item.name} ------- ${item.speed}")
                        movingList.add(item.imei)
                    }
                } else {
                    Log.d(TAG, "onCreateView: offline ${item.name} ------- ${item.speed}")
                    offlineList.add(item.imei)
                    /*if (item.lmsg == null) noMessage.add(item.id.toString())
                    else */noStateList.add(item.imei)
                }

            }
            listOfLists.clear()
            listOfLists.add(onlineList)
            listOfLists.add(offlineList)
            listOfLists.add(stationaryList)
            listOfLists.add(stationaryListWIthEngine)
            listOfLists.add(movingList)
            listOfLists.add(zoneList)
            listOfLists.add(noStateList)
            listOfLists.add(noMessage)
            adapter?.notifyDataSetChanged()
            Utils.hideProgressBar()
        }
        val listener = object : DashboardAdapter.itemlistener {
            override fun itemClick(position: Int) {
                unitsMode.nm = dashTitle[position]
                unitsMode.u = listOfLists[position]
                val bundle = bundleOf(
                    "groupName" to unitsMode,
                    "dashTitle" to dashTitle[position],
                    "comingFrom" to "Dash"
                )
                if (position == 5)
                    findNavController().navigate(
                        R.id.geoZoneFragment,
                        bundle
                    )
                else findNavController().navigate(
                    R.id.groupUnitListFragment,
                    bundle
                )

            }
        }
        adapter =
            DashboardAdapter(
                context,
                listener,
                dashTitle,
                listOfLists,
                dashImages,
                dashColors
            )
        binding.dashRecyclerview.adapter = adapter
    }

    private fun clearLists() {
        (onlineList.clear())
        (offlineList.clear())
        (stationaryList.clear())
        (stationaryListWIthEngine.clear())
        (movingList.clear())
        (noStateList.clear())
        (noMessage.clear())
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume:titleSize $adapter ")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun intiatRecycler() {

        if (serverData.contains("s3"))
            (requireActivity() as MainActivity).homeMapViewModel.carCarDetailsGPS3.observe(
                viewLifecycleOwner
            ) {
                (onlineList.clear())
                (offlineList.clear())
                (stationaryList.clear())
                (stationaryListWIthEngine.clear())
                (movingList.clear())
//            (movingList3.clear())
                (noStateList.clear())
                (noMessage.clear())
                val items = Utils.getCarListingDataGps3(context!!).items

                Utils.hideProgressBar()


                for (item in items) {

                    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(item.dt_server)
                    if (((Calendar.getInstance(TimeZone.getTimeZone(tz.id)).timeInMillis / 1000) - ((date.time) + tz.getOffset(
                            date.time
                        )) / 1000) / 60 < 11
                    ) {
                        Log.d(TAG, "onCreateView: online ${item.name}")
                        onlineList.add(item.imei)
                        if (item.speed == null || item.speed == "0") {

                            stationaryList.add(item.imei)


                           /* (requireActivity() as MainActivity).homeMapViewModel.sensorDetailsGps3.observe(
                                viewLifecycleOwner
                            ) {
                                if (it.data != null && it!!.data!!.size > 0)
                                    for (sensor in it.data!!) {

                                        if (sensor.name == "حالة المحرك") {

                                            if (sensor.value.toString().toDouble() > 0) {
                                                stationaryListWIthEngine.add(item.imei.toString())

                                            } else {
                                                stationaryList.add(item.imei.toString())

                                            }
                                            break
                                        }


                                    }
                            }*/


                        }else{
                            movingList.add(item.imei.toString())
                        }
                    } else {
                        offlineList.add(item.imei)
                        /*
                            if (item.lmsg == null) noMessage.add(item.id.toString())
                            else noStateList.add(item.id.toString())*/
                    }

                }
                listOfLists.clear()
                listOfLists.add(onlineList)
                listOfLists.add(offlineList)
                listOfLists.add(stationaryList)
                listOfLists.add(stationaryListWIthEngine)
                listOfLists.add(movingList)
                listOfLists.add(zoneList)
                listOfLists.add(noStateList)
                listOfLists.add(noMessage)

                for (list in listOfLists) {
                    Log.d(TAG, "onCreateView: ListSize ${list.size}")
                }
                Log.d(TAG, "onCreateView: ListSize ================")
                adapter?.notifyDataSetChanged()
            }
        else
            (requireActivity() as MainActivity).homeMapViewModel.unitList.observe(viewLifecycleOwner) {
                (requireActivity() as MainActivity).homeMapViewModel.getCarImageFromData(
                    it, requireContext()
                )
                (onlineList.clear())
                (offlineList.clear())
                (stationaryList.clear())
                (stationaryListWIthEngine.clear())
                (movingList.clear())
//            (movingList3.clear())
                (noStateList.clear())
                (noMessage.clear())
                val items = Utils.getCarListingData(context!!).items
                for (item in items) {
                    Log.d(TAG, "onCreateView: PWR ${item.nm} *${item.lmsg?.p?.pwr_ext}*")
                    if (((Calendar.getInstance().timeInMillis / 1000) - (item.trip_m?.toLong()
                            ?: 0)) / 60 < 11
                    ) {
                        Log.d(TAG, "onCreateView: online ${item.nm}")
                        onlineList.add(item.id.toString())
                        if (item.trip_curr_speed == null || item.trip_curr_speed == "0") {
                            if ((item.trip_state.toString()
                                    .equals("0", true) ||
                                        item.trip_state?.trim()!!
                                            .isEmpty()) || item.sens?.size() == 0
                            ) {
                                stationaryList.add(item.id.toString())
                            } else {
                                Log.d(TAG, "onCreateView:stationaryListWIthEngine ${item.nm}")
                                stationaryListWIthEngine.add(item.id.toString())
                            }
                        }
                    } else {
                        Log.d(
                            TAG,
                            "onCreateView: offline ${item.nm} ------- ${item.trip_curr_speed}"
                        )
                        offlineList.add(item.id.toString())
                        if (item.lmsg == null) noMessage.add(item.id.toString())
                        else noStateList.add(item.id.toString())
                    }
                    if (item.trip_curr_speed != null && item.trip_curr_speed?.toInt()!! > 0) {
                        Log.d(TAG, "onCreateView: speed ${item.nm} ------- ${item.trip_curr_speed}")
                        movingList.add(item.id.toString())
                    }
                }
                listOfLists.clear()
                listOfLists.add(onlineList)
                listOfLists.add(offlineList)
                listOfLists.add(stationaryList)
                listOfLists.add(stationaryListWIthEngine)
                listOfLists.add(movingList)
                listOfLists.add(zoneList)
                listOfLists.add(noStateList)
                listOfLists.add(noMessage)

                for (list in listOfLists) {
                    Log.d(TAG, "onCreateView: ListSize ${list.size}")
                }
                Log.d(TAG, "onCreateView: ListSize ================")
                adapter?.notifyDataSetChanged()
            }
        binding.dashRecyclerview.layoutManager = GridLayoutManager(context, 2)
        val listener = object : DashboardAdapter.itemlistener {
            override fun itemClick(position: Int) {
                var bundle:Bundle
                if (serverData.contains("s3")){
                    imeisModel.setName(dashTitle[position])
                    imeisModel.data=listOfLists[position]
                    imeisModel.status = false
                     bundle = bundleOf(
                        "groupNameGps3" to imeisModel
                    )
                }else{
                    unitsMode.nm = dashTitle[position]
                    unitsMode.u = listOfLists[position]
                    bundle = bundleOf(
                        "groupName" to unitsMode,
                        "dashTitle" to dashTitle[position],
                        "comingFrom" to "Dash"
                    )
                }
                unitsMode.nm = dashTitle[position]
                unitsMode.u = listOfLists[position]
                bundle = bundleOf(
                    "groupName" to unitsMode,
                    "dashTitle" to dashTitle[position],
                    "comingFrom" to "Dash"
                )
                if (position == 5)
                    findNavController().navigate(
                        R.id.geoZoneFragment,
                        bundle
                    )
                else findNavController().navigate(
                    R.id.groupUnitListFragment,
                    bundle
                )

            }
        }
        adapter =
            DashboardAdapter(
                context,
                listener,
                dashTitle,
                listOfLists,
                dashImages,
                dashColors
            )
        binding.dashRecyclerview.adapter = adapter

    }
}