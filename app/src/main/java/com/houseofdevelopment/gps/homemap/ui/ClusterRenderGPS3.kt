package com.houseofdevelopment.gps.homemap.ui

import android.view.View
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.houseofdevelopment.gps.vehicallist.model.ItemGps3

class ClusterRenderGPS3 : ClusterItem {
    private var mPosition: LatLng
    private val mTitle: String
    private val mSnippet: String

    var markerView : View? = null

    var mCarModel : ItemGps3

    constructor(lat: Double, lng: Double, carModel : ItemGps3) {
        mPosition = LatLng(lat, lng)
        mTitle = carModel.name.toString()
        mSnippet = "null"
        mCarModel = carModel
    }

    fun updateItems(lat: Double, lng: Double,carModel : ItemGps3) {
        mPosition = LatLng(lat, lng)
        mCarModel = carModel
    }


    override fun getPosition(): LatLng {
        return mPosition
    }

    override fun getTitle(): String {
        return mTitle
    }

    override fun getSnippet(): String {
        return mSnippet
    }


}