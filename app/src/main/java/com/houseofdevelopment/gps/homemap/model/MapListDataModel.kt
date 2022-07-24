package com.houseofdevelopment.gps.homemap.model

import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.maps.android.clustering.ClusterItem
import java.util.*

class MapListDataModel {

    @SerializedName("items")
    @Expose
    var items: ArrayList<Item> = ArrayList<Item>()
}

class Item {
    @SerializedName("nm")
    @Expose
    val nm: String? = null

    @SerializedName("cls")
    @Expose
    val cls: Int? = null

    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("bact")
    @Expose
    val bact: Int? = null

    @SerializedName("pos")
    @Expose
    val pos: Pos? = null

    @SerializedName("sens")
    @Expose
    val sens: JsonObject? = null

    @SerializedName("lmsg")
    @Expose
    val lmsg: Lmsg? = null

    @SerializedName("uri")
    @Expose
    val uri: String? = null

    // use to add trip data
    var tripFromLong: String? = null
    var tripFromLat: String? = null
    var tripFromT: String? = null
    var tripToLong: String? = null
    var tripToLat: String? = null
    var tripToT: String? = null

    var trip_m: String? = null
    var trip_f: String? = null
    var trip_state: String? = null
    var trip_max_speed: String? = null
    var trip_curr_speed: String? = null
    var trip_avg_speed: String? = null
    var trip_distance: String? = null
    var trip_odometer: String? = null
    var trip_course: String? = null
    var trip_altitude: String? = null

    var car_address: String? = null
    var isSelected = false

    var isExpanded = false
}

class Pos {
    @SerializedName("t")
    @Expose
    val t: Int? = null

    @SerializedName("f")
    @Expose
    val f: Int? = null

    @SerializedName("lc")
    @Expose
    val lc: Any? = null

    @SerializedName("y")
    @Expose
    val y: Double? = null

    @SerializedName("x")
    @Expose
    val x: Double? = null

    @SerializedName("z")
    @Expose
    val z: Double? = null

    @SerializedName("s")
    @Expose
    val s: Int? = null

    @SerializedName("c")
    @Expose
    val c: Int? = null

    @SerializedName("sc")
    @Expose
    val sc: Int? = null
}

class Lmsg {
    @SerializedName("t")
    @Expose
    val t: Int? = null

    @SerializedName("f")
    @Expose
    val f: Int? = null

    @SerializedName("tp")
    @Expose
    val tp: String? = null

    @SerializedName("pos")
    @Expose
    val pos: Pos_? = null

    @SerializedName("lc")
    @Expose
    val lc: Any? = null

    @SerializedName("p")
    @Expose
    val p: P? = null

    @SerializedName("i")
    @Expose
    val i: Int? = null

    @SerializedName("o")
    @Expose
    val o: Int? = null
}

class P {
    @SerializedName("pwr_ext")
    @Expose
    val pwr_ext: String? = null
}

class Pos_ : ClusterItem {
    @SerializedName("y")
    @Expose
    val y: Double? = null

    @SerializedName("x")
    @Expose
    val x: Double? = null

    @SerializedName("z")
    @Expose
    val z: Double? = null

    @SerializedName("s")
    @Expose
    val s: Int? = null

    @SerializedName("c")
    @Expose
    val c: Int? = null

    @SerializedName("sc")
    @Expose
    val sc: Int? = null
    override fun getPosition(): LatLng {
        return LatLng(x!!, y!!)
    }

    override fun getTitle(): String? {
        return null
    }

    override fun getSnippet(): String? {
        return null
    }
}