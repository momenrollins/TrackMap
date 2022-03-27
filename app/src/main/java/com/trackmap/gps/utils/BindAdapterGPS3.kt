package com.trackmap.gps.utils

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.trackmap.gps.homemap.model.SensorItemModelGps3
import com.trackmap.gps.homemap.ui.CarDetailsAdapterGPS3

class BindAdapterGPS3 {
    companion object {
        @BindingAdapter(value = ["bind:home_tab_list"], requireAll = false)
        @JvmStatic
        fun bindHomeTabList(
            view: RecyclerView,
            hsSensor: SensorItemModelGps3
        ) {
            if (hsSensor != null) {
                val adapter = CarDetailsAdapterGPS3(hsSensor)
                view.adapter = adapter
            }
        }
    }
}