package com.trackmap.gps.homemap.ui

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.trackmap.gps.R
import com.trackmap.gps.homemap.model.SensorGps3
import com.trackmap.gps.homemap.model.SensorItemModelGps3

import kotlinx.android.synthetic.main.sensor_list_adapter.view.*

class CarDetailsAdapterGPS3(
    var value: SensorItemModelGps3,
) : RecyclerView.Adapter<CarDetailsAdapterGPS3.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.sensor_list_adapter, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateValues(value1: SensorItemModelGps3) {
        value = value1

        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = value.data!!.size
    private val TAG = "CarDetailsAdapterGPS3"
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            holder.bind(value.data!![position])
        } catch (e: Exception) {
            Log.e(TAG, "onBindViewHolder: ${e.message}")
        }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(item: SensorGps3) {
            with(itemView) {
                val name = item.name
                var value = "."
                if (item.value != null)
                    value = "${item.value} ${item.units}"

                // val status = item.getString("m")
                // val status = item.getString("m")
//
//                var jsonObj = JSONObject()
//                if(valuesObj!=null)
//                    jsonObj = JSONObject(valuesObj)

                sensor_name.text = name
                if (value != null)
                    if (name == "حالة المحرك") {
                        sensor_name.text = context!!.getString(R.string.engine_status)
                        if (value.toDouble() > 0)
                            sensor_status.text = context!!.getString(R.string.on)
                        else
                            sensor_status.text = context!!.getString(R.string.off)
                    } else
                        sensor_status.text = value.toString()
                else
                    sensor_status.text = "-"


            }
        }

    }
}