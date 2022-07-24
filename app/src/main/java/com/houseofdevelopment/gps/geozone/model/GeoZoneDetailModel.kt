package com.houseofdevelopment.gps.geozone.model

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName

class GeoZoneDetailModel {
    @SerializedName("id")
    @Expose
    val id: Long = 0

    @SerializedName("n")
    @Expose
    val n: String? = null

    @SerializedName("d")
    @Expose
    val d: String? = null

    @SerializedName("rid")
    @Expose
    val rid: Long = 0

    @SerializedName("t")
    @Expose
    val t: Int = 0

    @SerializedName("w")
    @Expose
    val w: Double = 0.0

    @SerializedName("f")
    @Expose
    val f: Long = 0

    @SerializedName("c")
    @Expose
    val c: Long = 0

    @SerializedName("tc")
    @Expose
    val tc: Long = 0

    @SerializedName("ts")
    @Expose
    val ts: Long = 0

    @SerializedName("min")
    @Expose
    val min: Long = 0

    @SerializedName("max")
    @Expose
    val max: Long = 0

    @SerializedName("i")
    @Expose
    val i: Long = 0

    @SerializedName("libId")
    @Expose
    val libId: Long = 0

    @SerializedName("path")
    @Expose
    val path: String? = null

    @SerializedName("ar")
    @Expose
    val ar = 0.0

    @SerializedName("p")
    @Expose
    val p: ArrayList<P>? = null

    @SerializedName("ct")
    @Expose
    val ct: Long = 0

    @SerializedName("mt")
    @Expose
    val mt: Long = 0
}

class P {
    @SerializedName("x")
    @Expose
    val x = 0.0

    @SerializedName("y")
    @Expose
    val y = 0.0

    @SerializedName("r")
    @Expose
    val r: Long = 0
}