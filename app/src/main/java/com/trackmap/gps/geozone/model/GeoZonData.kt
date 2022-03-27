package com.trackmap.gps.geozone.model

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

class GeoZonData : Serializable {
    var radius = 0.0
    var centerLatLong = LatLng(0.0, 0.0)
    var minLatLong = LatLng(0.0, 0.0)
    var maxLatLong = LatLng(0.0, 0.0)
    var colorCode = 0
    var id = ""
}