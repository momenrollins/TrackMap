package com.trackmap.gps.addgroup.ui


import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.trackmap.gps.DataValues.serverData
import com.trackmap.gps.MainActivity
import com.trackmap.gps.R
import com.trackmap.gps.addgroup.viewmodel.AddUnitsViewModel
import com.trackmap.gps.base.BaseFragment
import com.trackmap.gps.databinding.FragmentGroupUnitListBinding
import com.trackmap.gps.homemap.model.Item
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import com.trackmap.gps.utils.Utils
import com.trackmap.gps.vehicallist.adapter.SingleCarAdapter
import com.trackmap.gps.vehicallist.adapter.SingleCarAdapterGps3
import com.trackmap.gps.vehicallist.model.GroupImeisModelGps3
import com.trackmap.gps.vehicallist.model.GroupListDataModel
import com.trackmap.gps.vehicallist.model.ItemGps3
import com.trackmap.gps.vehicallist.viewmodel.VehiclesListViewModel
import kotlinx.android.synthetic.main.layout_custom_action_bar.*
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 */
class GroupUnitListFragment : BaseFragment() {

    private lateinit var adapter: SingleCarAdapter
    private lateinit var adapterGps3: SingleCarAdapterGps3
    private lateinit var binding: FragmentGroupUnitListBinding
    private lateinit var viewModel: AddUnitsViewModel
    private lateinit var viewmodel: VehiclesListViewModel
    private var carIdList = ArrayList<String>()
    private var selectedCarList = java.util.ArrayList<Item>()
    private var selectedCarListGps3 = java.util.ArrayList<ItemGps3>()
    private var removedCarListGps3 = java.util.ArrayList<String>()
    private var recyclerViewState: Parcelable? = null
    private var groupName = GroupListDataModel.Item()
    private var groupNameGps3: GroupImeisModelGps3 = GroupImeisModelGps3()
    private var handlerGps3: SingleCarAdapterGps3.NotificationCommandHandler? = null
    var vehicalList = ArrayList<Item>()
    var vehicalListGps3 = ArrayList<ItemGps3>()
    var comingFrom = ""
    var dashTitle = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_group_unit_list, container, false
        )

        viewModel = ViewModelProvider(this)[AddUnitsViewModel::class.java]
        viewmodel = ViewModelProvider(this)[VehiclesListViewModel::class.java]
        vehicalList = Utils.getCarListingData(requireContext()).items
        if (arguments != null) {

            Utils.hideProgressBar()


            if (serverData.contains("s3")) {
                groupNameGps3 = arguments?.getSerializable("groupNameGps3") as GroupImeisModelGps3
            } else {


                comingFrom = arguments?.getSerializable("comingFrom").toString()
                Log.d(TAG, "onCreateView: comingFrom $comingFrom")

                if (comingFrom == "Dash") {
                    dashTitle = arguments?.getSerializable("dashTitle").toString()
                }

                groupName = arguments?.getSerializable("groupName") as GroupListDataModel.Item
            }
        }
        getGroupUnitList()
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getGroupUnitList() {
        if (serverData.contains("s3")) {
            /* if (Utils.getCarListingDataGps3(requireContext()).items.size > 0 && selectedCarListGps3.size == 0)
                 carDetailListGps3 = Utils.getCarListingDataGps3(requireContext()).items


             selectedCarListGps3.clear()
             for (imei in groupNameGps3.data) {
                 for (carId in carDetailListGps3!!) {
                     if (imei == carId.imei) {
                         selectedCarListGps3.add(carId)
                         break
                     }
                 }
             }*/
            (requireActivity() as MainActivity).homeMapViewModel.getCarList()
            Log.d(TAG, "getGroupUnitList: selectedCarListGps3 SIZE0 ${groupNameGps3.data?.size}")
            if (groupNameGps3.data != null && groupNameGps3.data.size > 0) {
                binding.rvGroupCar.visibility = View.VISIBLE
                binding.txtGroupUnitsEmpty.visibility = View.INVISIBLE
                Utils.showProgressBar(requireContext())
                (requireActivity() as MainActivity).homeMapViewModel.carCarDetailsGPS3.observe(
                    viewLifecycleOwner
                ) {
                    Log.d(TAG, "getGroupUnitList: selectedCarListGps3 SIZEss ${it.size}")

                    val vehicleList1 = Utils.getCarListingDataGps3(requireContext()).items
                    vehicalListGps3.clear()
                    for (imei in groupNameGps3.data) {
                        for (carId in vehicleList1!!) {
                            if (imei == carId.imei) {
                                vehicalListGps3.add(carId)
                                break
                            }
                        }
                    }
                    for (i in 0 until selectedCarListGps3!!.size) {
                        for (j in 0 until vehicalListGps3.size) {
                            if (vehicalListGps3[j].imei
                                    .equals(selectedCarListGps3!![i].imei, true)
                            ) {
                                vehicalListGps3[j].isExpanded = selectedCarListGps3!![i].isExpanded
                                vehicalListGps3[j].isSelected = selectedCarListGps3!![i].isSelected
                            }
                        }
                    }
                    selectedCarListGps3.clear()
                    selectedCarListGps3.addAll(vehicalListGps3)
                    Log.d(
                        TAG,
                        "getGroupUnitList: selectedCarListGps3 SIZE ${selectedCarListGps3.size}"
                    )
                    adapterGps3.notifyDataSetChanged()
                    adapterGps3.filter.filter(binding.etSearch.text.toString());
                    Utils.hideProgressBar()
                }

            } else {
                binding.rvGroupCar.visibility = View.INVISIBLE
                binding.txtGroupUnitsEmpty.visibility = View.VISIBLE
            }
        } else {
            if (groupName.u != null) {
                (requireActivity() as MainActivity).homeMapViewModel.unitList.observe(
                    viewLifecycleOwner
                ) {
                    it.let {
                        (requireActivity() as MainActivity).homeMapViewModel.getCarImageFromData(
                            it, requireContext()
                        )

                        binding.rvGroupCar.visibility = View.VISIBLE
                        binding.txtGroupUnitsEmpty.visibility = View.INVISIBLE
                        val vehicalList1 = Utils.getCarListingData(requireContext()).items
                        vehicalList.clear()

                        if (comingFrom == "Dash") {
                            detectedDashState(vehicalList1)

                        } else {
                            for (ut in groupName.u) {
                                for (carId in vehicalList1) {
                                    if (carId.id.toString().equals(ut, true)) {
                                        vehicalList.add(carId)
                                    }
                                }
                            }
                        }

                        for (i in 0 until selectedCarList!!.size) {
                            for (j in 0 until vehicalList.size) {
                                if (vehicalList[j].id.toString()
                                        .equals(selectedCarList!![i].id.toString(), true)
                                ) {
                                    vehicalList[j].isExpanded = selectedCarList!![i].isExpanded
                                    vehicalList[j].isSelected = selectedCarList!![i].isSelected
                                }
                            }
                        }
                        selectedCarList.clear()
                        selectedCarList.addAll(vehicalList)
                        Log.d(TAG, "getGroupUnitList: selectedCarList SIZE ${selectedCarList.size}")
                        adapter.notifyDataSetChanged()
                        if (comingFrom == "Dash"|| comingFrom.equals("GeoZonesFragment"))
                            handleActionBarAString(groupName.nm + " (${vehicalList.size})")
                        else handleActionBarAStringUpdateUnit(groupName.nm + " (${vehicalList.size})")
                        adapter.filter.filter(binding.etSearch.text.toString());

                        if (vehicalList.size > 0) {
                            binding.rvGroupCar.visibility = View.VISIBLE
                            binding.txtGroupUnitsEmpty.visibility = View.INVISIBLE
                        } else {
                            binding.rvGroupCar.visibility = View.INVISIBLE
                            binding.txtGroupUnitsEmpty.visibility = View.VISIBLE
                        }
                    }
                }
            } else {
                binding.rvGroupCar.visibility = View.INVISIBLE
                binding.txtGroupUnitsEmpty.visibility = View.VISIBLE
            }

            // groupDetailList = Constants.finalGroupDetailList

            /* var unitList = listOf<String>()
             for (name in groupDetailList!!) {
                 if (name.nm.equals(groupName)) {
                     unitList = name.u
                 }
             }*/

        }
        initRecyclerView()
    }


    fun detectedDashState(vehicalList1: ArrayList<Item>) {
        try {

            vehicalList1.forEach { item ->
                val lastUpdateTime =((Calendar.getInstance().timeInMillis / 1000) - (item.trip_m?.toLong()
                    ?: 0)) / 60
                if (dashTitle == getString(R.string.offline) && lastUpdateTime  >= 11
                ) {
                    vehicalList.add(item)
                } else if (dashTitle == getString(R.string.stationary_with_ignition_on) && item.trip_state != null && lastUpdateTime <11 &&!((item.trip_state.toString()
                        .equals("0", true) ||
                            (item.trip_state?.trim()!!
                                .isEmpty())) || item.sens?.size() == 0
                            )
                ) {
                    vehicalList.add(item)
                } else if (dashTitle == getString(R.string.no_messages) && (lastUpdateTime / 60 >= 11) && item.lmsg == null
                ) {
                    vehicalList.add(item)
                } else
                    if (groupName.nm.equals(getString(R.string.online))) {

                        if (((Calendar.getInstance().timeInMillis / 1000) - (item.trip_m?.toLong()
                                ?: 0)) / 60 < 11
                        ) {
                            vehicalList.add(item)
                        }
                    } else if (groupName.nm.equals(getString(R.string.stationary))) {
                        if (((Calendar.getInstance().timeInMillis / 1000) - (item.trip_m?.toLong()
                                ?: 0)) / 60 < 11
                        ) {
                            if (item.trip_curr_speed == null || item.trip_curr_speed == "0") {
                                if ((item.trip_state.toString()
                                        .equals("0", true) ||
                                            item.trip_state?.trim()!!
                                                .isEmpty()) || item.sens?.size() == 0
                                ) {
                                    vehicalList.add(item)
                                }
                            }
                        }


                    } else if (groupName.nm.equals(getString(R.string.moving))) {


                        if (item.trip_curr_speed != null && item.trip_curr_speed?.toInt()!! > 0) {
                            vehicalList.add(item)
                        }


                    } else if (groupName.nm.equals(getString(R.string.moving))) {


                        if (item.trip_curr_speed != null && item.trip_curr_speed?.toInt()!! > 0) {
                            vehicalList.add(item)
                        }


                    } else if (groupName.nm.equals(getString(R.string.no_actual_state))) {
                        if (((Calendar.getInstance().timeInMillis / 1000) - (item.trip_m?.toLong()
                                ?: 0)) / 60 > 11
                        ) {
                            if (item.lmsg != null) vehicalList.add(item)
                        }


                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "detectedDashState: CATCH ${e.message}")
        }
    }

    private val TAG = "GroupUnitListFragment"

    @SuppressLint("NotifyDataSetChanged")
    private fun initRecyclerView() {
        binding.rvGroupCar.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        handlerGps3 = object : SingleCarAdapterGps3.NotificationCommandHandler {
            override fun onNotificationClick(toString: String) {
                val bundle = bundleOf(
                    "comingFrom" to "onClickOfVehicle",
                    "carId" to toString
                )
                findNavController().navigate(
                    R.id.action_groupUnitListFragment_to_notificationFragment, bundle
                )
            }

            override fun onCommandClick(carId: String) {
                val bundle = bundleOf("carId" to carId)
                findNavController().navigate(
                    R.id.action_groupListFragment_to_commandFragment, bundle
                )
            }

            override fun onItemClick(model: ItemGps3, selected: Boolean) {
                if (selected) carIdList.add(model.imei)
                else carIdList.remove(model.imei)

                var isOneCheck = false
                for (int in 0 until selectedCarListGps3.size) {
                    if (carIdList.contains(selectedCarListGps3[int].imei.trim())) {
                        isOneCheck = true
                        break
                    }
                }
                if (isOneCheck) {
                    binding.btnShowOnMap.isEnabled = true
                    binding.btnShowOnMap.setTextColor(
                        ContextCompat.getColor(
                            requireContext(), R.color.color_white
                        )
                    )
                } else {
                    binding.btnShowOnMap.isEnabled = false
                }
            }

            override fun onMapIconClick(position: Int) {
                val lat = selectedCarListGps3[position].lat
                val lng = selectedCarListGps3[position].lng

                val urlAddress =
                    "http://maps.google.com/maps?q=" + lat + "," + lng + "(" + selectedCarListGps3[position].name + ")&iwloc=A&hl=es"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress))
                context!!.startActivity(intent)
            }

            override fun onRemoveClick(position: Int) {
                recyclerViewState = binding.rvGroupCar.layoutManager!!.onSaveInstanceState()
                removedCarListGps3.clear()
                removedCarListGps3.add(selectedCarListGps3[position].imei)
                groupNameGps3.data.removeAt(position).also {
                    selectedCarListGps3.removeAt(position)
                    Log.d(TAG, "onRemoveClick: $position")
                    adapterGps3.notifyDataSetChanged()
                }
                if (selectedCarListGps3.size == 0) {
                    binding.rvGroupCar.visibility = View.INVISIBLE
                    binding.txtGroupUnitsEmpty.visibility = View.VISIBLE
                } else {
                    binding.rvGroupCar.visibility = View.VISIBLE
                    binding.txtGroupUnitsEmpty.visibility = View.INVISIBLE
                }

                viewModel.callApiForUpdateGroupImeisGps3(
                    groupNameGps3.group_id.toInt(),
                    removedCarListGps3,
                    "remove"
                )
                viewModel.updatedUnitList.observeOnce { b: Boolean? ->
                    if (b == true) {
                        Toast.makeText(
                            context, getString(R.string.successfully_removed_unit_in_group),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            getString(R.string.something_goes_wrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                /*viewModel.updatedUnitList.observe(viewLifecycleOwner) {
                    it.let {
                        if (it) {
                            Toast.makeText(
                                context, getString(R.string.successfully_removed_unit_in_group),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context, getString(R.string.something_goes_wrong), Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }*/
            }
        }

        val handler = object : SingleCarAdapter.NotificationCommandHandler {
            override fun onRemoveClick(position: Int) {
                recyclerViewState = binding.rvGroupCar.layoutManager!!.onSaveInstanceState()
                groupName.u.removeAt(position)

                selectedCarList.removeAt(position)
                adapter.notifyDataSetChanged()
                if (selectedCarList.size == 0) {
                    binding.rvGroupCar.visibility = View.INVISIBLE
                    binding.txtGroupUnitsEmpty.visibility = View.VISIBLE
                } else {
                    binding.rvGroupCar.visibility = View.VISIBLE
                    binding.txtGroupUnitsEmpty.visibility = View.INVISIBLE
                }

                viewModel.callApiForAddUnitsToGroup(
                    JSONArray(groupName.u),
                    groupName.id.toLong(),
                    true
                )
//                viewModel.callApiForUpdateGroupItems(groupName.id,selectedCarIds)
                viewModel.updatedUnitList.observeOnce { b: Boolean? ->
                    if (b == true) {
                        Toast.makeText(
                            context, getString(R.string.successfully_removed_unit_in_group),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            getString(R.string.something_goes_wrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                /*


                  viewModel.callApiForUpdateGroupImeisGps3(
                      groupNameGps3.group_id.toInt(),
                      removedCarListGps3,
                      "remove"
                  )
                 */
            }

            override fun onItemClick(model: Item, selected: Boolean) {
                if (selected)
                    carIdList.add(model.id!!.toString())
                else
                    carIdList.remove(model.id!!.toString())
                var isOneCheck = false
                for (int in 0 until selectedCarList.size) {
                    if (carIdList.contains(selectedCarList[int].id.toString().trim())) {
                        isOneCheck = true
                        break
                    }
                }
                if (isOneCheck) {
                    binding.btnShowOnMap.isEnabled = true
                    binding.btnShowOnMap.setTextColor(
                        ContextCompat.getColor(
                            requireContext(), R.color.color_white
                        )
                    )
                } else {
                    binding.btnShowOnMap.isEnabled = false
                }
            }

            override fun onNotificationClick(toString: String) {
                val bundle = bundleOf(
                    "comingFrom" to "onClickOfVehicle",
                    "carId" to toString
                )
                findNavController().navigate(
                    R.id.action_groupUnitListFragment_to_notificationFragment, bundle
                )
            }

            override fun onCommandClick(carId: String) {
                val bundle = bundleOf(
                    "carId" to carId
                )
                findNavController().navigate(
                    R.id.action_groupListFragment_to_commandFragment, bundle
                )
            }

            override fun onMapIconClick(position: Int) {
                val lat = selectedCarList[position].pos?.y
                val lng = selectedCarList[position].pos?.x
                val urlAddress =
                    "http://maps.google.com/maps?q=" + lat + "," + lng + "(" + selectedCarList[position]?.nm + ")&iwloc=A&hl=es"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress))
                context!!.startActivity(intent)

            }
        }
        binding.btnShowOnMap.setOnClickListener {
            Utils.hideSoftKeyboard(requireActivity())
            val bundle = bundleOf(
                "comingFrom" to "ShowOnGroupSingleVehicle",
                "carId" to carIdList
            )
            MyPreference.setValueString(PrefKey.SELECTED_CAR_LISTING, setListOfCar())
            findNavController().navigate(
                R.id.homemapFragment, bundle
            )
        }

        (activity as MainActivity).add_vehicle.setOnClickListener {
            var bundle: Bundle;
            if (serverData.contains("s3")) {
                bundle = bundleOf(
                    "comingFrom" to "GroupUnitList",
                    "groupName" to binding.etSearch.text.toString(),
                    "UnitList" to selectedCarListGps3,
                    "groupId" to groupNameGps3.group_id
                )

            } else {

                bundle = bundleOf(
                    "comingFrom" to "GroupUnitList",
                    "groupName" to groupName.nm,
                    "UnitList" to selectedCarList,
                    "groupId" to groupName.id.toString()
                )
            }
            Log.d(TAG, "initRecyclerView:groupID11 ${groupName.id}")

            findNavController()?.navigate(
                R.id.addunitsFragment, bundle
            )
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                if (serverData.contains("s3")) {
                    adapterGps3.filter.filter(editable.toString())

                } else adapter.filter.filter(editable.toString())
            }
        })
        if (serverData.contains("s3")) {
            Log.d(TAG, "initRecyclerView: selectedCarListGps3 SIZE ${selectedCarListGps3.size}")
            adapterGps3 =
                SingleCarAdapterGps3(handlerGps3!!, selectedCarListGps3, comingFrom != "Dash")
            binding.rvGroupCar.adapter = adapterGps3
            adapterGps3.notifyDataSetChanged()
        } else {
            Log.d(TAG, "initRecyclerView: selectedCarList SIZE ${selectedCarList.size}")
            adapter = SingleCarAdapter(handler, selectedCarList, comingFrom != "Dash")
            binding.rvGroupCar.adapter = adapter
        }
    }

    private fun setListOfCar(): String {
        val data = StringBuilder()
        for (i in 0 until carIdList.size) {
            data.append(carIdList[i])
            data.append(",")
        }
        return data.toString()
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
        if (carIdList != null && carIdList.size > 0)
            binding.btnShowOnMap.isEnabled = true
        if (comingFrom.equals("Dash" )|| comingFrom.equals("GeoZonesFragment")) {
            handleActionBarAString("${groupName.nm} (${groupName.u.size})")
        } else
            if (serverData.contains("s3")) handleActionBarAStringUpdateUnit(groupNameGps3.name)
            else handleActionBarAStringUpdateUnit(groupName.nm)
    }

}