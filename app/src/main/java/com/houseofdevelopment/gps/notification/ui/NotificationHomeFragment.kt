package com.houseofdevelopment.gps.notification.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.houseofdevelopment.gps.MainActivity
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.base.BaseFragment
import com.houseofdevelopment.gps.base.GenericRecyclerViewAdapter
import com.houseofdevelopment.gps.databinding.AdapterNotificationHomeItemBinding
import com.houseofdevelopment.gps.databinding.FragmentNotificationHomeBinding
import com.houseofdevelopment.gps.notification.model.NotificationDataGps3
import com.houseofdevelopment.gps.notification.model.NotificationListData
import com.houseofdevelopment.gps.notification.viewModel.NotificationViewModel
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey
import com.houseofdevelopment.gps.utils.Utils
import kotlinx.android.synthetic.main.layout_custom_action_bar.*

class NotificationHomeFragment : BaseFragment() {

    lateinit var binding: FragmentNotificationHomeBinding
    lateinit var viewModel: NotificationViewModel
    var notiNameList = ArrayList<NotificationListData.FirstObj>()
    var notiNameListGps3 = ArrayList<NotificationDataGps3>()
//    lateinit var notificationHomeAdapter: NotificationHomeAdapter

    private var serverData: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_notification_home, container, false
        )

        binding.lifecycleOwner = this
        serverData = MyPreference.getValueString(PrefKey.SELECTED_SERVER_DATA, "")!!

        addObserver()
        initListener()
        /* requireActivity().onBackPressedDispatcher.addCallback(this) {
             findNavController().navigate(NotificationHomeFragmentDirections.actionNotificationHomeFragmentToHomeFragmnet())
         }*/
        initializeRecyclerview()
        return binding.root
    }

    private fun initListener() {
        (activity as MainActivity).add_vehicle.setOnClickListener {
            findNavController().navigate(R.id.action_notificationHomeFragment_to_notificationSetting)
        }
    }

    @SuppressLint("FragmentLiveDataObserve", "NotifyDataSetChanged")
    private fun addObserver() {

        if (serverData.contains("s3"))
            viewModel.notiNameListGps3.observe(this) {
                notiNameListGps3.clear()
                notiNameListGps3.addAll(it)
                if (notiNameListGps3.size > 0) {
                    binding.rvNotificationList.adapter?.notifyDataSetChanged()
                    binding.noDataLay.visibility = View.GONE
                } else {
                    binding.noDataLay.visibility = View.VISIBLE
                    binding.rvNotificationList.visibility = View.GONE
                }
            }
        else viewModel.notiNameList.observe(this) {
            Utils.hideProgressBar()
            notiNameList.clear()
            notiNameList.addAll(it)
            if (notiNameList.size > 0) {
                binding.rvNotificationList.adapter?.notifyDataSetChanged()
                binding.noDataLay.visibility = View.GONE
            } else {
                binding.noDataLay.visibility = View.VISIBLE
                binding.rvNotificationList.visibility = View.GONE
            }
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun initializeRecyclerview() {
        /*val handler = object : NotificationHomeAdapter.NotificationItemClick{
            override fun onItemClick(position: Int) {
            }

            override fun onSwitchClick(selId: String, isSwitchOn: Boolean) {
                if (isSwitchOn) {
                    viewModel.callApiForSwitchOnOff(selId, 1)
                } else {
                    viewModel.callApiForSwitchOnOff(selId, 0)
                }
            }
        }

        notificationHomeAdapter = NotificationHomeAdapter(handler, notiNameList)
        binding.rvNotificationList.layoutManager =
                LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)
        binding.rvNotificationList.adapter = notificationHomeAdapter*/
        if (serverData.contains("s3")) {
            binding.rvNotificationList.adapter = object :
                GenericRecyclerViewAdapter<NotificationDataGps3, AdapterNotificationHomeItemBinding>(
                    requireContext(),
                    notiNameListGps3
                ) {
                override val layoutResId: Int
                    get() = R.layout.adapter_notification_home_item

                @SuppressLint("NotifyDataSetChanged")
                override fun onBindData(
                    model: NotificationDataGps3,
                    position: Int,
                    dataBinding: AdapterNotificationHomeItemBinding
                ) {
                    dataBinding.txtNotiName.text = model.name
                    dataBinding.imgShowNotification.isChecked = model.notify_push.toBoolean()
                    dataBinding.imgShowNotification.setOnClickListener {
                        viewModel.callApiForSwitchOnOffGps3(
                            model.event_id, model.notify_push.toBoolean()
                        )
                        notiNameListGps3[position].notify_push =
                            (!notiNameListGps3[position].notify_push.toBoolean()).toString()
//                        notifyDataSetChanged()
                    }
                    dataBinding.executePendingBindings()
                }

                override fun onItemClick(model: NotificationDataGps3, position: Int) {
                }
            }
        } else {
            binding.rvNotificationList.adapter = object :
                GenericRecyclerViewAdapter<NotificationListData.FirstObj, AdapterNotificationHomeItemBinding>(
                    requireContext(),
                    notiNameList
                ) {
                override val layoutResId: Int
                    get() = R.layout.adapter_notification_home_item

                @SuppressLint("NotifyDataSetChanged")
                override fun onBindData(
                    model: NotificationListData.FirstObj,
                    position: Int,
                    dataBinding: AdapterNotificationHomeItemBinding
                ) {
                    dataBinding.txtNotiName.text = model.n
                    dataBinding.imgShowNotification.isChecked = model.fl == 0
//                    dataBinding.model = model
//                    dataBinding.imgShowNotification.tag = position
                    dataBinding.imgShowNotification.setOnClickListener {
                        if (notiNameList[position].fl == 0) {
                            notiNameList[position].fl = 1
                            viewModel.callApiForSwitchOnOff(notiNameList[position].id, 0)
                        } else {
                            notiNameList[position].fl = 0
                            viewModel.callApiForSwitchOnOff(notiNameList[position].id, 1)
                        }
//                        notifyDataSetChanged()
                    }
                    dataBinding.executePendingBindings()
                }

                override fun onItemClick(model: NotificationListData.FirstObj, position: Int) {

                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        handleActionBar(R.string.notification)
        (activity as MainActivity).add_vehicle.visibility = View.VISIBLE
        (activity as MainActivity).chk_check.visibility = View.INVISIBLE
        if (serverData.contains("s3"))
            viewModel.callApiForNotificationListDataGps3()
        else
            viewModel.callApiForNotificationListData()
    }
}