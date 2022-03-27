package com.trackmap.gps.vehicallist.ui


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.trackmap.gps.MainActivity
import com.trackmap.gps.R
import com.trackmap.gps.base.BaseFragment
import com.trackmap.gps.databinding.FragmentVehicalsListBinding
import com.trackmap.gps.utils.ViewPagerAdapter
import com.trackmap.gps.vehicallist.model.ConvertModel
import com.trackmap.gps.vehicallist.viewmodel.VehiclesListViewModel
import kotlinx.android.synthetic.main.layout_custom_action_bar.*

class VehicalsListFragment : BaseFragment() {

    lateinit var binding: FragmentVehicalsListBinding
    lateinit var viewmodel: VehiclesListViewModel
    var selectedPosition = 0
    private val fragmentList = ArrayList<Fragment>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentList.add(SingleVehicalFragment())
        fragmentList.add(GroupVehicalFragment())
        (activity as MainActivity).chk_check.tag = 0 // 0 for unchecked
        (activity as MainActivity).chk_check.isChecked = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_vehicals_list, container, false
        )
        viewmodel = ViewModelProvider(this)[VehiclesListViewModel::class.java]
        val adapter = ViewPagerAdapter(requireContext(), childFragmentManager, fragmentList)
        binding.viewPager.adapter = adapter
        ConvertModel().isGroupPage = false
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        binding.viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.tabLayout))
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.viewPager.currentItem = tab?.position!!
                selectedPosition = tab.position
                ConvertModel().isGroupPage = !ConvertModel().isGroupPage
                toolBarVisible()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        binding.viewPager.setPagingEnabled(false)
        (activity as MainActivity).add_vehicle.visibility = View.INVISIBLE

        (activity as MainActivity).add_vehicle.setOnClickListener {
            findNavController().navigate(R.id.action_vehicalsListFragment_to_createGroupFragment)
        }

        (activity as MainActivity).chk_check.setOnClickListener {
            Log.d(TAG, "onCreateView: SELECTED")
            /*if ((activity as MainActivity).chk_check.tag as Int == 0) (activity as MainActivity).chk_check.tag =
                1
            else
                (activity as MainActivity).chk_check.tag = 0*/
            (fragmentList[0] as SingleVehicalFragment).checkAllItems((activity as MainActivity).chk_check.isChecked)
        }

        return binding.root
    }

    private val TAG = "VehicalsListFragment"
    private fun toolBarVisible() {
        if (selectedPosition == 0) {
            (activity as MainActivity).chk_check.visibility = View.VISIBLE
            (activity as MainActivity).add_vehicle.visibility = View.INVISIBLE
        } else {
            (activity as MainActivity).chk_check.visibility = View.INVISIBLE
            (activity as MainActivity).add_vehicle.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        handleActionBar(R.string.vehicles)
        toolBarVisible()
    }
}
