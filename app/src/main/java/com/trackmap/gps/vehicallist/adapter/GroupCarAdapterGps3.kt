package com.trackmap.gps.vehicallist.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.trackmap.gps.R
import com.trackmap.gps.base.AppBase
import com.trackmap.gps.utils.CommonAlertDialog
import com.trackmap.gps.vehicallist.model.ItemGroupDataModelGps3
import kotlinx.android.synthetic.main.adapter_group_vehical_layout.view.*
import java.util.*

class GroupCarAdapterGps3(
    var handlerAlertDialog: CommonAlertDialog.DeleteCarHandler?,
    var viewOnclick: GroupOnclickGps3,
    var value: ArrayList<ItemGroupDataModelGps3>?
) : RecyclerView.Adapter<GroupCarAdapterGps3.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.adapter_group_vehical_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = value?.size!!

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(value?.get(position), position)

    inner class ViewHolder(var binding: View) : RecyclerView.ViewHolder(binding.rootView) {
        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        fun bind(
            itemList: ItemGroupDataModelGps3?, position: Int
        ) {
            with(itemView) {
                if (Locale.getDefault().displayLanguage
                        .equals("العربية")
                ) itemView.imgtopUpperArrow.rotation = 270f
                else
                    itemView.imgtopUpperArrow.rotation = 90f
                itemView.car_name.text = itemList?.group_name
                itemView.car_lastUpdate.text =
                    context.getString(R.string.total_units) + " " + itemList?.count

                Glide.with(context)
                    .load("http://gps2.tawasolmap.com/avl_library_image/3/0/library/group/default.svg")
                    .placeholder(R.drawable.default_car)
                    .into(itemView.car_image)
                if (itemList?.isSelected == true)
                    itemView.const_cardetails.setBackgroundColor(
                        ContextCompat.getColor(
                            context!!,
                            R.color.selected_color
                        )
                    )
                else
                    itemView.const_cardetails.setBackgroundColor(
                        ContextCompat.getColor(
                            context!!,
                            R.color.color_white
                        )
                    )
                itemView.img_delete.setOnClickListener {
                    CommonAlertDialog.showYesNoAlerter(
                        context!!,
                        handlerAlertDialog!!,
                        itemList!!.group_id.toInt(),
                        position,
                        context.getString(R.string.delete_group),
                        context.getString(R.string.are_you_sure_n_you_want_to_delete_this_group)
                    )
                }
                itemView.imgtopUpperArrow.tag = position
                itemView.imgtopUpperArrow.setOnClickListener {
                    viewOnclick.onClickGps3(position, itemList!!.group_name)
                }
                itemView.const_cardetails.tag = position
                itemView.const_cardetails.setOnClickListener {
                    if (itemList?.count!=0){
                        for (i in 0 until value!!.size) {
                            value!![i].isSelected = false
                        }
                        value!![it.tag as Int].isSelected = true
                        viewOnclick.itemOnClick(
                            value!!, it.tag as Int,
                            value!![it.tag as Int].isSelected
                        )
                    }else{
                        Toast.makeText(context,
                            AppBase.instance.getText(R.string.this_group_is_empty), Toast.LENGTH_SHORT).show()
                    }
                    notifyDataSetChanged()


                    //binding.const_cardetails.setBackgroundColor(ContextCompat.getColor(context,R.color.selected_color))
                }

            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(filterList: ArrayList<ItemGroupDataModelGps3>) {
        value = filterList
        notifyDataSetChanged()
    }

    interface GroupOnclickGps3 {
        fun onClickGps3(position: Int, groupName: String)
        fun itemOnClick(value: ArrayList<ItemGroupDataModelGps3>, position: Int, selected: Boolean)
    }
}
