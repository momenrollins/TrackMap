package com.houseofdevelopment.gps.addgroup.ui


import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.os.postDelayed
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.houseofdevelopment.gps.DataValues.isFromGroup
import com.houseofdevelopment.gps.DataValues.serverData
import com.houseofdevelopment.gps.MainActivity
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.addgroup.viewmodel.AddUnitsViewModel
import com.houseofdevelopment.gps.base.BaseFragment
import com.houseofdevelopment.gps.databinding.FragmentAddUnitsBinding
import com.houseofdevelopment.gps.geozone.viewmodel.GeoZoneViewModel
import com.houseofdevelopment.gps.vehicallist.model.ItemGps3
import com.houseofdevelopment.gps.homemap.model.Item
import com.houseofdevelopment.gps.notification.viewModel.NotificationViewModel
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey
import com.houseofdevelopment.gps.utils.DebugLog
import com.houseofdevelopment.gps.utils.Utils
import com.houseofdevelopment.gps.vehicallist.model.GroupListDataModel
import kotlinx.android.synthetic.main.layout_custom_action_bar.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 */
class AddUnitsFragment : BaseFragment() {

    private var comingFrom: String? = ""
    lateinit var fragment: AddUnitsFragment
    lateinit var binding: FragmentAddUnitsBinding
    lateinit var viewmodel: AddUnitsViewModel
    private lateinit var viewModel: NotificationViewModel
    private lateinit var geoZoneViewModel: GeoZoneViewModel
    lateinit var adapter: AddUnitAdapter
    lateinit var adapterGps3: AddUnitAdapterGps3
    private var carDetailList: MutableList<Item>? = null
    private var carDetailListGps3 = ArrayList<ItemGps3>()
    var groupData: GroupListDataModel.Item = GroupListDataModel.Item()

    var list = ArrayList<String>()
    var filterList = mutableListOf<Item>()
    var filterListGps3 = ArrayList<ItemGps3>()

    private var carIdList = ArrayList<Long>()
    private var allCarIdList = ArrayList<Long>()
    private var carIdListGps3 = ArrayList<String>()
    private var selectedCarListGps3 = java.util.ArrayList<ItemGps3>()
    private var selectedCarListImeisGps3 = ArrayList<String>()
    private var selectedCarList = java.util.ArrayList<Item>()

    private var groupID = ""
    private var userId = ""

