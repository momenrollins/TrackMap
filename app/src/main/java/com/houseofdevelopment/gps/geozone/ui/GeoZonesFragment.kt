package com.houseofdevelopment.gps.geozone.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.houseofdevelopment.gps.MainActivity
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.base.BaseFragment
import com.houseofdevelopment.gps.databinding.FragmentGeozoneBinding
import com.houseofdevelopment.gps.geozone.model.GeoZonData
import com.houseofdevelopment.gps.geozone.model.GeoZoneListModel
import com.houseofdevelopment.gps.geozone.model.GeoZoneModelItemGps3
import com.houseofdevelopment.gps.geozone.viewmodel.GeoZoneViewModel
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey
import com.houseofdevelopment.gps.utils.Utils
import com.houseofdevelopment.gps.vehicallist.model.GroupListDataModel
import kotlinx.android.synthetic.main.layout_custom_action_bar.*
import org.json.JSONObject
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class GeoZonesFragment : BaseFragment() {

    private var coming_from: String = ""
    lateinit var binding: FragmentGeozoneBinding
    lateinit var viewmodel: GeoZoneViewModel
    lateinit var adapter: GeoZoneAdapter
    lateinit var adapterGps3: GeoZoneAdapterGps3
    private var isZoneCreated: Boolean = false
    private var isZoneDeleted: Boolean = false

    var geoNameList: MutableList<String>? = null
    private var geoZoneList: MutableList<GeoZoneListModel.Item>? = null
    private var geoZoneListGps3: ArrayList<GeoZoneModelItemGps3>? = null
    private var hs = mutableSetOf<String>()
    var zlObjectList = mutableListOf<GeoZoneListModel.ZLObj>()
    private var geozone_ids = ArrayList<String>()
    private var isFromNotification: Boolean = false
    private var typeFrom: String = ""
    private var serverData: String = ""
    private lateinit var units: ArrayList<ArrayList<String>>
    var recyclerViewState: Parcelable? = null
    var cFrom = ""
    private lateinit var dashID: ArrayList<String>
    var unitsMode = GroupListDataModel.Item()
    var filterListGps3 = mutableListOf<GeoZoneModelItemGps3>()

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_geozone, container, false)
        viewmodel = ViewModelProviders.of(this).get(GeoZoneViewModel::class.java)
        serverData = MyPreference.getValueString(PrefKey.SELECTED_SERVER_DATA, "")!!

        binding.lifecycleOwner = this

        if (arguments != null) {
            cFrom = arguments!!.getString("comingFrom").toString()
            if (cFrom.equals("Dash", true))
            unitsMode = arguments?.getSerializable("groupName") as GroupListDataModel.Item


            if (cFrom.equals("notification", true)) {
                (activity as MainActivity).add_vehicle.visibility = View.INVISIBLE

                var str = arguments!!.getString("type")
                if (str.equals("geoZoneEnterNotification")) {
                    typeFrom = "geoZoneEnterNotification"
                    coming_from = "notification"
                } else if (str.equals("geoZoneExitNotification")) {
                    typeFrom = "geoZoneExitNotification"
                    coming_from = "notification"
                }
            }
        }

        addObserver()
        initListeners()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("FragmentLiveDataObserve")
    private fun addObserver() {
        if (serverData.contains("s3")) {
            viewmodel.callApiForGetGeoZonesGps3()

            viewmodel.geoZoneListGps3.observe(this) {
                if(it !=null){
                    Utils.hideProgressBar()
                    if (it.getStatus()) {
                        geoZoneListGps3 = it.data as ArrayList<GeoZoneModelItemGps3>?
                        filterListGps3.clear()
                        filterListGps3.addAll(geoZoneListGps3!!)
                        getZoneCars(geoZoneListGps3)
                        if (geoZoneListGps3!!.size > 0) {
                            binding.noDataLay.visibility = View.GONE
                            binding.rvGeoZone.visibility = View.VISIBLE
                        } else {
                            binding.noDataLay.visibility = View.VISIBLE
                            binding.rvGeoZone.visibility = View.GONE
                        }
                    } else {
                        binding.noDataLay.visibility = View.VISIBLE
                        binding.rvGeoZone.visibility = View.GONE
                    }
                }
                viewmodel.geoZoneListGps3.postValue(null)
            }


        } else {
            viewmodel.callApiForGeoZoneListData()

            viewmodel.geoZoneList.observe(this) {

                if (it != null){
                    Utils.hideProgressBar()
                    geoZoneList = it as MutableList<GeoZoneListModel.Item>?

                    hs = HashSet<String>()
                    val nameList = mutableListOf<String>()
                    val colorList = mutableListOf<String>()
                    val idList = mutableListOf<String>()

                    var gList = mutableListOf<GeoZoneListModel.ZLObj>()

                    if (geoZoneList != null && geoZoneList!!.size > 0) {

                        for (md in geoZoneList!!) {

                            var zl = md.zl

                            var gson = Gson()
                            var str = gson.toJson(zl)

                            var jsonObject = JSONObject(str)

                            val keys = jsonObject.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                hs.add(key)

                                var value = jsonObject.get(key)
                                if (value is JSONObject) {
                                    var name = value.getString("n")
                                    nameList.add(name)

                                    var color = value.getString("c")
                                    colorList.add(color)
                                    Log.d(TAG, "addObserver: COLOR $color")

                                    var id = value.getString("id")
                                    idList.add(id)

                                    // store data into model object
                                    var zlObject = GeoZoneListModel().ZLObj()
                                    zlObject.n = value.getString("n")
                                    zlObject.c = value.getLong("c")
                                    zlObject.id = value.getInt("id")
                                    zlObject.w = value.getDouble("w")

                                    val obj = value.getJSONObject("b")

                                    val bModel = GeoZoneListModel.BModel()
                                    bModel.cen_x = obj.getDouble("cen_x")
                                    bModel.cen_y = obj.getDouble("cen_y")
                                    bModel.min_x = obj.getDouble("min_x")
                                    bModel.min_y = obj.getDouble("min_y")
                                    bModel.max_x = obj.getDouble("max_x")
                                    bModel.max_y = obj.getDouble("max_y")

                                    zlObject.b = bModel

                                    gList.add(zlObject)
                                }
                            }
                        }
                        zlObjectList.clear()
                        zlObjectList.addAll(gList)
//                geoNameList!!.addAll(nameList)

//                initializeAdapter(nameList, colorList, idList)


//                    initZoneUnitList(zlObjectList)
                        initializeAdapter(zlObjectList)
                        binding.noDataLay.visibility = View.GONE
                        if (zlObjectList.size > 0) {
                            binding.noDataLay.visibility = View.GONE
                            binding.rvGeoZone.visibility = View.VISIBLE
                        } else {
                            binding.noDataLay.visibility = View.VISIBLE
                            binding.rvGeoZone.visibility = View.GONE
                        }
                    } else {
                        binding.noDataLay.visibility = View.VISIBLE
                        binding.rvGeoZone.visibility = View.GONE
                    }
                }
                viewmodel.geoZoneList.postValue(null)
            }


        }
        viewmodel.isGeoZoneAdded.observe(this) {
            Utils.hideProgressBar()
            isZoneCreated = it

            if (isZoneCreated) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.zone_added_successfully),
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigate(R.id.action_homemapFragment_to_notificationHome)
            }
        }
    }

    //    @RequiresApi(Build.VERSION_CODES.N)
    private fun getZoneCars(geoZoneListGps3: MutableList<GeoZoneModelItemGps3>?) {
        var unitList = ArrayList<String>()
        val latlanList = ArrayList<LatLng>()
        val cars = ArrayList<ArrayList<String>>()
        var map: GoogleMap? = null

        geoZoneListGps3!!.forEach { geoZoneModelItemGps3 ->

            latlanList.clear()
            geoZoneModelItemGps3.zone_vertices.forEach { geoZoneVerticesGps3 ->
                latlanList.add(
                    LatLng(
                        geoZoneVerticesGps3.lat.toDouble(),
                        geoZoneVerticesGps3.lng.toDouble()
                    )
                );
            }
           /* unitList = ArrayList()
            for (item in Utils.getCarListingDataGps3(context!!).items) {
                var nside = PolyUtil.containsLocation(
                    LatLng(item.lat.toDouble(), item.lng.toDouble()),
                    latlanList,
                    true
                );

                if (nside) {
                    unitList.add(item.imei)

                }
            }
            geoZoneModelItemGps3.setZone_cars(unitList)*/
//            Log.d(TAG, "getZoneCars: ${geoZoneModelItemGps3.zone_cars.size}")

        }
        initializeAdapterGps3(geoZoneListGps3!!)


    }

    private fun initZoneUnitList(zlObjectList: MutableList<GeoZoneListModel.ZLObj>) {
        zlObjectList.forEach { zlObj ->
            try {
                val unitList = ArrayList<String>()
                for (item in Utils.getCarListingData(context!!).items) {
                    if (item.pos != null) {

                        if (item.pos.y!! >= zlObj.b.min_y && item.pos?.x!! >= zlObj.b.min_x &&
                            item.pos?.y!! <= zlObj.b.max_y && item.pos?.x!! <= zlObj.b.max_x
                        ) {
                            unitList.add(item.id.toString())
                            /* zoneUnitsList.add(item.id.toString())
                             counter++;*/

                        }
                    }
                }
                zlObj.setUnitIds(unitList)
            } catch (e: Exception) {
                Log.d(TAG, "bind: CATCH ${e.message}")
            }
        }
    }

    private val TAG = "GeoZonesFragment"

    @SuppressLint("UseRequireInsteadOfGet")
    private fun initListeners() {
        binding.btnShowOnMap.setOnClickListener {
            if (serverData.contains("s3")) {
                try {
                    geozone_ids = adapterGps3.getArrayList()
                } catch (e: Exception) {
                    Log.e(TAG, "initListeners: CATCH ⚠️ ${e.message}")
                }
            } else {
                try {
                    geozone_ids = adapter.getArrayList()
                } catch (e: Exception) {
                    Log.e(TAG, "initListeners: CATCH ⚠️ ${e.message}")
                }
            }
            if (geozone_ids.isEmpty()) {
                Toast.makeText(context!!, "Zone list is empty", Toast.LENGTH_SHORT).show()
            } else {
                if (typeFrom.equals("geoZoneEnterNotification", true)) {
                    val bundle = bundleOf(
                        "comingFrom" to "geoZoneEnterNotification",
                        "geoZoneId" to geozone_ids
                    )
                    findNavController()?.navigate(
                        R.id.action_geoZoneFragment_to_addNotification,
                        bundle
                    )
                } else if (typeFrom.equals("geoZoneExitNotification", true)) {
                    val bundle = bundleOf(
                        "comingFrom" to "geoZoneExitNotification",
                        "geoZoneId" to geozone_ids
                    )
                    findNavController()?.navigate(
                        R.id.action_geoZoneFragment_to_addNotification,
                        bundle
                    )
                }

            }
            /* findNavController().navigate(
                     GeoZonesFragmentDirections.actionGeoZoneFragmentToAddunitsFragment(coming_from))*/
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {

                try {
                    if (serverData.contains("s3")) {
                        filterListGps3 = mutableListOf<GeoZoneModelItemGps3>()
                        for (name in geoZoneListGps3!!) {
                            if (name.zone_name.lowercase()
                                    .contains(editable.toString().lowercase())
                            ) {
                                filterListGps3.add(name)
                            }
                        }

                        adapterGps3.updateList(filterListGps3)
                    } else {
                        var filterList = mutableListOf<GeoZoneListModel.ZLObj>()
                        for (name in zlObjectList!!) {
                            if (name.n.toLowerCase().contains(editable.toString().toLowerCase())) {
                                filterList.add(name)
                            }
                        }

                        adapter.updateList(filterList)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "afterTextChanged: CATCH ${e.message} ⚠️")
                }
                // Utils.hideSoftKeyboard(activity!!)
            }
        })
        (activity as MainActivity).add_vehicle.setOnClickListener {
            findNavController().navigate(R.id.action_geoZoneFragment_to_addGeoZoneFragment)
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun initializeAdapter(zlObjectList: MutableList<GeoZoneListModel.ZLObj>) {
        val handler = object : GeoZoneAdapter.SelectedZoneOnclick {
            override fun onClick(zlObj: GeoZoneListModel.ZLObj) {
                binding.btnShowOnMap.isEnabled = true
                binding.btnShowOnMap.setTextColor(
                    ContextCompat.getColor(
                        context!!,
                        R.color.color_white
                    )
                )
                // findNavController().navigate(R.id.action_geoZoneFragment_to_geoZoneMapFragment)

                Log.d(TAG, "onClick:zoooneId ${zlObj.id} ")
                val geoZonData = GeoZonData()
                geoZonData.colorCode = zlObj.c.toInt()
                geoZonData.centerLatLong = LatLng(zlObj.b.cen_y, zlObj.b.cen_x)
                geoZonData.minLatLong = LatLng(zlObj.b.min_y, zlObj.b.min_x)
                geoZonData.maxLatLong = LatLng(zlObj.b.max_y, zlObj.b.max_x)
                geoZonData.radius = zlObj.w
                geoZonData.id = zlObj.id.toString()




                if (cFrom.equals("Dash", true)) {

                    unitsMode.nm = zlObj.n
                    unitsMode.u = zlObj.getUnitIds()
                    val bundle = bundleOf(
                        "groupName" to unitsMode,
                        "dashTitle" to zlObj.n,
                        "comingFrom" to "GeoZonesFragment"
                    )
                    findNavController().navigate(
                        R.id.groupUnitListFragment,
                        bundle
                    )
                } else
                    findNavController().navigate(
                        GeoZonesFragmentDirections.actionGeoZoneFragmentToGeoZoneMapFragment(
                            geoZonData
                        )
                    )
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onDeleteClick(selName: String, selId: String, position: Int) {
                openDeleteDialog(position, selId, selName, zlObjectList1 = adapter.getList())
            }
        }

//        adapter = GeoZoneAdapter(coming_from, handler,context!!, zoneList, colorList, idList)
        adapter = GeoZoneAdapter(coming_from, handler, context!!)
        binding.rvGeoZone.layoutManager =
            LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)
        binding.rvGeoZone.adapter = adapter

        adapter.addAll(zlObjectList!!)

    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun initializeAdapterGps3(geoZoneList: MutableList<GeoZoneModelItemGps3>) {
        val handlerGps3 = object : GeoZoneAdapterGps3.SelectedZoneOnclickGps3 {
            override fun onClick(geoZoneItem: GeoZoneModelItemGps3) {

                binding.btnShowOnMap.isEnabled = true
                binding.btnShowOnMap.setTextColor(
                    ContextCompat.getColor(
                        context!!,
                        R.color.color_white
                    )
                )
                // findNavController().navigate(R.id.action_geoZoneFragment_to_geoZoneMapFragment)


                findNavController().navigate(
                    GeoZonesFragmentDirections.actionGeoZoneFragmentToGeoZoneMapFragmentGps3(
                        geoZoneItem
                    )
                )
            }


            override fun onDeleteClick(zoneId: String, postion: Int) {
                openDeleteDialog(postion, zoneId, geoZoneList = geoZoneList)

            }
        }

//        adapter = GeoZoneAdapter(coming_from, handler,context!!, zoneList, colorList, idList)
        adapterGps3 = GeoZoneAdapterGps3(coming_from, handlerGps3, context!!)
        binding.rvGeoZone.layoutManager =
            LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)
        binding.rvGeoZone.adapter = adapterGps3

        adapterGps3.addAll(geoZoneList!!)

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun openDeleteDialog(
        position: Int,
        zoneId: String = "",
        selName: String = "",
        geoZoneList: MutableList<GeoZoneModelItemGps3> = ArrayList(),
        zlObjectList1: MutableList<GeoZoneListModel.ZLObj> = ArrayList()
    ) {
        val builder = AlertDialog.Builder(requireContext())
        //set title for alert dialog
        builder.setTitle(getString(R.string.delete_zone))
        //set message for alert dialog
        builder.setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_zone))

        //performing positive action
        builder.setPositiveButton(getString(R.string.yes)) { dialogInterface, which ->
            if (serverData.contains("s3")) {
                viewmodel.callApiForDeleteGeoZonesGps3(zoneId)
                if (filterListGps3.isNotEmpty()) {
                    val itemGps3 = filterListGps3[position]
                    filterListGps3.remove(itemGps3)
                    geoZoneList.remove(itemGps3)
                } else geoZoneList.removeAt(position) //here
                adapterGps3.notifyDataSetChanged()
                viewmodel.geoZoneDeletedGps3.observeOnce { b: Boolean? ->
                    Utils.hideProgressBar()
                    if (b == true) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.zone_deleted_successfully),
                            Toast.LENGTH_SHORT
                        ).show()
                        adapterGps3.notifyDataSetChanged()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                viewmodel.callApiForDeleteGeoZone(selName, zoneId)
                var item=zlObjectList1[position]

                zlObjectList1?.remove(item)
                zlObjectList?.remove(item)

                viewmodel.isGeoZoneDeleted.observeOnce { b: Boolean? ->
                    Utils.hideProgressBar()
                    if (b == true) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.zone_deleted_successfully),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                adapter.notifyDataSetChanged()
            }
            dialogInterface.dismiss()


        }
        //performing negative action
        builder.setNegativeButton(getString(R.string.no)) { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    fun <T> LiveData<T>.observeOnce(observer: (T) -> Unit) {
        observeForever(object : Observer<T> {
            override fun onChanged(value: T) {
                removeObserver(this)
                observer(value)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        Utils.showProgressBar(requireContext())
        if (coming_from.equals("notification", true)) {
            (activity as MainActivity).add_vehicle.visibility = View.INVISIBLE
            binding.showOnMap.visibility = View.VISIBLE
            isFromNotification = true
            binding.btnShowOnMap.text = getString(R.string.add_unit_for_geozone_notifications)
            binding.btnShowOnMap.isEnabled = true
            binding.btnShowOnMap.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_white
                )
            )

            handleActionBarHidePlusIcon(R.string.geo_zone)
        } else {
            handleActionBar(R.string.geo_zone)
            (activity as MainActivity).add_vehicle.visibility = View.VISIBLE
        }
    }

}
