package com.houseofdevelopment.gps.history.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class RouteRootGps3() :Serializable, Parcelable {
    var route: ArrayList<RouteGps3>? = null
    var stops: ArrayList<StopGps3>? = null
    var drives: ArrayList<DriveGps3>? = null
    var  route_length = 0.0
    var top_speed = 0
    var avg_speed = 0
    var fuel_consumption = 0
    var fuel_cost = 0
    var stops_duration_time = 0
    var stops_duration: String = ""
    var drives_duration_time = 0
    var drives_duration: String = ""
    var engine_work_time = 0
    var engine_idle_time = 0
    var engine_work: String = ""
    var engine_idle: String = ""
    var fuel_consumption_per_100km = 0
    var fuel_consumption_mpg = 0

    constructor(parcel: Parcel) : this() {
        route_length = parcel.readDouble()
        top_speed = parcel.readInt()
        avg_speed = parcel.readInt()
        fuel_consumption = parcel.readInt()
        fuel_cost = parcel.readInt()
        stops_duration_time = parcel.readInt()
        stops_duration = parcel.readString()!!
        drives_duration_time = parcel.readInt()
        drives_duration = parcel.readString()!!
        engine_work_time = parcel.readInt()
        engine_idle_time = parcel.readInt()
        engine_work = parcel.readString()!!
        engine_idle = parcel.readString()!!
        fuel_consumption_per_100km = parcel.readInt()
        fuel_consumption_mpg = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(route_length)
        parcel.writeInt(top_speed)
        parcel.writeInt(avg_speed)
        parcel.writeInt(fuel_consumption)
        parcel.writeInt(fuel_cost)
        parcel.writeInt(stops_duration_time)
        parcel.writeString(stops_duration)
        parcel.writeInt(drives_duration_time)
        parcel.writeString(drives_duration)
        parcel.writeInt(engine_work_time)
        parcel.writeInt(engine_idle_time)
        parcel.writeString(engine_work)
        parcel.writeString(engine_idle)
        parcel.writeInt(fuel_consumption_per_100km)
        parcel.writeInt(fuel_consumption_mpg)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RouteRootGps3> {
        override fun createFromParcel(parcel: Parcel): RouteRootGps3 {
            return RouteRootGps3(parcel)
        }

        override fun newArray(size: Int): Array<RouteRootGps3?> {
            return arrayOfNulls(size)
        }
    }
}