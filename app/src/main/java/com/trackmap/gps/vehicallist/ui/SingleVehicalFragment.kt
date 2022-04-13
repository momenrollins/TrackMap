package com.trackmap.gps.vehicallist.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.trackmap.gps.DataValues.isFromGroup
import com.trackmap.gps.MainActivity
import com.trackmap.gps.R
import com.trackmap.gps.base.BaseFragment
import com.trackmap.gps.databinding.FragmentSingleVehicleBinding
import com.trackmap.gps.homemap.model.Item
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import com.trackmap.gps.utils.DebugLog
import com.trackmap.gps.utils.Utils
import com.trackmap.gps.vehicallist.adapter.SingleCarAdapter
import com.trackmap.gps.vehicallist.adapter.SingleCarAdapterGps3
import com.trackmap.gps.vehicallist.model.ItemGps3
import com.trackmap.gps.vehicallist.viewmodel.VehiclesListViewModel
import kotlinx.android.synthetic.main.layout_custom_action_bar.*

class SingleVehicalFragment :
    BaseFragment() {

    private var vehicleList: ArrayList<Item> = ArrayList()
    private var vehicleListGps3: ArrayList<ItemGps3> = ArrayList()
    lateinit var binding: FragmentSingleVehicleBinding
    lateinit var viewmodel: VehiclesListViewModel
    var recyclerViewState: Parcelable? = null

    lateinit var adapter: SingleCarAdapter
    lateinit var adapterGps3: SingleCarAdapterGps3
    private var carId = ArrayList<String>()
    private var serverData: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serverData = MyPreference.getValueString(PrefKey.SELECTED_SERVER_DATA, "").toString()
        if (serverData.contains("s3")) {
            vehicleListGps3 = ArrayList()
            vehicleListGps3 = Utils.getCarListingDataGps3(requireContext()).items
        } else {
            vehicleList = ArrayList()
            vehicleList = Utils.getCarListingData(requireContext()).items
        }
    }

    var handler3: Handler = Handler(Looper.myLooper()!!)
    var refresh: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_single_vehicle, container, false)
        viewmodel = ViewModelProvider(this)[VehiclesListViewModel::class.java]
        binding.lifecycleOwner = this
        addObserver()
        Utils.hideProgressBar()
        return binding.root
    }

    @SuppressLint("FragmentLiveDataObserve", "NotifyDataSetChanged")
    private fun addObserver() {
        initializeRecyclerview()
        if (serverData.contains("s3")) {
            (requireActivity() as MainActivity).homeMapViewModel.carCarDetailsGPS3.observe(this) {
                Log.d(TAG, "addObserver: LIST Obs1")
                val vehiclesGps3 = it
                for (i in 0 until vehicleListGps3.size) {
                    for (j in 0 until vehiclesGps3!!.size) {
                        if (vehiclesGps3[j].imei
                                .equals(vehicleListGps3[i].imei, true)
                        ) {
                            vehiclesGps3[j].isExpanded = vehicleListGps3[i].isExpanded
                            vehiclesGps3[j].isSelected = vehicleListGps3[i].isSelected
                        }
                    }
                }
                vehicleListGps3.clear()
                vehicleListGps3.addAll(vehiclesGps3)
                adapterGps3.refresh()
                adapterGps3.filter.filter(binding.etSearch.text.toString())
            }
        } else {
            (requireActivity() as MainActivity).homeMapViewModel.unitList.observe(this) {
                it.let {
                    try {
                        (requireActivity() as MainActivity).homeMapViewModel.getCarImageFromData(
                            it, requireContext()
                        )
                        Log.d(TAG, "addObserver: LIST Obs")
                        val vehicleList1 = Utils.getCarListingData(requireContext()).items
                        for (i in 0 until vehicleList.size) {
                            for (j in 0 until vehicleList1.size) {
                                if (vehicleList1[j].id.toString()
                                        .equals(vehicleList!![i].id.toString(), true)
                                ) {
                                    vehicleList1[j].isExpanded = vehicleList!![i].isExpanded
                                    vehicleList1[j].isSelected = vehicleList!![i].isSelected
                                }
                            }
                        }
                        vehicleList?.clear()
                        vehicleList?.addAll(vehicleList1)
                        adapter.notifyDataSetChanged()
                        adapter.filter.filter(binding.etSearch.text.toString()); } catch (e: Exception) {
                        Log.e(TAG, "addObserver: CATCH ${e.message}")
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun checkAllItems(isChecked: Boolean) {
        carId.clear()
        if (serverData.contains("s3")) {
            val temp = adapterGps3.getFilteredListGps3()

            for (i in 0 until temp.size) {
                adapterGps3.getFilteredListGps3()[i].isSelected = isChecked
                vehicleListGps3.map { itemGps3 ->
                    if (itemGps3.imei == temp[i].imei) itemGps3.isSelected = isChecked
                }
            }
            adapterGps3.notifyDataSetChanged()
        } else {
            val temp = adapter.getFilteredList()

            for (i in 0 until adapter.getFilteredList().size) {
                adapter.getFilteredList()!![i].isSelected = isChecked
                vehicleList?.map { item ->
                    if (item.id == temp[i].id) item.isSelected = isChecked
                }
            }
            adapter.notifyDataSetChanged()
        }
        Log.d(TAG, "onCreateView: SELECTED $isChecked")

        manageCheckBox()
    }

    private lateinit var handlerGps3: SingleCarAdapterGps3.NotificationCommandHandler
    lateinit var handler: SingleCarAdapter.NotificationCommandHandler

    @SuppressLint("UseRequireInsteadOfGet")
    private fun initializeRecyclerview() {
        if (serverData.contains("s3")) {
            handlerGps3 = object : SingleCarAdapterGps3.NotificationCommandHandler {

                override fun onNotificationClick(toString: String) {
                    val bundle = bundleOf(
                        "comingFrom" to "onClickOfVehicle",
                        "carId" to toString
                    )
                    findNavController().navigate(
                        R.id.action_vehicalsListFragment_to_notificationFragment,
                        bundle
                    )
                }

                override fun onCommandClick(carId: String) {
                    val bundle = bundleOf(
                        "carId" to carId
                    )
                    Log.d("TAG", "onCommandClick: $carId")
                    findNavController().navigate(
                        R.id.action_vehicalsListFragment_to_commandFragment,
                        bundle
                    )
                }

                override fun onItemClick(model: ItemGps3, selected: Boolean) {
                    /*if (selected)
                        carId.add(model.imei)
                    else
                        carId.remove(model.imei)*/
                    manageCheckBox()
                }

                override fun onMapIconClick(position: Int) {
                    val lat = vehicleListGps3!![position].lat
                    val lng = vehicleListGps3!![position].lng

                    val urlAddress =
                        "http://maps.google.com/maps?q=" + lat + "," + lng + "(" + vehicleListGps3!![position].name + ")&iwloc=A&hl=es"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress))
                    context!!.startActivity(intent)
                }

                override fun onRemoveClick(position: Int) {
                }
            }

        } else {
            handler = object : SingleCarAdapter.NotificationCommandHandler {
                override fun onItemClick(model: Item, selected: Boolean) {
                    if (selected)
                        carId.add(model.id!!.toString())
                    else
                        carId.remove(model.id!!.toString())
                    manageCheckBox()
                }

                override fun onNotificationClick(toString: String) {
                    val bundle = bundleOf(
                        "comingFrom" to "onClickOfVehicle",
                        "carId" to toString
                    )
                    findNavController().navigate(
                        R.id.action_vehicalsListFragment_to_notificationFragment,
                        bundle
                    )
                }

                override fun onCommandClick(carId: String) {
                    val bundle = bundleOf(
                        "carId" to carId
                    )
                    findNavController().navigate(
                        R.id.action_vehicalsListFragment_to_commandFragment,
                        bundle
                    )
                }

                override fun onMapIconClick(position: Int) {
                    val lat = vehicleList!![position].pos?.y
                    val lng = vehicleList!![position].pos?.x
//                findNavController().navigate(VehicalsListFragmentDirections.actionVehicalsListFragmentToCommonMapFragment("ShowOnSingleVehicle"))
                    val urlAddress =
                        "http://maps.google.com/maps?q=" + lat + "," + lng + "(" + vehicleList!![position].nm + ")&iwloc=A&hl=es"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress))
                    context!!.startActivity(intent)
                }

                override fun onRemoveClick(position: Int) {

                }
            }
        }

        binding.btnShowOnMap.setOnClickListener {
            Utils.hideSoftKeyboard(requireActivity())
            val bundle = bundleOf(
                "comingFrom" to "ShowOnSingleVehicle",
                "carId" to carId
            )
            Log.d(TAG, "initializeRecyclerview: SHOW $carId")
            MyPreference.setValueString(PrefKey.SELECTED_CAR_LISTING, setListOfCar())
            findNavController().navigate(
                R.id.homemapFragment,
                bundle
            )
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                if (serverData.contains("s3")) {
                    val t = ArrayList<ItemGps3>()
                    vehicleListGps3.forEach() { itemGps3 ->
                        if (itemGps3.name.lowercase().contains(charSequence.toString().lowercase()))
                            t.add(itemGps3)
                    }
                    Log.d(TAG, "addTextChangedListener: SIZE ${t.size}")
                    adapterGps3.addAllFiltered(t as MutableList<ItemGps3>)
                    adapterGps3.notifyDataSetChanged()
                } else {
                    val t = ArrayList<Item>()
                    vehicleList.forEach() { item ->
                        if (item.nm!!.lowercase().contains(charSequence.toString().lowercase()))
                            t.add(item)
                    }
                    Log.d(TAG, "addTextChangedListener: SIZE ${t.size}")
                    adapter.addAllFiltered(t as MutableList<Item>)
                    adapter.notifyDataSetChanged()
                }
                manageCheckBox()
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun afterTextChanged(editable: Editable) {
            }
        })
        binding.rvSingleCar.layoutManager =
            LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)
        if (serverData.contains("s3")) {
            adapterGps3 = SingleCarAdapterGps3(handlerGps3, vehicleListGps3!!, false)

            binding.rvSingleCar.isNestedScrollingEnabled = false
            binding.rvSingleCar.adapter = adapterGps3
        } else {
            adapter = SingleCarAdapter(handler, vehicleList!!, false)
            binding.rvSingleCar.adapter = adapter
        }
    }

    private val TAG = "SingleVehicalFragment"
    private fun setListOfCar(): String {
        val data = StringBuilder()

        data.clear()
        if (carId.size > 1)
            for (i in 0 until carId.size) {
                data.append(carId[i])
                if (i != carId.size)
                    data.append(",")
                DebugLog.d("setListOfCar: ${data[i]}")
            }
        else data.append(carId[0])
        return data.toString()
    }

    fun manageCheckBox() {
        var isAllChecked = 1
        var isOneCheck = false
//        carId.clear()
        if (serverData.contains("s3")) {
            if (adapterGps3.getFilteredListGps3().isEmpty())
                isAllChecked = 0
            else {
                for (item in vehicleListGps3)
                    if (item.isSelected) {
                        if (!carId.contains(item.imei)) carId.add(item.imei)
                    } else carId.remove(item.imei)

                for (int in 0 until adapterGps3.getFilteredListGps3().size) {
                    if (!carId.contains(adapterGps3.getFilteredListGps3()[int].imei.trim())) {
                        isAllChecked = 0
                    } else {
                        isOneCheck = true
                    }
                }
            }
        } else {
            if (adapter.getFilteredList().isEmpty())
                isAllChecked = 0
            else {
                for (item in vehicleList)
                    if (item.isSelected) {
                        if (!carId.contains(item.id!!.toString())) carId.add(item.id!!.toString())
                    } else carId.remove(item.id!!.toString())
                for (int in 0 until adapter.getFilteredList().size) {
                    if (!carId.contains(adapter.getFilteredList()[int].id.toString().trim())) {
                        isAllChecked = 0
                    } else {
                        isOneCheck = true
                    }
                }
            }
            Log.d(TAG, "onCreateView: SELECTED $isAllChecked")
        }

//        (activity as MainActivity).chk_check.tag = isAllChecked
        (activity as MainActivity).chk_check.isChecked = isAllChecked != 0
        if (carId.size>0) {
            binding.btnShowOnMap.isEnabled = true
            binding.btnShowOnMap.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_white
                )
            )
        } else {
            binding.btnShowOnMap.isEnabled = false
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("TAG", "onResume:aa $isFromGroup")
        if (isFromGroup) {
            (activity as MainActivity).chk_check.isChecked = false
            checkAllItems(false)
            isFromGroup = false
        }
    }
}
