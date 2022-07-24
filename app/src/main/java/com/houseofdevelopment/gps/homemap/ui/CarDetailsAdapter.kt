package com.houseofdevelopment.gps.homemap.ui

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.houseofdevelopment.gps.R
import kotlinx.android.synthetic.main.sensor_list_adapter.view.*
import org.json.JSONObject

class CarDetailsAdapter(
    var value: MutableList<JSONObject>,
    var valuesObj: String?
) : RecyclerView.Adapter<CarDetailsAdapter.ViewHolder>() {
    private val TAG = "CarDetailsAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.sensor_list_adapter, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateValues(
        value1: MutableList<JSONObject>,
        valuesObj1: String?
    ) {
        value = value1
        valuesObj = valuesObj1
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = value.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            holder.bind(value[position])
        } catch (e: Exception) {
            Log.e(TAG, "onBindViewHolder: ${e.message}")
        }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(item: JSONObject) {
            with(itemView) {
                val name = item.getString("n")
                val status = item.getString("m")

                var jsonObj = JSONObject()
                if (valuesObj != null)
                    jsonObj = JSONObject(valuesObj)

                sensor_name.text = name
                if (status.equals("On/Off", true)) {
                    Log.d("TAG", "bind: ${jsonObj.get(item.getString("id")).toString().toDouble()}")
                    if (jsonObj.get(item.getString("id")).toString().toDouble() > 0)
                        sensor_status.text = " ${context.getString(R.string.on)}"
                    else sensor_status.text = context.getString(R.string.off)
                } else {
                    try {
                        sensor_status.text = jsonObj.get(item.getString("id")).toString()
                    } catch (e: Exception) {

                    }
                }
            }
        }

    }
}