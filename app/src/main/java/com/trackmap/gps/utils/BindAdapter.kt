package com.trackmap.gps.utils

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.trackmap.gps.homemap.ui.CarDetailsAdapter
import org.json.JSONObject

class BindAdapter {
    companion object {
        @BindingAdapter(value = ["bind:home_tab_list", "bind:valuesObj"], requireAll = false)
        @JvmStatic
        fun bindHomeTabList(
                view: RecyclerView,
                hsSensor: MutableList<JSONObject>?,
                valuesObj:String?
        ) {
            if (hsSensor != null) {
                val adapter = CarDetailsAdapter(hsSensor, valuesObj)
                view.adapter = adapter
            }
        }
    }
}