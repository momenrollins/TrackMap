package com.trackmap.gps.notification.ui


import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.trackmap.gps.DataValues.serverData
import com.trackmap.gps.MainActivity
import com.trackmap.gps.R
import com.trackmap.gps.base.BaseFragment
import com.trackmap.gps.databinding.FragmentNotificationSettingBinding
import com.trackmap.gps.utils.InputFilterMinMax
import kotlinx.android.synthetic.main.layout_custom_action_bar.*
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class NotificationSetting : BaseFragment() {

    lateinit var binding: FragmentNotificationSettingBinding
    var speedValue: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_notification_setting, container, false
        )
        if (Locale.getDefault().displayLanguage.equals("العربية")) {
            binding.imgTopUpperArrow.rotation = 270F
            binding.imgStopUpperArrow.rotation = 270F
            binding.imgtopSpeedArrow.rotation = 270F
            binding.imgidlingArrow.rotation = 270F
            binding.imgGeoZoneUpperArrow.rotation = 270F
            binding.imgGeoZoneExitUpperArrow.rotation = 270F

        } else {
            binding.imgTopUpperArrow.rotation = 90F
            binding.imgStopUpperArrow.rotation = 90F
            binding.imgtopSpeedArrow.rotation = 90F
            binding.imgidlingArrow.rotation = 90F
            binding.imgGeoZoneUpperArrow.rotation = 90F
            binding.imgGeoZoneExitUpperArrow.rotation = 90F
        }
        if (serverData.contains("s3")) {
            binding.weightGps.visibility = View.GONE
            binding.radioGroup.visibility = View.GONE
            binding.constMovingWithout.visibility = View.GONE
        }
        else binding.weightGps3.visibility=View.GONE
        initializeListeners()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        handleActionBarHidePlusIcon(R.string.notification_setting)
        (activity as MainActivity).add_vehicle.visibility = View.INVISIBLE
        (activity as MainActivity).chk_check.visibility = View.INVISIBLE
    }

    private fun initializeListeners() {
        binding.etHh.filters = arrayOf<InputFilter>(InputFilterMinMax("0", "23"))
        binding.etMin.filters = arrayOf<InputFilter>(InputFilterMinMax("0", "59"))
        binding.constStartEngine.setOnClickListener {
            val bundle = bundleOf(
                "comingFrom" to "StartNotification"
            )
            findNavController().navigate(
                R.id.action_notificationHomeFragment_to_addNotification,
                bundle
            )
        }


        binding.constConnectioLoss.setOnClickListener {
            if (binding.connectionEtHh1.text!!.trim().toString()
                    .isNotEmpty() && binding.connectionEtMin1.text!!.trim()
                    .toString().isNotEmpty()
            ) {
                var hrs = binding.connectionEtHh1.text.toString()
                val min = binding.connectionEtMin1.text.toString()

                if (hrs.isEmpty())
                    hrs = "0"

                val totalMin = (hrs.toInt() * 60) + min.toInt()

                val bundle = bundleOf(
                    "comingFrom" to "ConnectionTimeNotification",
                    "totalMin" to totalMin
                )

                findNavController().navigate(
                    R.id.action_notificationHomeFragment_to_addNotification,
                    bundle
                )
            } else {
                Log.d("TAG", "initializeListeners:conn ${binding.connectionEtHh1.text}  ")
                if (binding.connectionEtHh1.text!!.trim().toString()
                        .isEmpty() && binding.txtConnectionMin.text!!.trim()
                        .toString().isNotBlank()
                ) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enter_connection_time_hours),
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (binding.connectionEtMin1.text!!.trim().toString()
                        .isEmpty() && binding.connectionEtHh1.text!!.trim().toString()
                        .isNotBlank()
                ) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enter_connection_time_minutes),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enter_valid_connection_time),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


        }


        binding.constWeight.setOnClickListener {

            if (serverData.contains("s3")){
                if (binding.weight.text.toString().trim().isNotEmpty()){
                    val bundle = bundleOf(
                        "comingFrom" to "WeightNotification",
                        "total_weight" to binding.weight.text.toString().trim()
                    )

                    findNavController().navigate(
                        R.id.action_notificationHomeFragment_to_addNotification,
                        bundle
                    )
                }else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enter_valid_weight),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }else{
                if (binding.txtWeightHH1.text!!.trim().toString()
                        .isNotEmpty() && binding.txtWeightMin.text!!.trim()
                        .toString().isNotEmpty()
                ) {
                    var from = binding.txtWeightHH1.text.toString()
                    val to = binding.txtWeightMin.text.toString()
                    var type=2


                    when (binding.radioGroup.getCheckedRadioButtonId()) {
                        R.id.out_range -> {
                            type=1
                        }
                        R.id.in_range -> {
                            type=0

                        }

                    }
                    if (type!=2){

                        Log.d("TAG", "initializeListeners:aa $from")

                        val bundle = bundleOf(
                            "comingFrom" to "WeightNotification",
                            "from" to from,
                            "to" to to,
                            "type" to type
                        )

                        findNavController().navigate(
                            R.id.action_notificationHomeFragment_to_addNotification,
                            bundle
                        )
                    }else{
                        Toast.makeText(
                            requireContext(),
                            "please enter type of range",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    if (binding.txtWeightHH1.text!!.trim().toString()
                            .isEmpty() && binding.txtWeightMin.text!!.trim()
                            .toString().isNotBlank()
                    ) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.please_enter_start_range_of_weight),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (binding.txtWeightMin.text!!.trim().toString()
                            .isEmpty() && binding.txtWeightHH1.text!!.trim().toString()
                            .isNotBlank()
                    ) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.please_enter_end_range_of_weight),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.please_enter_valid_weight),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }




        }


        binding.constStopEngine.setOnClickListener {
            val bundle = bundleOf(
                "comingFrom" to "StopNotification"
            )
            findNavController().navigate(
                R.id.action_notificationHomeFragment_to_addNotification,
                bundle
            )
        }
        binding.constMovingWithout.setOnClickListener {
            val bundle = bundleOf(
                "comingFrom" to "MovingWithoutDriver"
            )
            findNavController().navigate(
                R.id.action_notificationHomeFragment_to_addNotification,
                bundle
            )
        }

        binding.constIdlingTime.setOnClickListener {
            if (binding.etHh.text!!.trim().toString().isNotEmpty() && binding.etMin.text!!.trim()
                    .toString().isNotEmpty()
            ) {
                var hrs = binding.etHh.text.toString()
                val min = binding.etMin.text.toString()

                if (hrs.isEmpty())
                    hrs = "0"

                val totalMin = (hrs.toInt() * 60) + min.toInt()

                val bundle = bundleOf(
                    "comingFrom" to "IdleTimeNotification",
                    "totalMin" to totalMin
                )
                findNavController().navigate(
                    R.id.action_notificationHomeFragment_to_addNotification,
                    bundle
                )
            } else {
                if (binding.etHh.text!!.trim().toString().isEmpty() && binding.etMin.text!!.trim()
                        .toString().isNotBlank()
                ) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enter_ideal_time_hours),
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (binding.etMin.text!!.trim().toString()
                        .isEmpty() && binding.etHh.text!!.trim().toString().isNotBlank()
                ) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enter_ideal_time_minutes),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enter_valid_idle_time),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }




