package com.houseofdevelopment.gps.addgroup.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.houseofdevelopment.gps.MainActivity
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.vehicallist.model.ItemGps3
import kotlinx.android.synthetic.main.adapter_add_unit_group.view.*
import kotlinx.android.synthetic.main.adapter_group_vehical_layout.view.car_image
import kotlinx.android.synthetic.main.adapter_group_vehical_layout.view.car_name
import kotlinx.android.synthetic.main.layout_custom_action_bar.*

class AddUnitAdapterGps3(
    private var mContext: Context?,

    ) : RecyclerView.Adapter<AddUnitAdapterGps3.ViewHolder>() {

    var value = ArrayList<ItemGps3>()
    var selectedItem = ArrayList<Long>()
    var isSelectAll: Boolean = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.adapter_add_unit_group, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return value.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(value[position], position, mContext)
    }

    inner class ViewHolder(var binding: View) : RecyclerView.ViewHolder(binding.rootView) {
        @SuppressLint("NotifyDataSetChanged")
        fun bind(
            itemList: ItemGps3,
            position: Int,
            mContext: Context?
        ) {
            itemView.car_name.text = itemList.name
            /*       Glide.with(mContext!!)
                       .load(R.drawable.default_car)
                       .into(itemView.car_image)*/

            itemView.car_image.setImageResource(R.drawable.default_car)

            itemView.chk_check.isChecked = itemList.isSelected
//            itemView.const_carName.tag = position
            itemView.add_unit_card.setOnClickListener {
                try {
                    Log.d(tag, "bind: qw ${value[position].isSelected}")
                    itemView.chk_check.isChecked = !itemView.chk_check.isChecked

                    if (value[position].isSelected) {
                        itemList.imei.let {
                            selectedItem.remove(itemList.imei.toLong())
                        }
                    } else {
                        itemList.imei.let { selectedItem.add(itemList.imei.toLong()) }
                    }

                } catch (e: Exception) {
                    Log.e(tag, "bind: CATCH ${e.message}")
                }
                value[position].isSelected = (!value[position].isSelected)
                var selectedCounter = 0
                value.map { if (it.isSelected) selectedCounter++ }
                if (selectedCounter == value.size) {
                    (mContext as MainActivity).chk_check.isChecked = true
                    mContext.chk_check.tag = 1
                } else {
                    (mContext as MainActivity).chk_check.isChecked = false
                    mContext.chk_check.tag = 0
                }
//                notifyDataSetChanged()
            }
        }
    }

    private val tag = "AddUnitAdapterGps3"

    @SuppressLint("NotifyDataSetChanged")
    fun selectDeselectAll(check: Boolean) {
//        selectedItem.clear()
        for (f in value) {
            try {
                if (check) {
                    f.isSelected = (true)
                    f.imei.let { selectedItem.add(it.toLong()) }
                } else {
                    f.isSelected = (false)
                    f.imei.let { selectedItem.remove(it.toLong()) }
                }
            } catch (e: Exception) {
                Log.d(tag, "bind: ${e.message}")
            }
        }
        notifyDataSetChanged()
    }

    fun add(model: ItemGps3?) {
        if (model != null) {
            value.add(model)
        }
        notifyItemInserted(value.size - 1)
    }

    fun addAll(mList: ArrayList<ItemGps3>) {
        /*for (mc in mList) {
            add(mc)
        }*/
        value = mList
    }

    fun getArrayList(): ArrayList<Long> {
        val hashSet = HashSet<Long>()
        hashSet.addAll(selectedItem)
        selectedItem.clear()
        selectedItem.addAll(hashSet)
        return selectedItem
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(filterList: ArrayList<ItemGps3>) {
        value = filterList
        /*selectedItem.clear()
        value.forEach(){
           itemGps3 -> if (itemGps3.isSelected) selectedItem.add(itemGps3.imei.toLong())
        }*/
        notifyDataSetChanged()
    }
}
