package com.houseofdevelopment.gps.notification.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.databinding.AdapterNotificationListBinding
import com.houseofdevelopment.gps.notification.model.NotiDetailResponse

class NotificationAdapter(
    var notificationHandler: NotificationItemClick,
    var notiDetailList: MutableList<NotiDetailResponse>
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: AdapterNotificationListBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.adapter_notification_list, parent, false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(notiDetailList1: MutableList<NotiDetailResponse>) {
        notiDetailList = notiDetailList1
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = notiDetailList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(notiDetailList[position], position)

    inner class ViewHolder(var binding: AdapterNotificationListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: NotiDetailResponse, position: Int) {

            binding.txtTitle.text = model.unit_name
            binding.txtTime.text = model.time
            binding.txtDescription.text = model.msg

            binding.constNotification.setOnClickListener {
                notificationHandler.itemClick(position, binding.txtTitle.text.toString(),
                        binding.txtDescription.text.toString(), binding.txtTime.text.toString(),
                        model.lat, model.longitude)
            }
        }
    }

    interface NotificationItemClick{
        fun itemClick(position: Int, title: String, msg: String, time: String, lat: Double, longitude: Double)
    }
}
