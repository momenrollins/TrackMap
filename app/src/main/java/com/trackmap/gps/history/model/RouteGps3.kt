package com.trackmap.gps.history.model

import com.trackmap.gps.track.model.Params

class RouteGps3  {
    var dt_tracker: String = ""
    var lat: String = ""
    var lng: String = ""
    var altitude: String = ""
    var angle: String = ""
    var speed = 0
    var params: Params? = null
}