package com.trackmap.gps.addgroup.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.trackmap.gps.MainActivity
import com.trackmap.gps.R
import com.trackmap.gps.homemap.model.Item
import kotlinx.android.synthetic.main.adapter_add_unit_group.view.*
import kotlinx.android.synthetic.main.adapter_group_vehical_layout.view.car_image
import kotlinx.android.synthetic.main.adapter_group_vehical_layout.view.car_name
import kotlinx.android.synthetic.main.layout_custom_action_bar.*
import java.util.*
import kotlin.collections.ArrayList


class AddUnitAdapter(
    private var mContext: Context?
) : RecyclerView.Adapter<AddUnitAdapter.ViewHolder>() {

    lateinit var value: MutableList<Item>
    var selectedItem = ArrayList<Long>()
    var isSelectAll: Boolean = false
    private val TAG = "AddUnitAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.adapter_add_unit_group, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (value == null) 0 else value.size
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
            itemList: Item?,
            position: Int,
            mContext: Context?
        ) {
            try {
                itemView.car_name.text = itemList?.nm
                Glide.with(mContext!!)
                    .load("http://gps.tawasolmap.com" + itemList?.uri)
                    .into(itemView.car_image)

                itemView.chk_check.isChecked = itemList?.isSelected!!
//            itemView.add_unit_card.tag = position
                itemView.add_unit_card.setOnClickListener {
                    itemView.chk_check.isChecked = !itemView.chk_check.isChecked

                    if (value[position].isSelected) {
                        itemList.id?.let { it1 -> selectedItem.remove(it1.toLong()) }
                    } else {
                        itemList.id?.let { it1 -> selectedItem.add(it1.toLong()) }
                    }

                    if (selectedItem.size == value.size) {
                        (mContext as MainActivity).chk_check.isChecked = true
                        mContext.chk_check.tag = 1
                    } else {
                        (mContext as MainActivity).chk_check.isChecked = false
                        mContext.chk_check.tag = 0
                    }
                    value[position].isSelected = (!value[position].isSelected)
//                notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e(TAG, "bind: CATCH ${e.message}")
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectDeselectAll(check: Boolean) {
        selectedItem.clear()
        if (value != null)
            for (f in value) {
                if (check) {
                    f.isSelected = (true)
                    f.id?.let { selectedItem.add(it.toLong()) }
                } else {
                    f.isSelected = (false)
                    f.id?.let { selectedItem.remove(it.toLong()) }
                }
            }
        notifyDataSetChanged()
    }

    fun add(model: Item?) {
        if (model != null) {
            value?.add(model)
        }
        notifyItemInserted(value?.size!! - 1)
    }

    fun addAll(mList: MutableList<Item>) {
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
    fun updateList(filterList: MutableList<Item>) {
        try {
            value = filterList
            selectedItem.clear()
            value.forEach() { item ->
                if (item.isSelected) selectedItem.add(item.id!!.toLong())
            }
            notifyDataSetChanged()
        } catch (e: Exception) {
            Log.d(TAG, "updateList: CATCH ${e.message}")
        }
    }
}
