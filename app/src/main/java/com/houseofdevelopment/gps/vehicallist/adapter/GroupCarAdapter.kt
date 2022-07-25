package com.houseofdevelopment.gps.vehicallist.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.base.AppBase
import com.houseofdevelopment.gps.utils.CommonAlertDialog
import com.houseofdevelopment.gps.vehicallist.model.GroupListDataModel
import kotlinx.android.synthetic.main.adapter_group_vehical_layout.view.*
import java.util.*

class GroupCarAdapter(
    var handlerAlertDialog: CommonAlertDialog.DeleteCarHandler?,
    var viewOnclick: GroupOnclick,
    var value: MutableList<GroupListDataModel.Item>?
) : RecyclerView.Adapter<GroupCarAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.adapter_group_vehical_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = value?.size!!

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(value?.get(position), position)

    private val TAG = "GroupCarAdapter"

    inner class ViewHolder(var binding: View) : RecyclerView.ViewHolder(binding.rootView) {
        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        fun bind(
            itemList: GroupListDataModel.Item?,
            position: Int
        ) {
            with(itemView) {
                if (Locale.getDefault().displayLanguage
                        .equals("العربية")
                ) itemView.imgtopUpperArrow.rotation = 270f
                else
                    itemView.imgtopUpperArrow.rotation = 90f
                itemView.car_name.text = itemList?.nm
                itemView.car_lastUpdate.text =
                    context.getString(R.string.total_units) + " " + itemList?.u?.size.toString()
                Glide.with(context)
                    .load("http://gps.hod.sa" + itemList?.uri)
                    .placeholder(R.drawable.default_car)
                    .into(itemView.car_image)
                Log.d(TAG, "bind: URI Group ${"http://gps.hod.sa" + itemList?.uri}")
                if (itemList != null && itemList.isSelected)
                    itemView.const_cardetails.setBackgroundColor(
                        ContextCompat.getColor(context!!, R.color.selected_color)
                    )
                else
                    itemView.const_cardetails.setBackgroundColor(
                        ContextCompat.getColor(context!!, R.color.color_white)
                    )
                itemView.img_delete.setOnClickListener {
                    CommonAlertDialog.showYesNoAlerter(
                        context!!, handlerAlertDialog!!,
                        itemList?.id!!, position,
                        context.getString(R.string.delete_group),
                        context.getString(R.string.are_you_sure_n_you_want_to_delete_this_group)
                    )
                }
                if (Locale.getDefault().displayLanguage == "العربية") itemView.imgtopUpperArrow.rotation =
                    270f
                itemView.imgtopUpperArrow.tag = position
                itemView.imgtopUpperArrow.setOnClickListener {
                    viewOnclick.onClick(value!![it.tag as Int])
                }
                itemView.const_cardetails.tag = position
                itemView.const_cardetails.setOnClickListener {

                    if (itemList?.u?.size != 0) {
                        for (i in 0 until value!!.size) {
                            value!![i].isSelected = false
                        }
                        value!![it.tag as Int].isSelected = true
                        viewOnclick.itemOnClick(it.tag as Int, value!![it.tag as Int].isSelected,
                            value!![position].u)
                    }else{
                        Toast.makeText(context,AppBase.instance.getText(R.string.this_group_is_empty), Toast.LENGTH_SHORT).show()

                    }
                    notifyDataSetChanged()
                    //binding.const_cardetails.setBackgroundColor(ContextCompat.getColor(context,R.color.selected_color))
                }

            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(filterList: MutableList<GroupListDataModel.Item>) {
        value = filterList
        notifyDataSetChanged()
    }

    fun getList():
            MutableList<GroupListDataModel.Item> {
        return value!!
    }

    interface GroupOnclick {
        fun onClick(nm: GroupListDataModel.Item)
        fun itemOnClick(position: Int, selected: Boolean,idsList: MutableList<String>)
    }
}
