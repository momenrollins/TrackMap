package com.houseofdevelopment.gps.notification.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.notification.model.NotificationListData
import kotlinx.android.synthetic.main.adapter_notification_home_item.view.*
import java.util.*

class NotificationHomeAdapter(
    var listener: NotificationItemClick,
    private var valuelist: List<NotificationListData.FirstObj>?
) :
    RecyclerView.Adapter<NotificationHomeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.adapter_notification_home_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = valuelist?.size!!

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(valuelist?.get(position), position)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(value: NotificationListData.FirstObj?, position: Int) {
            if (Locale.getDefault().getDisplayLanguage().equals("العربية"))
                itemView.txt_NotiName.setTextDirection(View.TEXT_DIRECTION_RTL)
            else
                itemView.txt_NotiName.setTextDirection(View.TEXT_DIRECTION_LTR)


            itemView.txt_NotiName.text = value!!.n


            var flValue = value.fl

            itemView.img_showNotification.isChecked = flValue == 0
            itemView.img_showNotification.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked)
                    valuelist!![position].fl = 0
                else
                    valuelist!![position].fl = 1

                listener.onSwitchClick(valuelist!![position].id, isChecked)
            }
            /*itemView.img_showNotification.setOnClickListener {
                itemView.img_showNotification.isSelected = !itemView.img_showNotification.isSelected

                if (itemView.img_showNotification.isSelected) {
                    itemView.img_showNotification.setButtonDrawable(R.drawable.ic_radio_on_green)
                    listener.onSwitchClick(value!!.id, true)
                } else {
                    itemView.img_showNotification.setButtonDrawable(R.drawable.ic_radio_off_white)
                    listener.onSwitchClick(value!!.id, false)
                }
            }*/
        }
    }

    interface NotificationItemClick {
        fun onItemClick(position: Int)
        fun onSwitchClick(selId: String, isSwitchOn: Boolean)
    }

}