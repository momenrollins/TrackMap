package com.houseofdevelopment.gps.geozone.ui

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.databinding.AdapterAddUnitGroupBinding
import com.houseofdevelopment.gps.geozone.model.GeoZoneModelItemGps3
import kotlinx.android.synthetic.main.adapter_add_unit_group.view.*
import kotlinx.android.synthetic.main.adapter_add_unit_group.view.img_topUpperArrow
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class GeoZoneAdapterGps3(
    var comingFrom: String,
    var selectedZoneOnclick: SelectedZoneOnclickGps3,
    var context: Context/*,
        var geoList: MutableList<GeoZoneListModel.ZLObj>,
        var colorList: MutableList<String>,
        var idList: MutableList<String>*/
) : RecyclerView.Adapter<GeoZoneAdapterGps3.ViewHolder>() {

    lateinit var geoList: MutableList<GeoZoneModelItemGps3>
    var selectedItem = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: AdapterAddUnitGroupBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.adapter_add_unit_group, parent, false
        )
        return ViewHolder(binding)
    }

//    override fun getItemCount(): Int = geoList!!.size

    override fun getItemCount(): Int {
        return if (geoList == null) 0 else geoList?.size!!
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(
            geoList?.get(position),/* colorList.get(position), idList.get(position),*/
            position
        )

    inner class ViewHolder(var binding: AdapterAddUnitGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(model: GeoZoneModelItemGps3, /*color: String, id: String,*/ position: Int) {
            if (Locale.getDefault().getDisplayLanguage()
                    .equals("العربية")
            ) itemView.img_topUpperArrow.rotation = 270f
            else
                itemView.img_topUpperArrow.rotation = 90f
            binding.carImage.setImageDrawable(null)
            binding.carName.text = model.zone_name

            binding.carImage.visibility = View.INVISIBLE
            binding.viewColor.visibility = View.VISIBLE

            try {
                binding.viewColor.setCardBackgroundColor(Color.parseColor(model.zone_color))
            } catch (e: Exception) {
                Log.d("TAG", "bind:eee ${e.message}")
            }
            if (comingFrom.equals("notification", true)) {
                binding.imgDelete.visibility = View.GONE
                binding.imgTopUpperArrow.visibility = View.INVISIBLE
                binding.chkCheck.visibility = View.VISIBLE
                // selectedZoneOnclick.onClick()

            } else {

                binding.chkCheck.visibility = View.INVISIBLE
                binding.imgTopUpperArrow.visibility = View.VISIBLE
                binding.imgDelete.visibility = View.VISIBLE
                binding.constCarName.tag = position
            /*    binding.numberOfCars.visibility = View.VISIBLE
                Log.d("TAG", "bind:sii ${model.zone_cars.size} ")
                binding.numberOfCars.text = "${model.zone_cars.size} cars"*/

                binding.constCarName.setOnClickListener {
                    selectedZoneOnclick.onClick(geoList[binding.constCarName.tag as Int])
                    binding.constCarName.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.selected_color
                        )
                    )
                }

                binding.imgDelete.setOnClickListener {
                    selectedZoneOnclick.onDeleteClick(model.zone_id.toString(), position)
                }
            }

            itemView.chk_check.isChecked = model?.isSelected!!

            itemView.chk_check.setOnClickListener {
                if (model?.isSelected!!) {
                    model.setChkselect(false)
                    selectedItem.remove(model.zone_id.toString())
                } else {
                    model.setChkselect(true)
                    selectedItem.add(model.zone_id.toString())
                }
                itemView.chk_check.isChecked = itemView.chk_check.isChecked
            }

            /*if(position %2 == 0) {
                binding.carName.text = "Home"
            } else {
                binding.carName.text = "Office"
            }*/
        }
    }

    fun selectDiselectAll(check: Boolean) {
        selectedItem.clear()
        for (f in geoList!!) {
            if (check) {
                f.setChkselect(true)
                selectedItem.add(f.zone_id.toString())
            } else {
                f.setChkselect(false)
                selectedItem.remove(f.zone_id.toString())
            }
        }
        notifyDataSetChanged()
    }

    fun add(model: GeoZoneModelItemGps3) {
        if (model != null) {
            geoList?.add(model)
        }
        notifyItemInserted(geoList?.size!! - 1)
    }

    fun addAll(mList: MutableList<GeoZoneModelItemGps3>) {
        /*for (mc in mList) {
            add(mc)
        }*/
        geoList = mList
    }

    fun getArrayList(): ArrayList<String> {
        val hashSet = HashSet<String>()
        hashSet.addAll(selectedItem)
        selectedItem.clear()
        selectedItem.addAll(hashSet)
        return selectedItem
    }

    fun updateList(filterList: MutableList<GeoZoneModelItemGps3>) {
        geoList = filterList
        notifyDataSetChanged()
    }

    interface SelectedZoneOnclickGps3 {
        fun onClick(zlObj: GeoZoneModelItemGps3)
        fun onDeleteClick(zoneId: String, postion: Int)
    }

}
