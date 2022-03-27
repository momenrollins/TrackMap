package com.trackmap.gps.track.model

import android.os.Parcel
import android.os.Parcelable
import com.trackmap.gps.history.model.RouteRootGps3
import com.trackmap.gps.history.model.TripDetails
import com.trackmap.gps.homemap.model.UpdatedUnitModel
import com.trackmap.gps.homemap.model.UpdatedUnitModelGps3
import java.io.Serializable

class TrackPathModel() :Serializable, Parcelable {
    var startDate  = ""
    var startTimeStamp  : Long= 0

    var endDate  = ""
    var endTimeStamp  : Long = 0
    var carName=""
    var carSpeed=0
    var carAngle=0

    var dataList = ArrayList<TripDetails.FirstObj>()
    var dataListGps3 = ArrayList<MessageRoot>()
    var  routeRootGps3:RouteRootGps3?=null

    var stopDataList = ArrayList<TripDetails.FirstObj>()


    var selectedCarData =  mutableListOf<UpdatedUnitModel>()
    var selectedCarDataGps3 =  mutableListOf<UpdatedUnitModelGps3>()

    constructor(parcel: Parcel) : this() {
        startDate = parcel.readString()!!
        startTimeStamp = parcel.readLong()
        endDate = parcel.readString()!!
        endTimeStamp = parcel.readLong()
        carName = parcel.readString()!!
        carSpeed = parcel.readInt()
        carAngle = parcel.readInt()
        routeRootGps3 = parcel.readParcelable(RouteRootGps3::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(startDate)
        parcel.writeLong(startTimeStamp)
        parcel.writeString(endDate)
        parcel.writeLong(endTimeStamp)
        parcel.writeString(carName)
        parcel.writeInt(carSpeed)
        parcel.writeInt(carAngle)
        parcel.writeParcelable(routeRootGps3, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TrackPathModel> {
        override fun createFromParcel(parcel: Parcel): TrackPathModel {
            return TrackPathModel(parcel)
        }

        override fun newArray(size: Int): Array<TrackPathModel?> {
            return arrayOfNulls(size)
        }
    }

}