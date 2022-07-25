package com.houseofdevelopment.gps.vehicallist.ui

import android.annotation.SuppressLint
import android.os.Bundle
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.base.BaseFragment
import com.houseofdevelopment.gps.databinding.FragmentGroupVehicleBinding
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey
import com.houseofdevelopment.gps.utils.CommonAlertDialog
import com.houseofdevelopment.gps.utils.Utils
import com.houseofdevelopment.gps.vehicallist.adapter.GroupCarAdapter
import com.houseofdevelopment.gps.vehicallist.adapter.GroupCarAdapterGps3
import com.houseofdevelopment.gps.vehicallist.model.ConvertModel
import com.houseofdevelopment.gps.vehicallist.model.GroupImeisModelGps3
import com.houseofdevelopment.gps.vehicallist.model.GroupListDataModel
import com.houseofdevelopment.gps.vehicallist.model.ItemGroupDataModelGps3
import com.houseofdevelopment.gps.vehicallist.viewmodel.VehiclesListViewModel

class GroupVehicalFragment : BaseFragment() {

    lateinit var binding: FragmentGroupVehicleBinding
    lateinit var viewmodel: VehiclesListViewModel
    lateinit var adapter: GroupCarAdapter
    lateinit var adapterGps3: GroupCarAdapterGps3
    private var groupListDetail: MutableList<GroupListDataModel.Item>? = null
    private var groupListDetailGps3: ArrayList<ItemGroupDataModelGps3>? = null
    private var groupId = ArrayList<String>()
    var carId = ArrayList<String>()
    var dataListGps3: ArrayList<ItemGroupDataModelGps3>? = null
    private var serverData: String = ""
    private var positionItem: Int? = null
    private var groupNameItem: String? = null
    var filterList = mutableListOf<GroupListDataModel.Item>()
    var filterListGps3 = ArrayList<ItemGroupDataModelGps3>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_group_vehicle, container, false)
        viewmodel = ViewModelProvider(this)[VehiclesListViewModel::class.java]
        binding.lifecycleOwner = this
        serverData = MyPreference.getValueString(PrefKey.SELECTED_SERVER_DATA, "")!!
        MyPreference.setValueString("SHowgroup", "")
        addObserver()

        return binding.root
    }

    private val TAG = "GroupVehicalFragment"

    @SuppressLint("NotifyDataSetChanged")
    private fun addObserver() {
        if (serverData.contains("s3")) {

            viewmodel.itemGroupDataGps3.observe(viewLifecycleOwner) {
                val imeisModel: GroupImeisModelGps3
                if (MyPreference.getValueString("SHowgroup", "").equals("ShowItems")) {

                    if (viewmodel.itemGroupDataGps3.value!!.data.size >= 0) {
                        if (it.data.size >= 0) {
                            imeisModel = it
                            imeisModel.setName(groupNameItem)
                            imeisModel.status = false
                            imeisModel.setGroup_id(groupListDetailGps3?.get(positionItem!!)?.group_id!!)
                            val bundle = bundleOf(
                                "groupNameGps3" to imeisModel
                            )
                            MyPreference.setValueBoolean("unit", false)
                            findNavController().navigate(
                                R.id.groupUnitListFragment,
                                bundle
                            )
                            // viewmodel.loading(false)

                        }
                    }
                } else if (MyPreference.getValueString("SHowgroup", "").equals("ShowMaping")) {
                    carId.clear()
                    Utils.hideProgressBar()
                    if (it.data != null && it.data.size > 0) {
                        for (gId in groupId) {
                            for (item in groupListDetailGps3!!) {
                                if (item.group_id == gId) {
                                    // store list of cars
                                    for (imei in it.data) {
                                        carId.add(imei)
                                    }
                                }
                            }
                        }
                        val bundle = bundleOf(
                            "comingFrom" to "ShowOnGroupVehicle",
                            "carId" to carId
                        )
                        MyPreference.setValueString(PrefKey.SELECTED_CAR_LISTING, setListOfCar())
                        findNavController().navigate(
                            R.id.homemapFragment,
                            bundle
                        )
                    } else {
                        Toast.makeText(
                            context,
                            getString(R.string.this_group_is_empty),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
            viewmodel.groupDataGps3.observe(viewLifecycleOwner) {
                it.let {
                    if (it.status) {
                        if (groupListDetailGps3.isNullOrEmpty()) {
                            binding.rvGroupCar.visibility = View.VISIBLE
                            binding.txtNoDatagroub.visibility = View.GONE
                            groupListDetailGps3 = it.data as ArrayList<ItemGroupDataModelGps3>?
                            initializeRecyclerview()
                        }
                    } else {
                        binding.rvGroupCar.visibility = View.GONE
                        binding.txtNoDatagroub.visibility = View.VISIBLE
                    }
                    Utils.hideProgressBar()
                }
            }
        }
        else {
            viewmodel.groupData.observe(viewLifecycleOwner) {
                it.let {
                    Log.d(TAG, "group addObserver: ${it.size}")
                    if (it.isNotEmpty()) {
                        binding.rvGroupCar.visibility = View.VISIBLE
                        binding.txtNoDatagroub.visibility = View.GONE
                        if (groupListDetail.isNullOrEmpty()) {
                            groupListDetail = it as MutableList<GroupListDataModel.Item>?
                            initializeRecyclerview()
                        }
                    } else {
                        binding.rvGroupCar.visibility = View.GONE
                        binding.txtNoDatagroub.visibility = View.VISIBLE
                    }
                    Utils.hideProgressBar()

                }
            }
            viewmodel.deleteGroup.observe(viewLifecycleOwner) {
                if (!it) {
                    try {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.unable_to_delete_group),
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Log.e(TAG, "addObserver: CATCH ${e.message}")
                    }
                }
            }
        }
    }

    private fun initializeRecyclerview() {
        val handler = object : GroupCarAdapter.GroupOnclick {
            override fun itemOnClick(
                position: Int,
                selected: Boolean,
                idsList: MutableList<String>
            ) {
                positionItem = position

                /* if (selected) {
                     groupId.add(itemList?.get(position)?.id!!.toString())
                 } else
                     groupId.remove(itemList?.get(position)?.id!!.toString())*/
                groupId.clear()
                groupId.add(groupListDetail?.get(position)?.id!!.toString())

                if (groupId.size > 0) {
                    binding.btnShowOnMap.isEnabled = true
                    binding.btnShowOnMap.setTextColor(
                        ContextCompat.getColor(
                            context!!,
                            R.color.color_white
                        )
                    )
                    carId.clear()
                    if (!serverData.contains("s3")) {
                        Log.d(TAG, "itemOnClick: POS $position")
                        for (ut in idsList) {
                            carId.add(ut)
                        }

                    }
                } else {
                    binding.btnShowOnMap.isEnabled = false
                }
            }

            override fun onClick(nm: GroupListDataModel.Item) {
                Utils.showProgressBar(requireContext())
                val bundle = bundleOf(
                    "groupName" to nm
                )
                findNavController().navigate(
                    R.id.groupUnitListFragment,
                    bundle
                )
            }
        }

        val handlerGps3 = object : GroupCarAdapterGps3.GroupOnclickGps3 {
            override fun onClickGps3(position: Int, groupName: String) {
                //  viewmodel.loading(true)
                Utils.showProgressBar(requireContext())
                positionItem = position
                groupNameItem = groupName
                viewmodel.callApiForGetGroupDataGps3(
                    groupListDetailGps3?.get(position)?.group_id!!.toInt(),
                    false
                )
            }

            override fun itemOnClick(
                value: ArrayList<ItemGroupDataModelGps3>, position: Int, selected: Boolean
            ) {
                /* if (selected) {
                     groupId.add(itemList?.get(position)?.id!!.toString())
                 } else
                     groupId.remove(itemList?.get(position)?.id!!.toString())*/
                groupId.clear()
                groupId.add(groupListDetailGps3?.get(position)?.group_id!!.toString())
                positionItem = position
                dataListGps3 = value
                if (groupId.size > 0) {
                    binding.btnShowOnMap.isEnabled = true
                    binding.btnShowOnMap.setTextColor(
                        ContextCompat.getColor(
                            context!!, R.color.color_white
                        )
                    )
                } else {
                    binding.btnShowOnMap.isEnabled = false
                }
            }
        }
        val handlerAlertDialog = object : CommonAlertDialog.DeleteCarHandler {
            @SuppressLint("NotifyDataSetChanged")
            override fun onYesClick(itemId: Int, selectedPosition: Int) {
                if (serverData.contains("s3")) {
                    viewmodel.callApiForDeleteGroupGps3(itemId)
                    if (filterListGps3.isNotEmpty()) {
                        val item = filterListGps3[selectedPosition]
                        filterListGps3.remove(item)
                        groupListDetailGps3?.remove(item)
                        adapterGps3.notifyDataSetChanged()
                    } else groupListDetailGps3?.removeAt(selectedPosition)
                    groupListDetailGps3?.map { itemGroupDataModelGps3 ->
                        itemGroupDataModelGps3.isSelected = false
                    }
                    binding.btnShowOnMap.isEnabled = false
                    adapterGps3.notifyDataSetChanged()
                    if (groupListDetailGps3?.size == 0) {
                        binding.rvGroupCar.visibility = View.GONE
                        binding.txtNoDatagroub.visibility = View.VISIBLE
                    }
                } else {
                    viewmodel.callApiForDeleteGroup(itemId)
                    adapter
                    if (filterList.isNotEmpty()) {
                        val item: GroupListDataModel.Item = filterList[selectedPosition]
                        filterList.remove(item)
                        groupListDetail?.remove(item)
                        adapter.updateList(filterList)
                    } else {
                        groupListDetail?.removeAt(selectedPosition)
                    }
                    groupListDetail?.map { item -> item.isSelected = false }
                    if (groupListDetail?.size == 0) {
                        binding.rvGroupCar.visibility = View.GONE
                        binding.txtNoDatagroub.visibility = View.VISIBLE
                    }
                    binding.btnShowOnMap.isEnabled = false
                    adapter.notifyDataSetChanged()
                }
            }
        }

        binding.btnShowOnMap.setOnClickListener {
            if (serverData.contains("s3")) {
                Utils.showProgressBar(requireContext())

                binding.btnShowOnMap.isEnabled = false
                carId.clear()
                viewmodel.callApiForGetGroupDataGps3(
                    dataListGps3?.get(positionItem!!)?.group_id!!.toInt(), true
                )
            } else {
                val bundle = bundleOf(
                    "comingFrom" to "ShowOnGroupVehicle", "carId" to carId
                )
                MyPreference.setValueString(PrefKey.SELECTED_CAR_LISTING, setListOfCar())
                Log.d(
                    TAG,
                    "initializeRecyclerview:carid ${carId.size}  ${
                        MyPreference.getValueString(
                            PrefKey.SELECTED_CAR_LISTING,
                            ""
                        )
                    } "
                )
                findNavController().navigate(
                    R.id.homemapFragment, bundle
                )
            }
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {

                filterList = mutableListOf()
                filterListGps3 = ArrayList()
                if (serverData.contains("s3")) {
                    for (name in groupListDetailGps3!!) {
                        if (name.group_name.lowercase().trim()
                                .contains(editable.toString().lowercase().trim())
                        ) {
                            filterListGps3.add(name)
                        }
                    }
                    adapterGps3.updateList(filterListGps3)
                } else {
                    for (name in groupListDetail!!) {
                        if (name.nm.lowercase().trim()
                                .contains(editable.toString().lowercase().trim())
                        ) {
                            filterList.add(name)
                        }
                    }
                    adapter.updateList(filterList)
                }
            }
        })
        binding.rvGroupCar.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        if (serverData.contains("s3")) {
            adapterGps3 = GroupCarAdapterGps3(handlerAlertDialog, handlerGps3, groupListDetailGps3)
            binding.rvGroupCar.adapter = adapterGps3
        } else {
            adapter = GroupCarAdapter(handlerAlertDialog, handler, groupListDetail)
            binding.rvGroupCar.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            Utils.showProgressBar(requireContext())
            if (serverData.contains("s3")) {
                if (filterListGps3.isNotEmpty()) {
                    filterListGps3.clear()
                    binding.etSearch.setText("")
                }
                groupListDetailGps3?.clear()
                viewmodel.callApiForGroupListDataGps3()

            } else {
                if (!groupListDetail.isNullOrEmpty()) {
                    groupListDetail?.clear()
                    filterList.clear()
                    binding.etSearch.setText("")
                }
                viewmodel.callApiForGroupListData()
            }
        } catch (e: Exception) {
            Log.e(TAG, "onResume: CATCH ${e.message}")
        }
    }

    private fun setListOfCar(): String {
        val data = StringBuilder()
        for (i in 0 until carId.size) {
            data.append(carId[i])
            data.append(",")
        }
        return data.toString()
    }
}
