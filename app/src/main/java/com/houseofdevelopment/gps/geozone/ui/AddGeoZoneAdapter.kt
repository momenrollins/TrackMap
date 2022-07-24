package com.houseofdevelopment.gps.geozone.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.houseofdevelopment.gps.R
import kotlinx.android.synthetic.main.layout_geo_color_item.view.*

class AddGeoZoneAdapter(var context: Context,
                        private var colorList: ArrayList<String>,
                        var handler: OnClickColorList
)
    : RecyclerView.Adapter<AddGeoZoneAdapter.ViewHolder>() {

    var selectPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rootView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_geo_color_item, parent, false)

        return ViewHolder(rootView)
    }

    override fun getItemCount(): Int = colorList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bindData(colorList.get(position), position)

    inner class ViewHolder(private var rootView: View) : RecyclerView.ViewHolder(rootView) {

        @SuppressLint("NotifyDataSetChanged")
        fun bindData(gList: String, position: Int) {
            if(selectPosition>=0 && position==selectPosition) {
                rootView.outSideLayout.background = ContextCompat.getDrawable(context,R.drawable.blackbackground_square)
            }else {
                rootView.outSideLayout.background = null
            }
            rootView.view.setCardBackgroundColor(Color.parseColor(gList))
            rootView.mainLayout.tag = position
            rootView.mainLayout.setOnClickListener {
                selectPosition = rootView.mainLayout.tag as Int
                handler.onColorClick(gList, rootView.mainLayout.tag as Int)
                notifyDataSetChanged()
            }
        }

    }

    interface OnClickColorList {
        fun onColorClick(hexCode: String, position: Int)
    }
}