/*
        binding.seekBarSpeed.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(
                seek: SeekBar,
                progress: Int, fromUser: Boolean
            ) {
                // write custom code for progress is changed
                binding.txtSpeed.text = "${getString(R.string.max_speed)} $progress"
            }




            binding.constTopSpeedLimit.setOnClickListener {
                val passValue: Int = if (speedValue == 0) {
                    120
                } else {
                    speedValue
                }

                val bundle = bundleOf(
                    "comingFrom" to "SpeedNotification",
                    "speedValue" to passValue
                )
                findNavController().navigate(
                    R.id.action_notificationHomeFragment_to_addNotification,
                    bundle
                )
            }
        })
*/
            (activity as MainActivity).add_vehicle.visibility = View.INVISIBLE
        }
        binding.constGeoZoneEnter.setOnClickListener {

            val bundle = bundleOf(
                "comingFrom" to "notification",
                "type" to "geoZoneEnterNotification"
            )
            findNavController().navigate(
                R.id.action_notificationSetting_to_geoZoneFragment,
                bundle
            )

            /*findNavController().navigate(
                NotificationSettingDirections.actionNotificationSettingToGeoZoneFragment(
                        "notification"
                )
        )*/
        }

        binding.constGeoZoneExit.setOnClickListener {
            val bundle = bundleOf(
                "comingFrom" to "notification",
                "type" to "geoZoneExitNotification"
            )
            findNavController().navigate(
                R.id.action_notificationSetting_to_geoZoneFragment,
                bundle
            )
        }

        binding.constTopSpeedLimit.setOnClickListener {
            if (binding.seekBarSpeed0.text.isNullOrEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.enter_max_speed),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                speedValue = binding.seekBarSpeed0.text.toString().toInt()
                val passValue: Int = if (speedValue == 0) {
                    120
                } else {
                    speedValue
                }

                val bundle = bundleOf(
                    "comingFrom" to "SpeedNotification",
                    "speedValue" to passValue
                )
                findNavController().navigate(
                    R.id.action_notificationHomeFragment_to_addNotification,
                    bundle
                )
            }
        }

/*
            binding.seekBarSpeed.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(
                    seek: SeekBar,
                    progress: Int, fromUser: Boolean
                ) {
                    // write custom code for progress is changed
                    binding.txtSpeed.text = "${getString(R.string.max_speed)} $progress"
                }

                override fun onStartTrackingTouch(seek: SeekBar) {
                    // write custom code for progress is started
                }

                override fun onStopTrackingTouch(seek: SeekBar) {
                    // write custom code for progress is stopped
                    speedValue = seek.progress
                }
            })
*/
        (activity as MainActivity).add_vehicle.visibility = View.INVISIBLE
    }
}