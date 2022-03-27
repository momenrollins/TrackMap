package com.trackmap.gps.notification.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.trackmap.gps.R
import com.trackmap.gps.base.BaseFragment
import com.trackmap.gps.databinding.ActivityAddNotificationBinding
import java.util.*

class AddNotificationFragment : BaseFragment() {

    lateinit var binding: ActivityAddNotificationBinding
    private var speedValue: Int = 0
    private var totalMin: Int = 0
    private var from: Int = 0
    private var type: Int = 0
    private var to: Int = 0
    private var isStopNotification: Boolean = false
    private var isIdlingNotification: Boolean = false
    private var isConnectionNotification: Boolean = false
    private var isGeoZoneNotification: Boolean = false
    private var isWeightNotification: Boolean = false
    private var isStartNotification: Boolean = false
    private var geoZoneIds = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.activity_add_notification, container, false
        )
        binding.lifecycleOwner = this
        handleActionBarHidePlusIcon(R.string.add_notification)

        when (arguments?.getString("comingFrom")!!) {
            "StartNotification" -> {
                isStartNotification = true
                isStopNotification = false
            }
            "SpeedNotification" -> {
                val str = arguments?.getInt("speedValue")
                speedValue = str!!.toInt()
            }
            "StopNotification" -> {
                isStopNotification = true
                isStartNotification = false
            }
            "IdleTimeNotification" -> {
                val str = arguments?.getInt("totalMin")
                totalMin = str!!.toInt()
                isIdlingNotification=true
            }
            "ConnectionTimeNotification" -> {
                val str = arguments?.getInt("totalMin")
                totalMin = str!!.toInt()

                isConnectionNotification=true
            }
            "WeightNotification" -> {
                val strfrom = arguments?.getString("from")
                val strto = arguments?.getString("to")
                val strtype = arguments?.getInt("type")
                from = strfrom!!.toInt()
                to = strto!!.toInt()
                type = strtype!!.toInt()

                isWeightNotification=true
            }
            "geoZoneEnterNotification" -> {
                geoZoneIds = arguments?.getStringArrayList("geoZoneId") as ArrayList<String>
                isGeoZoneNotification = true
                isStopNotification = false
            }
            "geoZoneExitNotification" -> {
                isGeoZoneNotification = false
                isStopNotification = false
                geoZoneIds = arguments?.getStringArrayList("geoZoneId") as ArrayList<String>
            }
        }

        binding.btnAddNotification.setOnClickListener {
            val notificationName = binding.etNotificationName.text?.trim().toString()
            if (notificationName.length in 4..50) {
                if (speedValue > 0) {
                    val bundle = bundleOf(
                        "comingFrom" to "SpeedNotification",
                        "speedValue" to speedValue,
                        "notificationName" to binding.etNotificationName.text.toString()
                    )
                    findNavController().navigate(
                        R.id.action_addNotification_to_addunitsFragment,
                        bundle
                    )
                } else {
                    if (totalMin > 0) {
                        val bundle:Bundle
                        if (isConnectionNotification){
                             bundle = bundleOf(
                                "comingFrom" to "ConnectionTimeNotification",
                                "totalMin" to totalMin,
                                "notificationName" to binding.etNotificationName.text.toString()
                            )
                        }else{
                             bundle = bundleOf(
                                "comingFrom" to "IdleTimeNotification",
                                "totalMin" to totalMin,
                                "notificationName" to binding.etNotificationName.text.toString()
                            )

                        }
                        findNavController().navigate(
                            R.id.action_addNotification_to_addunitsFragment,
                            bundle
                        )

                    } else {
                        if (isStopNotification) {
                            val bundle = bundleOf(
                                "comingFrom" to "StopNotification",
                                "notificationName" to binding.etNotificationName.text.toString()
                            )
                            findNavController().navigate(
                                R.id.action_addNotification_to_addunitsFragment,
                                bundle
                            )
                        } else {
                            when {

                                isGeoZoneNotification -> {
                                    val bundle = bundleOf(
                                        "comingFrom" to "geoZoneEnterNotification",
                                        "geoZoneId" to geoZoneIds,
                                        "notificationName" to binding.etNotificationName.text.toString()
                                    )
                                    findNavController().navigate(
                                        R.id.action_addNotification_to_addunitsFragment,
                                        bundle
                                    )
                                }
                                isWeightNotification -> {
                                    val bundle = bundleOf(
                                        "comingFrom" to "WeightNotification",
                                        "from" to from,
                                        "to" to to,
                                        "type" to type,
                                        "notificationName" to binding.etNotificationName.text.toString()
                                    )
                                    findNavController().navigate(
                                        R.id.action_addNotification_to_addunitsFragment,
                                        bundle
                                    )
                                }
                                isStartNotification -> {
                                    val bundle = bundleOf(
                                        "comingFrom" to "StartNotification",
                                        "notificationName" to binding.etNotificationName.text.toString()
                                    )
                                    findNavController().navigate(
                                        R.id.action_addNotification_to_addunitsFragment,
                                        bundle
                                    )
                                }
                                else -> {
                                    val bundle = bundleOf(
                                        "comingFrom" to "geoZoneExitNotification",
                                        "geoZoneId" to geoZoneIds,
                                        "notificationName" to binding.etNotificationName.text.toString()
                                    )
                                    findNavController().navigate(
                                        R.id.action_addNotification_to_addunitsFragment,
                                        bundle
                                    )
                                }
                            }

                        }
                    }
                }
            } else {
                if (notificationName.length <= 3) {
                    Toast.makeText(
                        requireActivity(),
                        getString(R.string.notification_name_must_contain_more_than_3_characters),
                        Toast.LENGTH_LONG
                    ).show()
                } else if (notificationName.length > 50) {
                    Toast.makeText(
                        requireActivity(),
                        getString(R.string.notification_name_must_contain_less_than_50_characters),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        return binding.root
    }
}