    private var groupName = ""
    private var createdGId = ""
    private var geoZoneName = ""
    private var notificationName = ""
    private var isNotiCreated: Boolean = false
    private var isNotification: Boolean = false
    private var isStopNotification: Boolean = false
    private var isMovingWithoutDriver: Boolean = false
    private var isIdlingNotification: Boolean = false
    private var isConnectionNotification: Boolean = false
    private var isWeightNotification: Boolean = false
    private var isGeoZoneInNotification: Boolean = false
    private var isGeoZoneOutNotification: Boolean = false
    private var totalMin: Int = 0
    private var fromWeight: Int = 0
    private var toWeight: Int = 0
    private var typeWeight: Int = 0
    private var totalWeight: Int = 0
    private var speedValue: Int = 0
    private var convertedRGB: Int? = 0
    private var isGeoZoneAdd: Boolean = false
    private var unitsItemList = mutableListOf<String>()
    private var geozone_ids = ArrayList<String>()
    private var uniteImeis: String = ""
    private val TAG = "AddUnitsFragment"

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_add_units, container, false
        )
        viewmodel = ViewModelProvider(this)[AddUnitsViewModel::class.java]
        viewModel = ViewModelProvider(this)[NotificationViewModel::class.java]
        geoZoneViewModel = ViewModelProvider(this)[GeoZoneViewModel::class.java]

        binding.lifecycleOwner = this
        (activity as MainActivity).chk_check.tag = 0

        if (arguments != null) {
            comingFrom = arguments?.getString("comingFrom")

            when {
                comingFrom?.equals("CreateGroup", true)!! -> {
                    isNotification = false
                    isGeoZoneAdd = false
                    groupName = arguments!!.getString("groupName").toString()
                    binding.btnAddUnit.text = getString(R.string.add_unit_to_this_group)
                }
                comingFrom.equals("StartNotification", true) -> {
                    isNotification = true
                    notificationName = arguments!!.getString("notificationName").toString()
                    binding.btnAddUnit.text = getString(R.string.create_notification)
                }
                comingFrom.equals("SpeedNotification", true) -> {
                    isNotification = true
                    notificationName = arguments!!.getString("notificationName").toString()
                    speedValue = arguments!!.getInt("speedValue")
                    binding.btnAddUnit.text = getString(R.string.create_notification)
                }
                comingFrom.equals("StopNotification", true) -> {
                    isNotification = true
                    isStopNotification = true
                    notificationName = arguments!!.getString("notificationName").toString()
                    binding.btnAddUnit.text = getString(R.string.create_notification)
                }
                comingFrom.equals("IdleTimeNotification", true) -> {
                    isNotification = true
                    isIdlingNotification = true
                    notificationName = arguments!!.getString("notificationName").toString()
                    totalMin = arguments!!.getInt("totalMin")
                    binding.btnAddUnit.text = getString(R.string.create_notification)
                }
                comingFrom.equals("ConnectionTimeNotification", true) -> {
                    isNotification = true
                    isConnectionNotification = true
                    notificationName = arguments!!.getString("notificationName").toString()
                    binding.btnAddUnit.text = getString(R.string.create_notification)
                }
                comingFrom.equals("CreateGeoZone", true) -> {
                    isNotification = false
                    isGeoZoneAdd = true
                    geoZoneName = arguments!!.getString("geoZoneName").toString()
                    convertedRGB = arguments?.getInt("selectedRGB")
                    binding.btnAddUnit.text = getString(R.string.add_unit_to_this_zone)
                }
                comingFrom.equals("geoZoneEnterNotification", true) -> {
                    isNotification = true
                    isGeoZoneInNotification = true
                    isGeoZoneOutNotification = false
                    notificationName = arguments!!.getString("notificationName").toString()
                    geozone_ids =
                        arguments!!.getStringArrayList("geoZoneId") as java.util.ArrayList<String>
                    binding.btnAddUnit.text = getString(R.string.create_notification)
                }
                comingFrom.equals("geoZoneExitNotification", true) -> {
                    isNotification = true
                    isGeoZoneInNotification = false
                    isGeoZoneOutNotification = true
                    notificationName = arguments!!.getString("notificationName").toString()
                    geozone_ids =
                        arguments!!.getStringArrayList("geoZoneId") as java.util.ArrayList<String>
                    binding.btnAddUnit.text = getString(R.string.create_notification)
                }
                comingFrom?.equals("GroupUnitList", true)!! -> {
                    isNotification = false
                    isGeoZoneAdd = false
                    groupName = arguments!!.getString("groupName").toString()
                    groupID = arguments!!.getString("groupId").toString()
                    Log.d(TAG, "onCreateView: name $groupName $groupID")
                    if (serverData.contains("s3")) {
                        selectedCarListGps3 =
                            arguments!!.get("UnitList") as ArrayList<ItemGps3>
                    } else {
                        selectedCarList =
                            arguments!!.get("UnitList") as ArrayList<Item>
                    }
                    binding.btnAddUnit.text = getString(R.string.add_unit_to_this_group)
                }
            }
        }

        if (serverData != "s3") {
            if (Utils.getCarListingData(requireContext()).items.size > 0)
                carDetailList = Utils.getCarListingData(requireContext()).items
        }
        addObserver()
        try {
            initRecyclerView()
        } catch (e: Exception) {
            DebugLog.e(e.message.toString())
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    @SuppressLint("FragmentLiveDataObserve", "UseRequireInsteadOfGet")
    private fun addObserver() {
        viewmodel.groupIdData.observe(this) {
            createdGId = it
            val jsonArray = JSONArray(carIdList)
            groupID = createdGId
            viewmodel.callApiForAddUnitsToGroup(jsonArray, createdGId.toLong(), false)
        }
        viewmodel.unitList.observe(this) {
            it.let {
                binding.btnAddUnit.isEnabled = true

                unitsItemList = it as MutableList<String>
                if (unitsItemList.size > 0) {
                    Utils.hideProgressBar()
                    groupData.nm = groupName
                    groupData.id = groupID.toInt()
                    if (allCarIdList.isNotEmpty())
                        allCarIdList.forEach { s ->
                            list!!.add(s.toString())
                        }
                    else carIdList.forEach { s ->
                        list!!.add(s.toString())
                    }
                    groupData.u = list
                    val bundle = bundleOf(
                        "groupName" to groupData
                    )

                    if (comingFrom.equals("CreateGroup", true)) {
                        findNavController().navigate(
                            R.id.groupUnitListFragment,
                            bundle, NavOptions.Builder()
                                .setPopUpTo(R.id.createGroupFragment, true).build()
                        )
                    } else {
                        findNavController().navigate(
                            R.id.groupUnitListFragment,
                            bundle, NavOptions.Builder()
                                .setPopUpTo(R.id.groupUnitListFragment, true).build()
                        )
                    }
                    Toast.makeText(
                        context!!,
                        getString(R.string.successfully_added_unit_in_group),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context!!,
                        getString(R.string.something_goes_wrong),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        viewmodel.updatedUnitList.observe(this) {
            it.let {
                binding.btnAddUnit.isEnabled = true

                if (it) {
                    findNavController().popBackStack(R.id.groupUnitListFragment, true)
                    Toast.makeText(
                        context!!,
                        getString(R.string.successfully_added_unit_in_group),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context!!,
                        getString(R.string.something_goes_wrong),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        viewmodel.unitItemsGps3.observe(this) {
            Utils.hideProgressBar()
            it.let {
                binding.btnAddUnit.isEnabled = true
                if (it.data.size > 0) {

                    val bundle = bundleOf(
                        "groupNameGps3" to it
                    )
                    MyPreference.setValueBoolean("unit", false)
                    findNavController().navigate(
                        R.id.groupUnitListFragment,
                        bundle, NavOptions.Builder()
                            .setPopUpTo(R.id.createGroupFragment, true).build()
                    )
                    Toast.makeText(
                        context!!,
                        getString(R.string.group_created_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context!!,
                        getString(R.string.something_goes_wrong),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        viewmodel.updateGroupItemsGps3.observe(this) {
            it.let {
                binding.btnAddUnit.isEnabled = true
                if (it.data != null && it.data.size > 0) {

                    val bundle = bundleOf(
                        "groupNameGps3" to it
                    )
                    MyPreference.setValueBoolean("unit", false)
                    findNavController().navigate(
                        R.id.groupUnitListFragment,
                        bundle, NavOptions.Builder()
                            .setPopUpTo(R.id.groupUnitListFragment, true).build()
                    )
                    Toast.makeText(
                        context!!,
                        getString(R.string.successfully_added_unit_in_group),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context!!,
                        getString(R.string.something_goes_wrong),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        viewModel.isNotificationONCreated.observe(this) {
            isNotiCreated = it
            binding.btnAddUnit.isEnabled = true
            if (isNotiCreated) {
                Toast.makeText(
                    context!!,
                    getString(R.string.notification_added_successfully),
                    Toast.LENGTH_SHORT
                )
                    .show()
                Handler(Looper.getMainLooper()).postDelayed(500) {
                    findNavController().popBackStack(R.id.notificationSetting, true)
                }
            }
        }
    }

    @SuppressLint("UseRequireInsteadOfGet", "NotifyDataSetChanged", "SetTextI18n")
    private fun initRecyclerView() {
        binding.rvAddedCar.layoutManager =
            LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)
        if (serverData.contains("s3")) {

            carDetailListGps3 = Utils.getCarListingDataGps3(context!!).items

            for (x in selectedCarListGps3.indices) {
                selectedCarListImeisGps3.add(selectedCarListGps3[x].imei)
                for (item in carDetailListGps3.indices) {
                    if (selectedCarListGps3[x].imei == carDetailListGps3[item].imei) {
                        carDetailListGps3.remove(carDetailListGps3[item])
                        break
                    }
                }
            }
            adapterGps3 = AddUnitAdapterGps3(context)
            binding.rvAddedCar.adapter = adapterGps3

            adapterGps3.addAll(carDetailListGps3)
            adapterGps3.notifyDataSetChanged()
        } else {


            carDetailList = Utils.getCarListingData(context!!).items

            for (x in selectedCarList.indices) {
                for (item in carDetailList!!.indices) {
                    if (selectedCarList[x].id == carDetailList!![item].id) {
                        carDetailList!!.remove(carDetailList!![item])
                        break
                    }
                }
            }

            adapter = AddUnitAdapter(context)
            binding.rvAddedCar.adapter = adapter

            adapter.addAll(carDetailList!!)
        }
        if (comingFrom.equals("notification", true)) {
            binding.btnAddUnit.text = "Add Unit for Notifications"
        }

        binding.btnAddUnit.setOnClickListener {
            Utils.showProgressBar(requireContext())
            binding.btnAddUnit.isEnabled = false
            if (comingFrom.equals("notification", true)) {
                Toast.makeText(context, notificationName, Toast.LENGTH_SHORT).show()
//                findNavController().navigate(R.id.action_addunitsFragment_to_notificationSetting)
//                activity!!.supportFragmentManager.popBackStack()
//                Navigation.findNavController(getView()).popBackStack();
                findNavController().popBackStack(R.id.geoZoneFragment, true)
            } else if (comingFrom.equals("GroupUnitList")) {
                carIdList = if (serverData.contains("s3")) {
                    adapterGps3.getArrayList()
                } else {
                    adapter.getArrayList()
                }
                if (carIdList.isEmpty()) {
                    binding.btnAddUnit.isEnabled = true
                    Toast.makeText(
                        context!!,
                        getString(R.string.unit_list_is_empty),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (isGeoZoneAdd) {
                        geoZoneViewModel.callApiForAddGeoZoneData(geoZoneName, convertedRGB)
                    } else {
                        if (serverData.contains("s3")) {
                            for (item in carIdList) {
                                carIdListGps3.add(item.toString())
                            }
                            viewmodel.callApiForUpdateGroupImeisGps3(
                                groupID.toInt(),
                                carIdListGps3,
                                "add",
                                selectedCarListImeisGps3,
                                groupName
                            )
                        } else {
                            selectedCarList.forEach { item ->
                                allCarIdList.add(item.id!!.toLong())
                            }
                            carIdList.forEach { l ->
                                allCarIdList.add(l)
                            }
                            val jsonArray = JSONArray(allCarIdList)
                            Log.d(
                                TAG,
                                "initRecyclerView:callApiForAddUnitsToGroup ${jsonArray} ${groupID} "
                            )
                            viewmodel.callApiForAddUnitsToGroup(jsonArray, groupID.toLong(), false)
                        }
                    }
                }
            } else {
                if (isNotification) {

                    if (serverData.contains("s3")) {
                        carIdList = adapterGps3.getArrayList()
                        if (carIdList.isEmpty()) {
                            binding.btnAddUnit.isEnabled = true

                            Toast.makeText(
                                context!!,
                                getString(R.string.unit_list_is_empty),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Log.d(TAG, "initRecyclerView: carIdList size ${carIdList.size}")

                            uniteImeis = TextUtils.join(",", carIdList)
                            if (speedValue > 0) {
                                val map = mutableMapOf<String, Any>(
                                    "service" to "create_event",
                                    "api_key" to MyPreference.getValueString(
                                        PrefKey.ACCESS_TOKEN, ""
                                    )!!,
                                    "imei" to uniteImeis,
                                    "name" to notificationName,
                                    "s_type" to "Speed",
                                    "zones" to "",
                                    "checked_value" to speedValue.toString()
                                )
                                viewModel.callApiForNotificationEmailGps3(map)
                            } else {
                                if (totalMin > 0) {
                                    val map = mutableMapOf<String, Any>(
                                        "service" to "create_event",
                                        "api_key" to MyPreference.getValueString(
                                            PrefKey.ACCESS_TOKEN, ""
                                        )!!,
                                        "imei" to uniteImeis,
                                        "name" to notificationName,
                                        "s_type" to "Idling",
                                        "zones" to "",
                                        "checked_value" to totalMin.toString()
                                    )
                                    viewModel.callApiForNotificationEmailGps3(map)
                                } else {
                                    when {
                                        isStopNotification -> {
                                            val map = mutableMapOf<String, Any>(
                                                "service" to "create_event",
                                                "api_key" to MyPreference.getValueString(
                                                    PrefKey.ACCESS_TOKEN, ""
                                                )!!,
                                                "imei" to uniteImeis,
                                                "name" to notificationName,
                                                "s_type" to "Stop Engine",
                                                "zones" to "",
                                            )
                                            viewModel.callApiForNotificationEmailGps3(map)
                                        }
                                        isGeoZoneInNotification -> {
                                            val sValue: String = TextUtils.join(",", geozone_ids)
                                            val map = mutableMapOf<String, Any>(
                                                "service" to "create_event",
                                                "api_key" to MyPreference.getValueString(
                                                    PrefKey.ACCESS_TOKEN, ""
                                                )!!,
                                                "imei" to uniteImeis,
                                                "name" to notificationName,
                                                "s_type" to "Enter Goezone",
                                                "zones" to sValue
                                            )
                                            viewModel.callApiForNotificationEmailGps3(map)
                                        }
                                        isGeoZoneOutNotification -> {
                                            val sValue: String = TextUtils.join(",", geozone_ids)
                                            val map = mutableMapOf<String, Any>(
                                                "service" to "create_event",
                                                "api_key" to MyPreference.getValueString(
                                                    PrefKey.ACCESS_TOKEN, ""
                                                )!!,
                                                "imei" to uniteImeis,
                                                "name" to notificationName,
                                                "s_type" to "Enter Out Of Zone",
                                                "zones" to sValue
                                            )
                                            viewModel.callApiForNotificationEmailGps3(map)
                                        }
                                        else -> {
                                            val map = mutableMapOf<String, Any>(
                                                "service" to "create_event",
                                                "api_key" to MyPreference.getValueString(
                                                    PrefKey.ACCESS_TOKEN, ""
                                                )!!,
                                                "imei" to uniteImeis,
                                                "name" to notificationName,
                                                "s_type" to "Start Engine",
                                                "zones" to "",
                                            )
                                            viewModel.callApiForNotificationEmailGps3(map)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        carIdList = adapter.getArrayList()
                        if (carIdList.isEmpty()) {
                            binding.btnAddUnit.isEnabled = true

                            Toast.makeText(
                                context!!,
                                getString(R.string.unit_list_is_empty),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val jsonArray = JSONArray(carIdList)
                            if (speedValue > 0) {
                                viewModel.callApiForSpeedNotificationEmail(
                                    jsonArray, notificationName, speedValue, requireContext()
                                )
                            } else {
                                when {
                                    isStopNotification -> {
                                        viewModel.callApiForStartEngineOFFNotification(
                                            jsonArray, notificationName, requireContext()
                                        )
                                    }
                                    isIdlingNotification -> {
                                        viewModel.callApiForIdleTimeNotification(
                                            jsonArray, notificationName, totalMin, requireContext()
                                        )
                                    }
                                    isGeoZoneInNotification -> {
                                        val sValue: String = TextUtils.join(", ", geozone_ids)
                                        viewModel.callApiForGeoZoneInNotification(
                                            jsonArray,
                                            notificationName,
                                            sValue,
                                            requireContext()
                                        )
                                    }
                                    isGeoZoneOutNotification -> {

                                        val sValue: String = TextUtils.join(", ", geozone_ids)

                                        viewModel.callApiForGeoZoneOutNotification(
                                            jsonArray,
                                            notificationName,
                                            sValue,
                                            requireContext()
                                        )
                                    }
                                    else -> {
                                        Log.d("TAG", "initRecyclerView: jsonArray $jsonArray")
                                        viewModel.callApiForStartEngineONNotification(
                                            jsonArray, notificationName, requireContext()
                                        )
                                    }
                                }
                            }
                        }
                    }

                } else {
                    if (serverData.contains("s3")) {
                        carIdList = adapterGps3.getArrayList()
                        if (carIdList.isEmpty()) {
                            binding.btnAddUnit.isEnabled = true

                            Toast.makeText(
                                context!!,
                                getString(R.string.unit_list_is_empty),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            if (isGeoZoneAdd) {
                                geoZoneViewModel.callApiForAddGeoZoneData(geoZoneName, convertedRGB)
                            } else {
                                for (item in carIdList) {
                                    carIdListGps3.add(item.toString())
                                }
                                viewmodel.callApiForCreateGroupGps3(groupName, carIdListGps3)
                            }
                        }
                    } else {
                        carIdList = adapter.getArrayList()
                        if (carIdList.isEmpty()) {
                            binding.btnAddUnit.isEnabled = true

                            Toast.makeText(
                                context!!,
                                getString(R.string.unit_list_is_empty),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            if (isGeoZoneAdd) {
                                geoZoneViewModel.callApiForAddGeoZoneData(geoZoneName, convertedRGB)
                            } else {
                                viewmodel.callApiForCreateGroup(groupName)
                            }
                        }
                    }
                }
            }
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }


            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                filterList = mutableListOf()
                filterListGps3 = ArrayList()


                if (serverData.contains("s3")) {
                    for (name in carDetailListGps3) {
                        if (name.name.lowercase().trim()
                                .contains(charSequence.toString().lowercase().trim())
                        ) {
                            filterListGps3.add(name)
                        }
                    }
                    adapterGps3.updateList(filterListGps3)
                } else {
                    for (name in carDetailList!!) {
                        if (name.nm?.lowercase()?.trim()
                                ?.contains(charSequence.toString().lowercase().trim())!!
                        ) {
                            filterList.add(name)
                        }
                    }
                    adapter.updateList(filterList)
                }
                manageCheckBox()
            }

            override fun afterTextChanged(editable: Editable) {
            }
        })

        (activity as MainActivity).chk_check.setOnClickListener {
            if ((activity as MainActivity).chk_check.tag as Int == 0)
                (activity as MainActivity).chk_check.tag = 1
            else
                (activity as MainActivity).chk_check.tag = 0
            val isSelect: Boolean = (activity as MainActivity).chk_check.tag as Int > 0
            if (serverData.contains("s3")) adapterGps3.selectDeselectAll(isSelect)
            else adapter.selectDeselectAll(isSelect)
            if (isSelect) {
                (activity as MainActivity).chk_check.isChecked = true
            } else {
                (activity as MainActivity).chk_check.isChecked = false
            }
        }
    }

    fun manageCheckBox() {
        var isAllChecked = 1
        if (serverData.contains("s3")) {
            if (filterListGps3.isEmpty())
                isAllChecked = 0
            else
                for (int in 0 until filterListGps3.size) {
                    if (!adapterGps3.getArrayList()
                            .contains(filterListGps3[int].imei.trim().toLong())
                    ) isAllChecked = 0

                }
        } else {
            if (filterList.isEmpty())
                isAllChecked = 0
            else
                for (int in 0 until filterList.size) {
                    if (!adapter.getArrayList().contains(filterList[int].id?.toLong())) {
                        isAllChecked = 0
                    }
                }
        }


        (activity as MainActivity).chk_check.tag = isAllChecked
        (activity as MainActivity).chk_check.isChecked = isAllChecked != 0
    }

    override fun onResume() {
        super.onResume()
        handleActionBar(R.string.add_unit)
        (activity as MainActivity).chk_check.visibility = View.VISIBLE
        (activity as MainActivity).add_vehicle.visibility = View.INVISIBLE
        (activity as MainActivity).chk_check.isChecked = false
        if (serverData.contains("s3")) adapterGps3.selectDeselectAll(false)
        else adapter?.selectDeselectAll(false)
        isFromGroup = true
    }
}
