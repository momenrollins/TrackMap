package com.houseofdevelopment.gps.vehicallist.model

class ItemGps3 {
    var imei: String = ""
    var protocol: String = ""
    var net_protocol: String = ""
    var ip: String = ""
    var port: String = ""
    var active: String = ""
    var object_expire: String = ""
    var object_expire_dt: String = ""
    var dt_server: String = ""
    var dt_tracker: String = ""
    var lat: String = ""
    var lng: String = ""
    var altitude: String = ""
    var angle: String = ""
    var speed: String = ""

//    public Params params;
    var loc_valid: String = ""
    var dt_last_stop: String = ""
    var dt_last_idle: String = ""
    var dt_last_move: String = ""
    var name: String = ""
    var address: String = ""
    var oldAddress: String = ""
    var sens: String = ""
    var device: String = ""
    var sim_number: String = ""
    var model: String = ""
    var vin: String = ""
    var plate_number: String = ""
    var odometer: String = ""
    var engine_hours: String = ""
    var custom_fields: List<Any>? = null
    var isSelected = false
    var isExpanded = false
    var dist = 0.0
}