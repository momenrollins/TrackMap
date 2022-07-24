package com.houseofdevelopment.gps.notification.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.houseofdevelopment.gps.DataValues.tz
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.databinding.AdapterNotificationListBinding
import com.houseofdevelopment.gps.notification.model.NotificationDataGps3
import com.houseofdevelopment.gps.utils.Utils
import java.text.SimpleDateFormat

class NotificationAdapterGps3(
    var context: Context,
    var notificationHandler: NotificationItemClick,
    var notiDetailList: MutableList<NotificationDataGps3>
) : RecyclerView.Adapter<NotificationAdapterGps3.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: AdapterNotificationListBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.adapter_notification_list, parent, false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(notiDetailList1: MutableList<NotificationDataGps3>) {
        notiDetailList = notiDetailList1
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = notiDetailList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(notiDetailList[position], position)

    inner class ViewHolder(var binding: AdapterNotificationListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(model: NotificationDataGps3, position: Int) {

            val date = Utils.getDateFromMillis(
                getTimeStamp(model.dt_tracker) + tz.getOffset(
                    getTimeStamp(model.dt_tracker)
                ),
                "yyyy-MM-dd HH:mm:ss"
            )
            binding.txtTitle.text = model.name

            binding.txtTime.text = date
            binding.txtDescription.text =
                "${context.getString(R.string.notif_title)} :  ${model.desc}\n" +
                        "${context.getString(R.string.unit_name)} :  ${model.name}\n" +
                        "${context.getString(R.string.in_location)} :  ${model.pos}\n" +
                        "${context.getString(R.string.in_time)} :  ${date!!.trim()}\n" +
                        "${context.getString(R.string.speed)} :  ${model.speed}"
            binding.constNotification.setOnClickListener {
                notificationHandler.itemClick(
                    position, binding.txtTitle.text.toString(),
                    binding.txtDescription.text.toString(), binding.txtTime.text.toString(),
                    model.lat.toDouble(), model.lng.toDouble()
                )
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTimeStamp(dateString: String?): Long {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString!!)
        return date!!.time
    }

    interface NotificationItemClick {
        fun itemClick(
            position: Int,
            title: String,
            msg: String,
            time: String,
            lat: Double,
            longitude: Double
        )
    }
}
