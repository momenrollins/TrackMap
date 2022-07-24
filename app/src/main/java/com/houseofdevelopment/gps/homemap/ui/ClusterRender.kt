package com.houseofdevelopment.gps.homemap.ui

import android.view.View
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.houseofdevelopment.gps.homemap.model.Item


class ClusterRender(lat: Double, lng: Double, carModel: Item) : ClusterItem {
    private var mPosition: LatLng
    private val mTitle: String
    private val mSnippet: String

    var markerView : View? = null

    var mCarModel : Item = carModel

    init {
        mPosition = LatLng(lat, lng)
        mTitle = carModel.nm.toString()
        mSnippet = carModel.uri.toString()
    }

    fun updateItems(lat: Double, lng: Double,carModel : Item) {
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