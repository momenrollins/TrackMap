package com.trackmap.gps.addgroup.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.trackmap.gps.DataValues
import com.trackmap.gps.MainActivity
import com.trackmap.gps.R
import com.trackmap.gps.base.BaseFragment
import com.trackmap.gps.databinding.FragmentCreateGroupBinding
import com.trackmap.gps.utils.Utils
import com.trackmap.gps.vehicallist.viewmodel.VehiclesListViewModel
import kotlinx.android.synthetic.main.layout_custom_action_bar.*

/**
 * A simple [Fragment] subclass.
 */
class CreateGroupFragment : BaseFragment() {

    lateinit var binding: FragmentCreateGroupBinding
    lateinit var viewModel: VehiclesListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_create_group, container, false
        )
        viewModel = ViewModelProvider(this).get(VehiclesListViewModel::class.java)
        binding.lifecycleOwner = this
        initializeListener()

        return binding.root
    }

    private fun initializeListener() {
        binding.btnAddUnitGrp.setOnClickListener {
            val strValue = binding.etSearch.text?.trim()
            if (!strValue.isNullOrEmpty() && (strValue.length > 3) && (strValue.length <= 50)) {


                if (!DataValues.serverData.contains("s3") && Utils.getCarListingData(requireContext()).items != null && Utils.getCarListingData(
                        requireContext()
                    ).items.size > 0

                    || DataValues.serverData.contains("s3") && Utils.getCarListingDataGps3(
                        requireContext()
                    ).items != null && Utils.getCarListingDataGps3(requireContext()).items.size > 0
                ) {
                    val bundle = bundleOf(
                        "comingFrom" to "CreateGroup",
                        "groupName" to strValue.toString()
                    )
                    findNavController().navigate(
                        R.id.addunitsFragment,
                        bundle
                    )
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(
                            R.string.unit_list_is_empty
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } else {
                when {
                    strValue?.length!! <= 3 -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.group_name_must_contain_more_than_3_characters),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    strValue.length > 50 -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.group_name_must_contain_less_than_50_characters),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.please_enter_valid_group_name),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handleActionBarHidePlusIcon(R.string.add_group)
        (activity as MainActivity).chk_check.visibility = View.INVISIBLE
    }

}
