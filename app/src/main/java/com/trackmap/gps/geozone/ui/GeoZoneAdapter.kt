package com.trackmap.gps.geozone.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.trackmap.gps.R
import com.trackmap.gps.databinding.AdapterAddUnitGroupBinding
import com.trackmap.gps.geozone.model.GeoZoneListModel
import kotlinx.android.synthetic.main.adapter_add_unit_group.view.*
import kotlinx.android.synthetic.main.adapter_add_unit_group.view.img_topUpperArrow
import java.util.*
import kotlin.collections.ArrayList

class GeoZoneAdapter(
    var comingFrom: String,
    var selectedZoneOnclick: SelectedZoneOnclick,
    var context: Context/*,
        var geoList: MutableList<GeoZoneListModel.ZLObj>,
        var colorList: MutableList<String>,
        var idList: MutableList<String>*/
) : RecyclerView.Adapter<GeoZoneAdapter.ViewHolder>() {

    lateinit var geoList: MutableList<GeoZoneListModel.ZLObj>
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

    private val TAG = "GeoZoneAdapter"

    inner class ViewHolder(var binding: AdapterAddUnitGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(model: GeoZoneListModel.ZLObj, /*color: String, id: String,*/ position: Int) {
            if (Locale.getDefault().getDisplayLanguage()
                    .equals("العربية")
            ) itemView.img_topUpperArrow.rotation = 270f
            else
                itemView.img_topUpperArrow.rotation = 90f
            binding.carImage.setImageDrawable(null)
            binding.carName.text = model.n

            binding.carImage.visibility = View.INVISIBLE
            binding.viewColor.visibility = View.VISIBLE

            val hexColor = java.lang.String.format("#%06X", 0xFFFFFF and model.c.toInt())
            binding.viewColor.setCardBackgroundColor(Color.parseColor(hexColor))
            Log.d(TAG, "bind:co ${model.id.toInt()} to $hexColor")

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
                Log.d(TAG, "bind: ZONE ID ${model.id}")
                var counter = model.getUnitIds().size

                binding.numberOfCars.visibility = View.VISIBLE
                binding.numberOfCars.text = "$counter cars"
                binding.constCarName.setOnClickListener {
                    selectedZoneOnclick.onClick(geoList[position])
                    binding.constCarName.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.selected_color)
                    )
                }
                binding.imgDelete.setOnClickListener {
                    selectedZoneOnclick.onDeleteClick(
                        binding.carName.text.toString(),
                        model.id.toString(),
                        position
                    )
                }
            }

            itemView.chk_check.isChecked = model?.isSelected!!

            itemView.add_unit_card.setOnClickListener {
                if (model?.isSelected!!) {
                    model.setChkselect(false)
                    selectedItem.remove(model.id.toString())
                } else {
                    model.setChkselect(true)
                    selectedItem.add(model.id.toString())
                }
                itemView.chk_check.isChecked = !itemView.chk_check.isChecked
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
                selectedItem.add(f.id.toString())
            } else {
                f.setChkselect(false)
                selectedItem.remove(f.id.toString())
            }
        }
        notifyDataSetChanged()
    }

    fun add(model: GeoZoneListModel.ZLObj?) {
        if (model != null) {
            geoList?.add(model)
        }
        notifyItemInserted(geoList?.size!! - 1)
    }

    fun addAll(mList: MutableList<GeoZoneListModel.ZLObj>) {
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

    fun updateList(filterList: MutableList<GeoZoneListModel.ZLObj>) {
        geoList = filterList
        notifyDataSetChanged()
    }

    interface SelectedZoneOnclick {
        fun onClick(zlObj: GeoZoneListModel.ZLObj)
        fun onDeleteClick(name: String, id: String, position: Int)
    }
}
