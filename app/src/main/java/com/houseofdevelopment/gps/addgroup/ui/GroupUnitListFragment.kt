package com.houseofdevelopment.gps.addgroup.ui


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
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
import com.houseofdevelopment.gps.DataValues.serverData
import com.houseofdevelopment.gps.MainActivity
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.addgroup.viewmodel.AddUnitsViewModel
import com.houseofdevelopment.gps.base.BaseFragment
import com.houseofdevelopment.gps.databinding.FragmentGroupUnitListBinding
import com.houseofdevelopment.gps.databinding.LayoutDeleteCarBinding
import com.houseofdevelopment.gps.homemap.model.Item
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey
import com.houseofdevelopment.gps.utils.Utils
import com.houseofdevelopment.gps.vehicallist.adapter.SingleCarAdapter
import com.houseofdevelopment.gps.vehicallist.adapter.SingleCarAdapterGps3
import com.houseofdevelopment.gps.vehicallist.model.GroupImeisModelGps3
import com.houseofdevelopment.gps.vehicallist.model.GroupListDataModel
import com.houseofdevelopment.gps.vehicallist.model.ItemGps3
import com.houseofdevelopment.gps.vehicallist.viewmodel.VehiclesListViewModel
import kotlinx.android.synthetic.main.layout_custom_action_bar.*
import org.json.JSONArray
import java.text.SimpleDateFormat
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
    private var carDetailList: MutableList<Item>? = null
    private var carDetailListGps3: ArrayList<ItemGps3>? = null
    private var carIdList = ArrayList<String>()
    private var selectedCarList = java.util.ArrayList<Item>()
    private var selectedCarListGps3 = java.util.ArrayList<ItemGps3>()
    private var filteredCarListGps3 = java.util.ArrayList<ItemGps3>()
    private var removedCarListGps3 = java.util.ArrayList<String>()
    private var recyclerViewState: Parcelable? = null
    private var groupName = GroupListDataModel.Item()
    private var groupNameGps3: GroupImeisModelGps3 = GroupImeisModelGps3()
    private var handlerGps3: SingleCarAdapterGps3.NotificationCommandHandler? = null
    var vehicalList = ArrayList<Item>()
    var vehicalListGps3 = ArrayList<ItemGps3>()
    var comingFrom = ""
    var inquiryTitle = ""


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
//            Utils.hideProgressBar()
            comingFrom = arguments?.getSerializable("comingFrom").toString()
            if (serverData.contains("s3")) {
                groupNameGps3 = arguments?.getSerializable("groupNameGps3") as GroupImeisModelGps3
            } else {
                Log.d(TAG, "onCreateView: comingFrom $comingFrom")
                groupName = arguments?.getSerializable("groupName") as GroupListDataModel.Item
            }
        }
        getGroupUnitList()
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getGroupUnitList() {
        if (serverData.contains("s3")) {
            (requireActivity() as MainActivity).homeMapViewModel.getCarList()
            Log.d(TAG, "getGroupUnitList: selectedCarListGps3 SIZE0 ${groupNameGps3.data?.size}")
            if (groupNameGps3.data != null && groupNameGps3.data.size > 0) {
                binding.rvGroupCar.visibility = View.VISIBLE
                binding.txtGroupUnitsEmpty.visibility = View.INVISIBLE
//                Utils.showProgressBar(requireContext())
                (requireActivity() as MainActivity).homeMapViewModel.carCarDetailsGPS3.observe(
                    viewLifecycleOwner
                ) {

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
                    for (i in 0 until selectedCarListGps3.size) {
                        for (j in 0 until vehicalListGps3.size) {
                            if (vehicalListGps3[j].imei
                                    .equals(selectedCarListGps3[i].imei, true)
                            ) {
                                vehicalListGps3[j].isExpanded = selectedCarListGps3[i].isExpanded
                                vehicalListGps3[j].isSelected = selectedCarListGps3[i].isSelected
                            }
                        }
                    }
                    selectedCarListGps3.clear()
                    selectedCarListGps3.addAll(vehicalListGps3)
                    Log.d(
                        TAG,
                        "getGroupUnitList: selectedCarListGps3 SIZE ${selectedCarListGps3.size}"
                    ); adapterGps3.notifyDataSetChanged()
                    adapterGps3.filter.filter(binding.etSearch.text.toString())
                    if (comingFrom.equals("Dash") || comingFrom.equals("GeoZonesFragment")) {
                        Log.d(
                            TAG, "get: selectedCarListGps3 SIZE ${selectedCarListGps3.size}"
                        )
                        handleActionBarAString("${groupNameGps3.name} (${selectedCarListGps3.size})")
                    }
                }

            } else {
                binding.rvGroupCar.visibility = View.INVISIBLE
                binding.txtGroupUnitsEmpty.visibility = View.VISIBLE
            }
            Utils.hideProgressBar()

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
                        for (ut in groupName.u) {
                            Log.d(TAG, "getGroupUnitList:wasl $ut")
                            for (carId in vehicalList1) {
                                if (carId.id.toString().equals(ut, true)) {
                                    vehicalList.add(carId)
                                }
                            }
                        }


                        for (i in 0 until selectedCarList.size) {
                            for (j in 0 until vehicalList.size) {
                                if (vehicalList[j].id.toString()
                                        .equals(selectedCarList[i].id.toString(), true)
                                ) {
                                    vehicalList[j].isExpanded = selectedCarList[i].isExpanded
                                    vehicalList[j].isSelected = selectedCarList[i].isSelected
                                }
                            }
                        }
                        selectedCarList.clear()
                        selectedCarList.addAll(vehicalList)
                        Log.d(TAG, "getGroupUnitList: selectedCarList SIZE ${selectedCarList.size}")
                        adapter.notifyDataSetChanged()
                        if (comingFrom == "Dash" || comingFrom.equals("GeoZonesFragment")
                        )
                            handleActionBarAString(groupName.nm + " (${vehicalList.size})")
                        else handleActionBarAStringUpdateUnit(groupName.nm + " (${vehicalList.size})")
                        adapter.filter.filter(binding.etSearch.text.toString())

                        if (vehicalList.size > 0) {
                            binding.rvGroupCar.visibility = View.VISIBLE
                            binding.txtGroupUnitsEmpty.visibility = View.INVISIBLE
                        } else {
                            binding.rvGroupCar.visibility = View.INVISIBLE
                            binding.txtGroupUnitsEmpty.visibility = View.VISIBLE
                        }
                        Utils.hideProgressBar()
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

                Log.d(TAG, "onRemoveClick:pos ${position} ")
                val filterd = adapterGps3.getFilteredListGps3()
                recyclerViewState = binding.rvGroupCar.layoutManager!!.onSaveInstanceState()
                removedCarListGps3.clear()
                removedCarListGps3.add(filterd[position].imei)


                Log.d(TAG, "onRemoveClick: SIZE ${filterd.size} ${selectedCarList.size}")
                groupNameGps3.data.remove(filterd[position].imei)
                carIdList.remove(filterd[position].imei.toString())
                Log.d(TAG, "onRemoveClick:carIdList ${carIdList.size} ")
                if (carIdList.size == 0)
                    binding.btnShowOnMap.isEnabled = false
                if (selectedCarListGps3.size != filterd.size)
                    selectedCarListGps3.remove(filterd[position])
                filterd.remove(filterd[position])
                adapterGps3.notifyDataSetChanged()
                handleActionBarAStringUpdateUnit("${groupNameGps3.name} (${selectedCarListGps3.size})")
                /* groupNameGps3.data.removeAt(position).also {
                     selectedCarListGps3.removeAt(position)
                     Log.d(TAG, "onRemoveClick: $position")
                     adapterGps3.notifyDataSetChanged()
                 }*/
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
                    "remove",
                    java.util.ArrayList<String>(),
                    ""
                )
                viewModel.updatedUnitList.observeOnce { b: Boolean? ->
                    if (b == true) {
                        try {
                            Toast.makeText(
                                context, getString(R.string.successfully_removed_unit_in_group),
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Log.e(TAG, "onRemoveClick: CATCH ${e.message}")
                        }

                    } else {
                        try {
                            Toast.makeText(
                                context, getString(R.string.something_goes_wrong),
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Log.e(TAG, "onRemoveClick: CATCH ${e.message}")
                        }
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

                showYesNoAlerter(position)

//                viewModel.callApiForUpdateGroupItems(groupName.id,selectedCarIds)

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
                    "http://maps.google.com/maps?q=" + lat + "," + lng + "(" + selectedCarList[position].nm + ")&iwloc=A&hl=es"
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
            val bundle: Bundle
            if (serverData.contains("s3")) {
                bundle = bundleOf(
                    "comingFrom" to "GroupUnitList",
                    "groupName" to groupNameGps3.name,
                    "UnitList" to selectedCarListGps3,
                    "groupId" to groupNameGps3.group_id
                )

            } else {
                val from = if (comingFrom == "DriverDash") "DriverDash" else "GroupUnitList"
                bundle = bundleOf(
                    "comingFrom" to from,
                    "groupName" to groupName.nm,
                    "UnitList" to selectedCarList,
                    "groupId" to groupName.id.toString()
                )
                Log.d(TAG, "initRecyclerView: GroupUnitList ${selectedCarList.size}")
            }
            Log.d(TAG, "initRecyclerView:groupID11 ${groupName.id}")

            findNavController().navigate(
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
                    for (name in selectedCarListGps3) {
                        if (name.name.lowercase().trim()
                                .contains(editable.toString().lowercase().trim())
                        ) {
                            filteredCarListGps3.add(name)
                        }
                    }
                    adapterGps3.addAllFiltered(filteredCarListGps3)


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
            adapter = SingleCarAdapter(
                handler,
                selectedCarList,
                comingFrom != "Dash"
            )
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

    fun showYesNoAlerter(
        position: Int
    ): Dialog {
        val mDialog = Dialog(requireContext(), R.style.DialogTheme)
        val binding2: LayoutDeleteCarBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.layout_delete_car,
            null,
            false
        )
        mDialog.setContentView(binding2.root)

        binding2.lblDeleteCar.text = getString(R.string.delete_unit)
        binding2.lblDescriptiveMsg.text = getString(R.string.are_you_sure_want_to_delete_this_unit)
        binding2.btnYes.setOnClickListener {
            Utils.showProgressBar(requireContext())
            val filtered = adapter.getFilteredList()
            Log.d(TAG, "onRemoveClick: SIZE ${filtered.size} ${selectedCarList.size}")
            groupName.u.remove(filtered[position].id.toString())
            carIdList.remove(filtered[position].id.toString())
            if (carIdList.size == 0)
                binding.btnShowOnMap.isEnabled = false
            if (selectedCarList.size != filtered.size)
                selectedCarList.remove(filtered[position])

            viewModel.callApiForAddUnitsToGroup(
                JSONArray(groupName.u),
                groupName.id.toLong(),
                true
            )

            viewModel.updatedUnitList.observeOnce { b: Boolean? ->
                if (b == true) {
                    try {

                        Toast.makeText(
                            context, getString(R.string.successfully_removed_unit_in_group),
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Log.e(TAG, "onRemoveClick: CATCH ${e.message}")
                    }

                } else {
                    try {
                        Toast.makeText(
                            context,
                            getString(R.string.something_goes_wrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Log.e(TAG, "onRemoveClick: CATCH ${e.message}")
                    }
                }
            }
            filtered.remove(filtered[position])
            adapter.notifyDataSetChanged()
            handleActionBarAString(groupName.nm + " (${selectedCarList.size})")
            if (selectedCarList.size == 0) {
                binding.rvGroupCar.visibility = View.INVISIBLE
                binding.txtGroupUnitsEmpty.visibility = View.VISIBLE
            } else {
                binding.rvGroupCar.visibility = View.VISIBLE
                binding.txtGroupUnitsEmpty.visibility = View.INVISIBLE
            }
            handleActionBarAStringUpdateUnit("${groupName.nm} (${groupName.u.size})")
            mDialog.dismiss()
        }

        binding2.btnNo.setOnClickListener {
            mDialog.dismiss()
        }

        mDialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        mDialog.setCancelable(false)

        binding2.executePendingBindings()
        try {
            if (!mDialog.isShowing)
                mDialog.show()
        } catch (e: Exception) {
        }
        return mDialog

    }


    override fun onResume() {
        super.onResume()
        if (carIdList != null && carIdList.size > 0)
            binding.btnShowOnMap.isEnabled = true
        if (comingFrom.equals("Dash") || comingFrom.equals("GeoZonesFragment")) {
            Log.d(TAG, "onResume:dash ${groupNameGps3.name}  ")
            if (serverData.contains("s3")) handleActionBarAString("${groupNameGps3.name} (${selectedCarListGps3.size})")
            else handleActionBarAString("${groupName.nm} (${selectedCarList.size})")
        } else {
            Log.d(TAG, "onResume:dash2 ${groupNameGps3.name}  ")
            if (serverData.contains("s3")) handleActionBarAStringUpdateUnit("${groupNameGps3.name} (${groupNameGps3.data.size})")
            else handleActionBarAStringUpdateUnit("${groupName.nm} (${groupName.u.size})")
        }
    }
}