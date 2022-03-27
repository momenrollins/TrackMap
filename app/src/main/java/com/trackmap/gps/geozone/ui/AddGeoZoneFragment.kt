package com.trackmap.gps.geozone.ui

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.trackmap.gps.MainActivity
import com.trackmap.gps.R
import com.trackmap.gps.base.BaseFragment
import com.trackmap.gps.databinding.FragmentAddGeoZoneBinding
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import com.trackmap.gps.utils.Utils
import kotlinx.android.synthetic.main.layout_custom_action_bar.*


class AddGeoZoneFragment : BaseFragment() {

    lateinit var binding: FragmentAddGeoZoneBinding
    lateinit var adapter: AddGeoZoneAdapter
    var selectedRGB: String? = ""
    private var serverData: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_add_geo_zone, container, false
        )
        (activity as MainActivity).add_vehicle.visibility = View.INVISIBLE
        serverData = MyPreference.getValueString(PrefKey.SELECTED_SERVER_DATA, "")!!

        binding.btnSelectZoneOnMap.setOnClickListener {

            Utils.hideSoftKeyboard(requireActivity())
            val comingFrom = if (serverData.contains("s3")) "addGeoGps3" else "addGeo"
            if (binding.etZoneName.text?.trim().toString().length > 3) {
                val bundle = bundleOf(
                    "comingFrom" to comingFrom,
                    "geoZoneName" to binding.etZoneName.text.toString(),
                    "selectedRGB" to selectedRGB
                )
                Log.d(TAG, "onCreateView: selectedRGB $selectedRGB")
                /*   if (serverData.contains("s3")) {
                       *//*requireContext().startActivity(
                        Intent(
                            context,
                            PolyActivity::class.java
                        ).putExtras(bundle)
                    )*//*
                    findNavController()?.navigate(
                        R.id.action_addGeoZoneFragment_to_commonMapFragment,
                        bundle
                    )
                } else */
                findNavController()?.navigate(
                    R.id.action_addGeoZoneFragment_to_commonMapFragment,
                    bundle
                )

                /* findNavController().navigate(
                         AddGeoZoneFragmentDirections.actionAddGeoZoneFragmentToCommonMapFragment(
                                 "addGeo"
                         )
                 )*/
            } else {
                val dataLength = binding.etZoneName.text?.trim().toString().length
                if (dataLength <= 3) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.zone_name_must_contain_more_than_3_characters),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enter_valid_zone_name),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            //findNavController().navigateUp()
        }

        getColorList()
        return binding.root
    }

    private fun getColorList() {
        var colorList = ArrayList<String>()

        colorList.add("#A7C4F7")
        colorList.add("#9D8ECC")
        colorList.add("#77BE7F")
        colorList.add("#F5BC6B")
        colorList.add("#E87A70")
        colorList.add("#B0806E")

        initializeAdapter(colorList)
    }

    private val TAG = "AddGeoZoneFragment"

    @SuppressLint("UseRequireInsteadOfGet")
    private fun initializeAdapter(colorList: ArrayList<String>) {
        val handler = object : AddGeoZoneAdapter.OnClickColorList {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onColorClick(hexCode: String, position: Int) {
//                var rgb = Color.argb(Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color))
//                Log.d("Get rgb", ":" + rgb.toBigInteger())
                selectedRGB = hexCode
                Log.d(TAG, "onColorClick: $selectedRGB - ${selectedRGB!!.removeRange(0, 1)}")
            }
        }
        selectedRGB = colorList[0]
        adapter = AddGeoZoneAdapter(context!!, colorList, handler)
        binding.rvColorList.layoutManager =
            LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
        binding.rvColorList.adapter = adapter
    }


    override fun onResume() {
        super.onResume()
        handleActionBarHidePlusIcon(R.string.add_geo_unit)
    }
}
